package cn.jiguang.iot.http.chain;

import android.util.Log;

import java.io.IOException;
import java.util.Map;

import cn.jiguang.iot.http.HttpCodec;
import cn.jiguang.iot.http.Request;
import cn.jiguang.iot.http.Response;
import cn.jiguang.iot.util.JiotLogger;

/**
 * @author: ouyangshengduo
 * e-mail: ouysd@jiguang.cn
 * date  : 2019/5/7 17:01
 * desc  :
 */

public class HeadersInterceptor implements Interceptor {
    @Override
    public Response intercept(InterceptorChain interceptorChain) throws IOException {
        Request request = interceptorChain.call.getRequest();
        Map<String,String> headers = request.getHeaders();
        if(!headers.containsKey(HttpCodec.HEAD_HOST)){
            headers.put(HttpCodec.HEAD_HOST,request.getHttpUrl().getHost());
        }
        if(!headers.containsKey(HttpCodec.HEAD_CONNECTION)) {
            headers.put(HttpCodec.HEAD_CONNECTION, HttpCodec.HEAD_VALUE_KEEP_ALIVE);
        }

        if(null != request.getRequestBody()){
            String contentType = request.getRequestBody().getContentType();
            if(null != contentType){
                headers.put(HttpCodec.HEAD_CONTENT_TYPE,contentType);
            }

            long contentLength = request.getRequestBody().getContentLength();

            if(-1 != contentLength){
                headers.put(HttpCodec.HEAD_CONTENT_LENGTH,Long.toString(contentLength));
            }
        }
        return interceptorChain.proceed();
    }
}
