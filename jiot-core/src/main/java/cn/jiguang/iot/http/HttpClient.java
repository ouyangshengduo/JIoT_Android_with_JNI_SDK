package cn.jiguang.iot.http;

import java.util.ArrayList;
import java.util.List;

import cn.jiguang.iot.http.chain.Interceptor;


/**
 * @author: ouyangshengduo
 * e-mail: ouysd@jiguang.cn
 * date  : 2019/5/7 17:01
 * desc  :
 */

public class HttpClient {

    //设置调度器
    private Dispather dispather;

    private List<Interceptor> interceptors;

    private int retryTimes;

    private ConnectionPool connectionPool;

    public int getRetryTimes() {
        return retryTimes;
    }

    public Dispather getDispather() {
        return dispather;
    }

    public List<Interceptor> getInterceptors() {
        return interceptors;
    }

    public ConnectionPool getConnectionPool() {
        return connectionPool;
    }

    /**
     * 构造方法
     */
    public HttpClient(Builder builder){
        this.dispather = builder.dispather;
        this.interceptors = builder.interceptors;
        this.retryTimes = builder.retryTimes;
        this.connectionPool = builder.connectionPool;
    }

    /**
     * 生成一个网络请求Call对象实例
     * @param request
     * @return
     */
    public Call newCall(Request request){
        return new Call(this,request);
    }

    //TODO 建造对象
    public static final class Builder{

        Dispather dispather;
        List<Interceptor> interceptors = new ArrayList<>();
        int retryTimes;
        ConnectionPool connectionPool;

        public Builder addInterceptors(Interceptor interceptor){
            interceptors.add(interceptor);
            return this;
        }

        public Builder setDispather(Dispather dispather){
            this.dispather = dispather;
            return this;
        }

        public Builder setRetryTimes(int retryTimes){
            this.retryTimes = retryTimes;
            return this;
        }

        public Builder setConnectionPool(ConnectionPool connectionPool){
            this.connectionPool = connectionPool;
            return this;
        }


        public HttpClient build(){

            if(null == dispather){
                dispather = new Dispather();
            }

            if(null == connectionPool){
                connectionPool = new ConnectionPool();
            }
            return new HttpClient(this);
        }

    }
}
