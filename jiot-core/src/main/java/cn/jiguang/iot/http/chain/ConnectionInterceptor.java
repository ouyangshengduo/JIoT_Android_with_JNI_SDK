package cn.jiguang.iot.http.chain;

import android.util.Log;

import java.io.IOException;

import cn.jiguang.iot.http.HttpClient;
import cn.jiguang.iot.http.HttpConnection;
import cn.jiguang.iot.http.HttpUrl;
import cn.jiguang.iot.http.Request;
import cn.jiguang.iot.http.Response;
import cn.jiguang.iot.util.JiotLogger;

/**
 * @author: ouyangshengduo
 * e-mail: ouysd@jiguang.cn
 * date  : 2019/5/7 17:01
 * desc  :
 */

public class ConnectionInterceptor implements Interceptor {
    @Override
    public Response intercept(InterceptorChain interceptorChain) throws IOException {

        Request request = interceptorChain.call.getRequest();
        HttpClient httpClient = interceptorChain.call.getHttpClient();
        HttpUrl httpUrl = request.getHttpUrl();

        HttpConnection httpConnection = httpClient.getConnectionPool().getHttpConnection(httpUrl.getHost(),httpUrl.getPort());
        if(null == httpConnection){
            httpConnection = new HttpConnection();
        }
        httpConnection.setRequest(request);

        try {
            Response response = interceptorChain.proceed(httpConnection);
            if (response.isKeepAlive()){
                httpClient.getConnectionPool().putHttpConnection(httpConnection);
            }else{
                httpConnection.close();
            }
            return response;
        }catch (IOException e){
            httpConnection.close();
            throw e;
        }
    }
}
