package im.common.http;


import java.util.concurrent.TimeUnit;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * http client base on okhttp
 * Date: 2018-06-14
 *
 * @author zouchuanhua
 */
public class HttpClient {


    private static final OkHttpClient client = new OkHttpClient().newBuilder()
            .connectTimeout(3, TimeUnit.SECONDS)
            .readTimeout(3, TimeUnit.SECONDS)
            .build();


    public <T> T post(String url, String param, MediaType mediaType, Class<T> respType) {
        RequestBody requestBody = RequestBody.create(mediaType, param);
        Request request = new Request.Builder().url(url).post(requestBody).build();

        try {
            Response response = client.newCall(request).execute();
            if(response == null || response.body() == null) {
                return null;
            }
            return JSON.toJavaObject(JSONObject.parseObject(response.body().string()), respType);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
