package im.netty.srv.acceptor;

import java.net.SocketAddress;

/**
 * 
 * @author zouchuanhua
 * @description netty server端的标准接口定义
 *  fork from https://github.com/BazingaLyn/netty-study/tree/master/src/main/java/com/lyncc/netty
 * @modifytime
 */
public interface SrvAcceptor {
	
	SocketAddress localAddress();
	
	void start() throws InterruptedException;
	
	void shutdownGracefully();
	
	void start(boolean sync) throws InterruptedException;

}
