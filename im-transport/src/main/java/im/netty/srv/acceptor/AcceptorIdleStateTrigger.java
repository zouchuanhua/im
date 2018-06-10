package im.netty.srv.acceptor;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * @author zouchuanhua
 * @description fork from https://github.com/BazingaLyn/netty-study/tree/master/src/main/java/com/lyncc/netty
 *
 */

@ChannelHandler.Sharable
public class AcceptorIdleStateTrigger extends ChannelInboundHandlerAdapter {
	
	private static final Logger logger = LoggerFactory.getLogger(AcceptorIdleStateTrigger.class);

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent) {
            IdleState state = ((IdleStateEvent) evt).state();
            if (state == IdleState.READER_IDLE) {
            	logger.error("occor exception");
                throw new Exception("NO SIGNAL");
            }
        } else {
            super.userEventTriggered(ctx, evt);
        }
    }
}
