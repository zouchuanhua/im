package im.mq.rabbitmq;

import java.io.IOException;



import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.Semaphore;

import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;


/**
 * Client
 * Date: 2018-06-09
 *
 * @author zouchuanhua
 */
public class Client {

    private static final Logger LOGGER = LoggerFactory.getLogger(Client.class);

    private static final Client INSTANCE = new Client();

    private Connection connection;

    private ChannelPool channelPool;

    private Client() {
        this.connection = getConnection();
        this.channelPool = new ChannelPool(25,connection);
    }

    public static Client getINSTANCE() {
        return INSTANCE;
    }

    public Connection getConnection() {
        ConnectionFactory connectionFactory = new ConnectionFactory();
        connectionFactory.setHost("127.0.0.1");
        connectionFactory.setUsername("admin");
        connectionFactory.setPassword("admin");
        try {
            this.connection = connectionFactory.newConnection();
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (TimeoutException e) {
            throw new RuntimeException(e);
        }
        return connection;
    }

    public void push(String exchange,String routeKey,String message) {
        Channel channel = this.channelPool.getChannel();
        if(channel == null) {
            throw new RuntimeException("can not get channel from channelPool");
        }

        try {
            channel.basicPublish(exchange,routeKey,null,message.getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            this.channelPool.release(channel);
        }

    }


    private static final class ChannelPool {

        private int maxSize;
        private AtomicInteger activeSize = new AtomicInteger(0);
        private Connection connection;
        private Semaphore semaphore;
        private BlockingQueue<Channel> idleChannels = new LinkedBlockingQueue<>();

        public ChannelPool(int maxSize,Connection connection) {
            this.maxSize = maxSize;
            this.connection = connection;
            this.semaphore = new Semaphore(maxSize);
        }

        public Channel getChannel() {
            semaphore.acquireUninterruptibly();

            Channel channel = idleChannels.poll();

            if(channel == null) {
                if(activeSize.get() < maxSize) {
                    try {
                        channel = connection.createChannel();
                    } catch (IOException e) {
                        channel = getChannel();
                    }
                }
            }else {
                if(!channel.isOpen()) {
                    idleChannels.remove(channel);
                    channel = getChannel();
                }
            }
            if(channel !=null) {
                activeSize.incrementAndGet() ;
            }
            return channel;
        }

        public void release(Channel channel) {
            idleChannels.offer(channel);
            activeSize.decrementAndGet();
            semaphore.release();
        }

    }

    public static void main(String[] args) {
        Client client = Client.getINSTANCE();

        ExecutorService executorService = Executors.newFixedThreadPool(30);
        for (int i = 1; i <= 5000; i++) {
            executorService.execute(new Runnable() {
                @Override
                public void run() {
                    client.push("test.log","test.log","nihao");
                }
            });
        }

    }

}
