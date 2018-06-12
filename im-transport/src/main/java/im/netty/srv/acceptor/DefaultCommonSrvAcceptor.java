package im.netty.srv.acceptor;

import static im.netty.common.NettyCommonProtocol.HEARTBEAT;
import static im.netty.common.NettyCommonProtocol.MAGIC;
import static im.netty.common.NettyCommonProtocol.REQUEST;
import static im.netty.common.NettyCommonProtocol.SERVICE_1;
import static im.netty.common.NettyCommonProtocol.SERVICE_2;
import static im.netty.common.NettyCommonProtocol.SERVICE_3;
import static im.netty.common.NettyCommonProtocol.SERVICE_4;

import static im.netty.serializer.SerializerHolder.serializerImpl;


import im.authentication.TokenGenerate;
import im.netty.common.Message;
import im.netty.common.NativeSupport;
import im.netty.common.NettyCommonProtocol;
import im.netty.common.NettyEvent;
import im.netty.common.NettyEventType;
import io.jsonwebtoken.SignatureException;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.ChannelPromise;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.MessageToByteEncoder;
import io.netty.handler.codec.ReplayingDecoder;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpObjectAggregator;

import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.HttpUtil;

import io.netty.handler.codec.http.QueryStringDecoder;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;
import io.netty.handler.codec.http.websocketx.CloseWebSocketFrame;
import io.netty.handler.codec.http.websocketx.PingWebSocketFrame;
import io.netty.handler.codec.http.websocketx.PongWebSocketFrame;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketServerHandshaker;
import io.netty.handler.codec.http.websocketx.WebSocketServerHandshakerFactory;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.netty.handler.codec.http.websocketx.extensions.compression.WebSocketServerCompressionHandler;

import io.netty.util.CharsetUtil;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * @author zouchuanhua
 * @description 基本的常用的netty Server配置  fork from https://github.com/BazingaLyn/netty-study/tree/master/src/main/java/com/lyncc/netty
 * @time
 * @modifytime
 */
public class DefaultCommonSrvAcceptor extends DefaultSrvAcceptor {


    private static final Logger logger = LoggerFactory.getLogger(DefaultCommonSrvAcceptor.class);

    //acceptor的trigger
    private final AcceptorIdleStateTrigger idleStateTrigger = new AcceptorIdleStateTrigger();

    //message的编码器
    private final MessageEncoder encoder = new MessageEncoder();

    //Ack的编码器
    private final AcknowledgeEncoder ackEncoder = new AcknowledgeEncoder();

    //SimpleChannelInboundHandler类型的handler只处理@{link Message}类型的数据
//    private final MessageHandler handler = new MessageHandler();

    private final ChannelEventListener channelEventListener;

    public DefaultCommonSrvAcceptor(int port, ChannelEventListener channelEventListener) {
        super(new InetSocketAddress(port));
        this.init();
        this.channelEventListener = channelEventListener;
    }

    @Override
    protected void init() {
        super.init();

        bootstrap().option(ChannelOption.SO_BACKLOG, 32768)
                .option(ChannelOption.SO_REUSEADDR, true)
                .childOption(ChannelOption.SO_REUSEADDR, true)
                .childOption(ChannelOption.SO_KEEPALIVE, true)
                .childOption(ChannelOption.TCP_NODELAY, true)
                .childOption(ChannelOption.ALLOW_HALF_CLOSURE, false);

    }


    @Override
    protected EventLoopGroup initEventLoopGroup(int nthread, ThreadFactory bossFactory) {
        return NativeSupport.isSupportNativeET() ? new EpollEventLoopGroup(nthread, bossFactory) : new NioEventLoopGroup(nthread, bossFactory);
    }


    @Override
    protected ChannelFuture bind(SocketAddress localAddress) {
        ServerBootstrap boot = bootstrap();

        boot.channel(NioServerSocketChannel.class)
                .childHandler(new ChannelInitializer<SocketChannel>() {

                    @Override
                    protected void initChannel(SocketChannel ch) throws Exception {
                        ch.pipeline().addLast(
                                new HttpServerCodec(),
                                new HttpObjectAggregator(65536),
                                new NettyConnetManageHandler(),
                                new WebSocketServerCompressionHandler(),
//                                new WebSocketServerProtocolHandler("/",null,true),
                                new MessageDecoder()
                        );
                    }
                });

        return boot.bind(localAddress);
    }

    /**
     *
     */
    /**
     * **************************************************************************************************
     * Protocol
     * ┌ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ┐
     * 2   │   1   │    1   │     8     │      4      │
     * ├ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ┤
     * │       │        │           │             │
     * │  MAGIC   Sign    Status   Invoke Id   Body Length                   Body Content              │
     * │       │        │           │             │
     * └ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ┘
     * <p>
     * 消息头16个字节定长
     * = 2 // MAGIC = (short) 0xbabe
     * + 1 // 消息标志位, 用来表示消息类型
     * + 1 // 空
     * + 8 // 消息 id long 类型
     * + 4 // 消息体body长度, int类型
     */
    static class MessageDecoder extends WebSocketServerProtocolHandler {


        public MessageDecoder() {
            super("/", null, true);
        }

        @Override
        public void handlerAdded(ChannelHandlerContext ctx) {
            ChannelPipeline cp = ctx.pipeline();
            if (cp.get(HandshakeHandler.class) == null) {
                cp.addBefore(ctx.name(), HandshakeHandler.class.getName(), new HandshakeHandler("/", false));
            }
        }

        @Override
        protected void decode(ChannelHandlerContext ctx, WebSocketFrame frame, List<Object> out) throws Exception {
            super.decode(ctx, frame, out);
            //文本帧处理(收到的消息广播到前台客户端)
            if (frame instanceof TextWebSocketFrame) {
                logger.info("文本帧消息：" + ((TextWebSocketFrame) frame).text());
                ctx.channel().writeAndFlush(new TextWebSocketFrame("hello world"));
            }
            //二进制帧处理,将帧的内容往下传
            else if (frame instanceof BinaryWebSocketFrame) {
                System.out.println("The WebSocketFrame is BinaryWebSocketFrame");
                BinaryWebSocketFrame binaryWebSocketFrame = (BinaryWebSocketFrame) frame;
                byte[] by = new byte[frame.content().readableBytes()];
                binaryWebSocketFrame.content().readBytes(by);
                ByteBuf bytebuf = Unpooled.buffer();
                bytebuf.writeBytes(by);
                out.add(bytebuf);
            }
        }

        @Override
        public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
            logger.info("handleRemoved,{}", ctx);
        }
    }

    /**
     * **************************************************************************************************
     * Protocol
     * ┌ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ┐
     * 2   │   1   │    1   │     8     │      4      │
     * ├ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ┤
     * │       │        │           │             │
     * │  MAGIC   Sign    Status   Invoke Id   Body Length                   Body Content              │
     * │       │        │           │             │
     * └ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ┘
     * <p>
     * 消息头16个字节定长
     * = 2 // MAGIC = (short) 0xbabe
     * + 1 // 消息标志位, 用来表示消息类型
     * + 1 // 空
     * + 8 // 消息 id long 类型
     * + 4 // 消息体body长度, int类型
     */
    @ChannelHandler.Sharable
    static class MessageEncoder extends MessageToByteEncoder<Message> {

        @Override
        protected void encode(ChannelHandlerContext ctx, Message msg, ByteBuf out) throws Exception {
            byte[] bytes = serializerImpl().writeObject(msg);
            out.writeShort(MAGIC)
                    .writeByte(msg.sign())
                    .writeByte(0)
                    .writeLong(0)
                    .writeInt(bytes.length)
                    .writeBytes(bytes);
        }
    }

//    @ChannelHandler.Sharable
//    class MessageHandler extends SimpleChannelInboundHandler<Object> {
//
//        private WebSocketServerHandshaker handShaker;
//
//        @Override
//        protected void channelRead0(ChannelHandlerContext ctx, Object message) throws Exception {
//
//            Channel channel = ctx.channel();
//            if (message instanceof FullHttpRequest) {
//                FullHttpRequest request = (FullHttpRequest) message;
//                String token = request.headers().get("x-im-token");
//
//                if (token == null) {
//                    QueryStringDecoder decoder = new QueryStringDecoder(request.uri());
//                    for (Map.Entry<String, List<String>> entry : decoder.parameters().entrySet()) {
//                        if (entry.getKey().equals("x-im-token")) {
//                            token = entry.getValue().get(0);
//                        }
//                    }
//                }
//
//                if (token == null || "".equals(token)) {
////                    sendHttpResponse(ctx, request, new DefaultFullHttpResponse(HttpVersion.HTTP_1_1,
////                            HttpResponseStatus.FORBIDDEN));
////                    return;
//                }
//
//                try {
//                    TokenGenerate.parser(token);
//                } catch (Exception e) {
////                    sendHttpResponse(ctx, request, new DefaultFullHttpResponse(HttpVersion.HTTP_1_1,
////                            HttpResponseStatus.FORBIDDEN));
////                    return;
//                }
//
//                WebSocketServerHandshakerFactory wsFactory = new WebSocketServerHandshakerFactory(
//                        "ws://localhost:8888", null, true);
//                handShaker = wsFactory.newHandshaker(request);
//                if (handShaker == null) {
//                    WebSocketServerHandshakerFactory
//                            .sendUnsupportedVersionResponse(ctx.channel());
//                } else {
//                    handShaker.handshake(ctx.channel(), request);
//                }
//            } else if (message instanceof WebSocketFrame) {
//                WebSocketFrame frame = (WebSocketFrame) message;
//                // 判断是否是关闭链路的指令
//                if (frame instanceof CloseWebSocketFrame) {
//                    handShaker.close(ctx.channel(),
//                            (CloseWebSocketFrame) frame.retain());
//                    return;
//                }
//                // 判断是否是Ping消息
//                if (frame instanceof PingWebSocketFrame) {
//                    ctx.channel().write(
//                            new PongWebSocketFrame(frame.content().retain()));
//                    return;
//                }
//                // 本例程仅支持文本消息，不支持二进制消息
//                if (!(frame instanceof TextWebSocketFrame)) {
//                    throw new UnsupportedOperationException(String.format(
//                            "%s frame types not supported", frame.getClass().getName()));
//                }
//
//                // 返回应答消息
//                String request = ((TextWebSocketFrame) frame).text();
//                channel.writeAndFlush(
//                        new TextWebSocketFrame(request
//                                + " , 欢迎使用Netty WebSocket服务，现在时刻："
//                                + new java.util.Date().toString()));
//            }
//
//            // 接收到发布信息的时候，要给Client端回复ACK
////			channel.writeAndFlush(new Acknowledge(message.sequence())).addListener(ChannelFutureListener.FIRE_EXCEPTION_ON_FAILURE);
//        }
//
//        private void sendHttpResponse(ChannelHandlerContext ctx,
//                                      FullHttpRequest req, FullHttpResponse res) {
//            // 返回应答给客户端
//            if (res.getStatus().code() != 200) {
//                ByteBuf buf = Unpooled.copiedBuffer(res.getStatus().toString(),
//                        CharsetUtil.UTF_8);
//                res.content().writeBytes(buf);
//                buf.release();
//                HttpUtil.setContentLength(res, res.content().readableBytes());
//            }
//
//            // 如果是非Keep-Alive，关闭连接
//            ChannelFuture f = ctx.channel().writeAndFlush(res);
//            if (!HttpUtil.isKeepAlive(req) || res.status().code() != 200) {
//                f.addListener(ChannelFutureListener.CLOSE);
//            }
//        }
//    }

    class NettyConnetManageHandler extends ChannelDuplexHandler {

        @Override
        public void connect(ChannelHandlerContext ctx, SocketAddress remoteAddress, SocketAddress localAddress, ChannelPromise future) throws Exception {
            final String local = localAddress == null ? "UNKNOW" : localAddress.toString();
            final String remote = remoteAddress == null ? "UNKNOW" : remoteAddress.toString();
            logger.info("NETTY CLIENT PIPELINE: CONNECT  {} => {}", local, remote);
            super.connect(ctx, remoteAddress, localAddress, future);

            if (DefaultCommonSrvAcceptor.this.channelEventListener != null) {
                DefaultCommonSrvAcceptor.this.putNettyEvent(new NettyEvent(NettyEventType.CONNECT, remoteAddress
                        .toString(), ctx.channel()));
            }
        }

        @Override
        public void disconnect(ChannelHandlerContext ctx, ChannelPromise future) throws Exception {
            final String remoteAddress = ctx.channel().remoteAddress().toString();
            logger.info("NETTY CLIENT PIPELINE: DISCONNECT {}", remoteAddress);
            super.disconnect(ctx, future);

            if (DefaultCommonSrvAcceptor.this.channelEventListener != null) {
                DefaultCommonSrvAcceptor.this.putNettyEvent(new NettyEvent(NettyEventType.CLOSE, remoteAddress, ctx.channel()));
            }
        }

        @Override
        public void close(ChannelHandlerContext ctx, ChannelPromise promise) throws Exception {
            final String remoteAddress = ctx.channel().remoteAddress().toString();
            logger.info("NETTY CLIENT PIPELINE: CLOSE {}", remoteAddress);
            super.close(ctx, promise);

            if (DefaultCommonSrvAcceptor.this.channelEventListener != null) {
                DefaultCommonSrvAcceptor.this.putNettyEvent(new NettyEvent(NettyEventType.CLOSE, remoteAddress, ctx.channel()));
            }
        }


        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
            final String remoteAddress = ctx.channel().remoteAddress().toString();
            logger.warn("NETTY CLIENT PIPELINE: exceptionCaught {}", remoteAddress);
            logger.warn("NETTY CLIENT PIPELINE: exceptionCaught exception.", cause);
            if (DefaultCommonSrvAcceptor.this.channelEventListener != null) {
                DefaultCommonSrvAcceptor.this.putNettyEvent(new NettyEvent(NettyEventType.EXCEPTION, remoteAddress, ctx.channel()));
            }
        }
    }

    @Override
    protected ChannelEventListener getChannelEventListener() {
        return channelEventListener;
    }
}
