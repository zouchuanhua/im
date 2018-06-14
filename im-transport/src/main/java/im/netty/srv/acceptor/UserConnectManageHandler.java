package im.netty.srv.acceptor;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import im.netty.common.AttributeKeys;
import io.netty.channel.Channel;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;

/**
 * UserConnectManageHandler
 * Date: 2018-06-13
 *
 * @author zouchuanhua
 */
public class UserConnectManageHandler extends ChannelDuplexHandler {

    private static final Logger logger = LoggerFactory.getLogger(UserConnectManageHandler.class);

    private static final Map<Integer, Channel> channelMap = new ConcurrentHashMap<>();


    public static Map<Integer, Channel> getChannelMap() {
        return channelMap;
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        final String remoteAddress = ctx.channel().remoteAddress().toString();
        logger.warn("NETTY CLIENT PIPELINE: exceptionCaught {}", remoteAddress);
        logger.warn("NETTY CLIENT PIPELINE: exceptionCaught exception.", cause);
    }

    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
        long userId = ctx.channel().attr(AttributeKeys.USER_ID_KEY).get();
        channelMap.remove(userId);
        logger.info("handler removed,userId={}", userId);
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof WebSocketServerProtocolHandler.HandshakeComplete) {
            Channel channel = ctx.channel();
            int userId = ctx.channel().attr(AttributeKeys.USER_ID_KEY).get();
            channelMap.put(userId, channel);
            logger.info("user online,userId={}", userId);
        }
    }
}
