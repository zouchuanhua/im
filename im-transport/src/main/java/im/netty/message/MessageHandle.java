package im.netty.message;


import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import im.netty.srv.acceptor.UserConnectManageHandler;
import im.protobuf.message.ImMessage;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;

/**
 * 消息处理，分发
 * Date: 2018-06-14
 *
 * @author zouchuanhua
 */
public class MessageHandle {

    private static final ThreadPoolExecutor POOL_EXECUTOR = new ThreadPoolExecutor(10, 10, 60, TimeUnit.SECONDS, new LinkedBlockingQueue<>(1000),
            new ThreadFactory() {
                @Override
                public Thread newThread(Runnable r) {
                    Thread thread = new Thread(r);
                    thread.setName("messageHandle");
                    thread.setDaemon(true);
                    return thread;
                }
            });

    private static final MessageHandle INSTANCE = new MessageHandle();

    private MessageHandle() {
    }

    public static MessageHandle getINSTANCE() {
        return INSTANCE;
    }

    public void send(ImMessage.Message message) {

        POOL_EXECUTOR.execute(() -> {

            Channel targetChannel = UserConnectManageHandler.getChannelMap().get(message.getToId());
            if (targetChannel != null) {
                ByteBuf byteBuf = Unpooled.buffer().writeBytes(message.toByteArray());
                targetChannel.writeAndFlush(new BinaryWebSocketFrame(byteBuf)).addListener((future) -> {
                    if (future.isSuccess()) {

                    } else {

                    }
                });
            } else {

            }
        });
    }


}
