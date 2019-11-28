package cn.jiguang.iot.http.chain;

import java.io.IOException;
import java.util.List;

import cn.jiguang.iot.http.Call;
import cn.jiguang.iot.http.HttpConnection;
import cn.jiguang.iot.http.Response;
import cn.jiguang.iot.util.JiotLogger;


/**
 * @author: ouyangshengduo
 * e-mail: ouysd@jiguang.cn
 * date  : 2019/5/7 17:01
 * desc  :
 */

public class InterceptorChain {


    final List<Interceptor> interceptors;
    final int index;
    final Call call;
    HttpConnection httpConnection;


    public InterceptorChain(List<Interceptor> interceptors, int index, Call call,HttpConnection httpConnection) {
        this.interceptors = interceptors;
        this.index = index;
        this.call = call;
        this.httpConnection = httpConnection;
    }

    public Response proceed(HttpConnection httpConnection) throws IOException{
        this.httpConnection = httpConnection;
        return proceed();
    }

    public Response proceed() throws IOException{
        if(index > interceptors.size()){
            throw new IOException("Interceptor Chain Error");
        }
        Interceptor interceptor = interceptors.get(index);
        InterceptorChain next = new InterceptorChain(interceptors,index + 1,call, httpConnection);
        Response response = interceptor.intercept(next);
        return response;
    }


}
