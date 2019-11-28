package cn.jiguang.iot.http.chain;

import android.util.Log;

import java.io.IOException;

import cn.jiguang.iot.http.Call;
import cn.jiguang.iot.http.Response;
import cn.jiguang.iot.util.JiotLogger;

/**
 * @author: ouyangshengduo
 * e-mail: ouysd@jiguang.cn
 * date  : 2019/5/7 17:01
 * desc  :
 */

public class RetryInterceptor implements Interceptor {
    @Override
    public Response intercept(InterceptorChain interceptorChain) throws IOException {

        Call call = interceptorChain.call;
        IOException ioException = null;

        for(int i = 0 ; i < call.getHttpClient().getRetryTimes(); i ++){

            if(call.isCanceled()){
                throw new IOException("this task had canceled");
            }

            try {
                Response response = interceptorChain.proceed();
                return response;
            }catch (IOException e){
                ioException = e;
            }
        }
        throw ioException;
    }
}
