package im.router;

import im.cache.redis.RedisClient;
import im.common.http.HttpClient;
import im.common.http.MediaConstants;

/**
 * DefaultRouter
 * Date: 2018-06-14
 *
 * @author zouchuanhua
 */
public class DefaultRouter implements Router{

    private static final HttpClient httpClient = HttpClient.getINSTANCE();

    private static final RedisClient redisClient = RedisClient.getINSTANCE();

    private static final String MESSAGE_URL = "im/messageReceive";


    @Override
    public void forwardMessage() {
        // 找到所在的机器节点，发送http请求
        String ip = redisClient.get("");
        if(ip == null) {
            // 可能离线了
            return;
        }
        String resp = httpClient.post(ip + MESSAGE_URL, "{}", MediaConstants.JSON, String.class);
    }
}
