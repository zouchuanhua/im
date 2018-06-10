package im.netty.common;

import static im.netty.common.NettyCommonProtocol.HEAD_LENGTH;
import static im.netty.common.NettyCommonProtocol.HEARTBEAT;
import static im.netty.common.NettyCommonProtocol.MAGIC;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

/**
 * 
 * @author zouchuanhua
 * @description 心跳包
 *  fork from https://github.com/BazingaLyn/netty-study/tree/master/src/main/java/com/lyncc/netty
 * @time
 * @modifytime
 */
public class Heartbeats {

    private static final ByteBuf HEARTBEAT_BUF;

    static {
        ByteBuf buf = Unpooled.buffer(HEAD_LENGTH);
        buf.writeShort(MAGIC);
        buf.writeByte(HEARTBEAT);
        buf.writeByte(0);
        buf.writeLong(0);
        buf.writeInt(0);
        HEARTBEAT_BUF = buf.asReadOnly();
    }

    public static ByteBuf heartbeatContent() {
        return HEARTBEAT_BUF.duplicate();
    }
}
