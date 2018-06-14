package im.netty.srv.acceptor;

import java.util.List;
import java.util.Map;


import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import im.authentication.TokenGenerate;
import im.netty.common.AttributeKeys;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpRequest;

import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.handler.codec.http.QueryStringDecoder;


import static io.netty.handler.codec.http.HttpMethod.GET;
import static io.netty.handler.codec.http.HttpResponseStatus.FORBIDDEN;
import static io.netty.handler.codec.http.HttpUtil.isKeepAlive;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;

/**
 * TokenValidateHandler
 * Date: 2018-06-12
 *
 * @author zouchuanhua
 */
public class TokenValidateHandler extends ChannelInboundHandlerAdapter {

    private String websocketPath;
    private boolean checkStartsWith;

    public TokenValidateHandler() {

    }

    public TokenValidateHandler(String websocketPath, boolean checkStartsWith) {
        this.websocketPath = websocketPath;
        this.checkStartsWith = checkStartsWith;
    }

    @Override
    public void channelRead(final ChannelHandlerContext ctx, Object msg) throws Exception {

        if (!(msg instanceof FullHttpRequest)) {
            ctx.fireChannelRead(msg);
            return;
        }

        final FullHttpRequest req = (FullHttpRequest) msg;
        if (req.method() != GET) {
            sendHttpResponse(ctx, req, new DefaultFullHttpResponse(HTTP_1_1, FORBIDDEN));
            return;
        }

        String token = null;
        QueryStringDecoder decoder = new QueryStringDecoder(req.uri());
        for (Map.Entry<String, List<String>> entry : decoder.parameters().entrySet()) {
            if (entry.getKey().equals("x-im-token")) {
                token = entry.getValue().get(0);
            }
        }

        if (token == null || "".equals(token)) {
            sendHttpResponse(ctx, req, new DefaultFullHttpResponse(HttpVersion.HTTP_1_1,
                    HttpResponseStatus.FORBIDDEN));
            return;
        }

        try {
            String subject = TokenGenerate.getSubject(token);
            JSONObject object = JSON.parseObject(subject);
            ctx.channel().attr(AttributeKeys.USER_ID_KEY).set(object.getIntValue("userId"));
            ctx.fireChannelRead(msg);
        } catch (Exception e) {
            sendHttpResponse(ctx, req, new DefaultFullHttpResponse(HttpVersion.HTTP_1_1,
                    HttpResponseStatus.FORBIDDEN));
        }

    }

    private boolean isNotWebSocketPath(FullHttpRequest req) {
        return checkStartsWith ? !req.uri().startsWith(websocketPath) : !req.uri().equals(websocketPath);
    }

    private static void sendHttpResponse(ChannelHandlerContext ctx, HttpRequest req, HttpResponse res) {
        ChannelFuture f = ctx.channel().writeAndFlush(res);
        if (!isKeepAlive(req) || res.status().code() != 200) {
            f.addListener(ChannelFutureListener.CLOSE);
        }
    }

}
