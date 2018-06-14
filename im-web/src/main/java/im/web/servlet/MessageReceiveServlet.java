package im.web.servlet;

import javax.servlet.AsyncContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import im.netty.message.MessageHandle;
import im.protobuf.message.ImMessage;

/**
 * MessageReceiveServlet
 * Date: 2018-06-14
 *
 * @author zouchuanhua
 */
@WebServlet(asyncSupported = true, urlPatterns = "/messageReceive")
public class MessageReceiveServlet extends HttpServlet {

    private static final Logger LOGGER = LoggerFactory.getLogger(MessageReceiveServlet.class);

    private static final ExecutorService EXECUTOR_SERVICE = Executors.newFixedThreadPool(2);
    private static final BlockingQueue<AsyncContext> QUEUE = new LinkedBlockingQueue<>(1000);

    static {
        EXECUTOR_SERVICE.execute(() -> {
            while (true) {
                AsyncContext asyncContext = null;
                try {
                    asyncContext = QUEUE.take();
                    HttpServletRequest request = (HttpServletRequest) asyncContext.getRequest();
                    ImMessage.Message.Builder builder = ImMessage.Message.newBuilder();
                    ImMessage.Message message = builder.build();
                    MessageHandle.getINSTANCE().send(message);
                    asyncContext.getResponse().getWriter().write("ok");
                } catch (InterruptedException e) {
                    LOGGER.info("interrupted", e);
                    Thread.currentThread().interrupt();
                    break;
                } catch (IOException e1) {
                    LOGGER.error("", e1);
                } finally {
                    asyncContext.complete();
                }
            }
        });
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        QUEUE.add(req.startAsync());
    }

}
