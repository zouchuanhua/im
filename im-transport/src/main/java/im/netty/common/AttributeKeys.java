package im.netty.common;

import io.netty.util.AttributeKey;

/**
 * AttributeKeys
 * Date: 2018-06-13
 *
 * @author zouchuanhua
 */
public class AttributeKeys {

    public static final AttributeKey<Integer> USER_ID_KEY =
            AttributeKey.valueOf(Integer.class, "USER_ID_KEY");
}
