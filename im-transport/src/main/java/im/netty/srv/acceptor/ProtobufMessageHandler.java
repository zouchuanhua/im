package im.netty.srv.acceptor;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import im.netty.message.MessageHandle;
import im.protobuf.message.ImMessage;


import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;



/**
 * ProtobufMessageHandler
 * Date: 2018-06-13
 *
 * @author zouchuanhua
 */
public class ProtobufMessageHandler extends SimpleChannelInboundHandler<ImMessage.Message> {

    private static final Logger LOGGER = LoggerFactory.getLogger(ProtobufMessageHandler.class);

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, ImMessage.Message msg) throws Exception {
        MessageHandle.getINSTANCE().send(msg);
    }
}
