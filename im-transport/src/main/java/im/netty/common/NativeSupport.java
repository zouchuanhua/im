package im.netty.common;

/**
 * 
 * @author zouchuanhua
 *  * @description fork from https://github.com/BazingaLyn/netty-study/tree/master/src/main/java/com/lyncc/netty
 * @copyright fjc
 * @time 2016年7月20日20:48:08
 * @modifytime
 */
public final class NativeSupport {

    private static final boolean SUPPORT_NATIVE_ET;

    static {
        boolean epoll;
        try {
            Class.forName("io.netty.channel.epoll.Native");
            epoll = true;
        } catch (Throwable e) {
            epoll = false;
        }
        SUPPORT_NATIVE_ET = epoll;
    }

    /**
     * The native socket transport for Linux using JNI.
     */
    public static boolean isSupportNativeET() {
        return SUPPORT_NATIVE_ET;
    }
}
