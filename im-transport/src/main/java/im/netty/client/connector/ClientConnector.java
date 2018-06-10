package im.netty.client.connector;

import io.netty.channel.Channel;

/**
 * 
 * @author BazingaLyn
 * @description fork from https://github.com/BazingaLyn/netty-study/tree/master/src/main/java/com/lyncc/netty
 * @modifytime
 */
public interface ClientConnector {
	
	Channel connect(int port, String host);
	
	void shutdownGracefully();
	
}
