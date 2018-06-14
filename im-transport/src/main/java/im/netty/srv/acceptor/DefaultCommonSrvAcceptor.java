package im.netty.srv.acceptor;



import im.netty.common.NativeSupport;

import im.protobuf.message.ImMessage;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

import io.netty.channel.ChannelFuture;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;


import io.netty.channel.EventLoopGroup;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;

import io.netty.handler.codec.http.HttpObjectAggregator;

import io.netty.handler.codec.http.HttpServerCodec;


import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;

import io.netty.handler.codec.http.websocketx.WebSocketFrame;

import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.netty.handler.codec.http.websocketx.extensions.compression.WebSocketServerCompressionHandler;
import io.netty.handler.codec.protobuf.ProtobufDecoder;
import io.netty.handler.codec.protobuf.ProtobufVarint32LengthFieldPrepender;


import java.net.InetSocketAddress;
import java.net.SocketAddress;

import java.util.List;
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
                                new WebSocketServerCompressionHandler(),
                                new TokenValidateHandler(),
                                new MessageHandle(),
                                new UserConnectManageHandler(),
                                new ProtobufDecoder(ImMessage.Message.getDefaultInstance()),
                                new ProtobufVarint32LengthFieldPrepender(),
                                new ProtobufMessageHandler()
                        );
                    }
                });

        return boot.bind(localAddress);
    }


    static class MessageHandle extends WebSocketServerProtocolHandler {


        public MessageHandle() {
            super("/", null, true, 65536, false, true);
        }

        @Override
        protected void decode(ChannelHandlerContext ctx, WebSocketFrame frame, List<Object> out) throws Exception {

            super.decode(ctx, frame, out);

            if (frame instanceof BinaryWebSocketFrame) {
                BinaryWebSocketFrame binaryWebSocketFrame = (BinaryWebSocketFrame) frame;
                byte[] by = new byte[frame.content().readableBytes()];
                binaryWebSocketFrame.content().readBytes(by);
                ByteBuf bytebuf = Unpooled.buffer();
                bytebuf.writeBytes(by);
                out.add(bytebuf);
            }
        }

    }


    @Override
    protected ChannelEventListener getChannelEventListener() {
        return channelEventListener;
    }
}
