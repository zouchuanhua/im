package im.web.servlet;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import im.netty.srv.acceptor.DefaultCommonSrvAcceptor;

/**
 * ImServerListener
 * Date: 2018-06-12
 *
 * @author zouchuanhua
 */
@WebListener
public class ImServerListener implements ServletContextListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(ImServerListener.class);

    private static final ExecutorService SERVICE = Executors.newSingleThreadExecutor();

    private DefaultCommonSrvAcceptor acceptor;

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        SERVICE.execute(()->{
            DefaultCommonSrvAcceptor acceptor = new DefaultCommonSrvAcceptor(8888,null);
            this.acceptor = acceptor;
            try {
                acceptor.start();
            } catch (InterruptedException e) {
                LOGGER.error("server start interrupted",e);
            }
        });

        Runtime.getRuntime().addShutdownHook(new Thread(()->{
            if (acceptor != null) {
                this.acceptor.shutdownGracefully();
            }
        }));
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        if(acceptor!=null) {
            acceptor.shutdownGracefully();
        }
    }
}
