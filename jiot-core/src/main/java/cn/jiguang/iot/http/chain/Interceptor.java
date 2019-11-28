package cn.jiguang.iot.http.chain;

import java.io.IOException;

import cn.jiguang.iot.http.Response;


/**
 * @author: ouyangshengduo
 * e-mail: ouysd@jiguang.cn
 * date  : 2019/5/7 17:01
 * desc  :
 */

public interface Interceptor {

    Response intercept(InterceptorChain interceptorChain) throws IOException;
}
