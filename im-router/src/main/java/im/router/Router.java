package im.router;

import im.cache.redis.RedisClient;
import im.common.http.HttpClient;
import im.common.http.MediaConstants;

/**
 * 消息路由服务
 * Date: 2018-06-14
 *
 * @author zouchuanhua
 */
public interface Router {

    void forwardMessage();
}
