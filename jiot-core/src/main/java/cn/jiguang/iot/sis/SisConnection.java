package cn.jiguang.iot.sis;

import cn.jiguang.iot.http.Call;
import cn.jiguang.iot.http.Callback;
import cn.jiguang.iot.http.HttpClient;
import cn.jiguang.iot.http.Request;
import cn.jiguang.iot.http.Response;

/**
 * @author: ouyangshengduo
 * e-mail: ouysd@jiguang.cn
 * date  : 2019/5/7 17:25
 * desc  : sis服务连接
 */
public class SisConnection {

    private HttpClient clientNoRetry;
    private HttpClient clientHasRetry;
    private SisHandleCallback sisHandleCallback;
    private String productKey;
    private int sisRequestProtocolType;
    public SisConnection(SisHandleCallback sisHandleCallback){
        this.sisHandleCallback = sisHandleCallback;

        clientHasRetry = new HttpClient.Builder().setRetryTimes(2).build();
        clientNoRetry = new HttpClient.Builder().setRetryTimes(1).build();
    }

    public String getProductKey() {
        return productKey;
    }

    public int getSisRequestProtocolType() {
        return sisRequestProtocolType;
    }

    public void request(SisRequestOptions options){
        this.productKey = options.getProductKey();
        this.sisRequestProtocolType = options.getRequestProtocolType();
        Request request = new Request.Builder()
                .setHttpUrl(options.getHttpUrl())
                .build();
        Call call = null;
        if(options.isShouldRetry()) {
            call = clientHasRetry.newCall(request);
        }else{
            call = clientNoRetry.newCall(request);
        }
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, Throwable throwable) {

                if(null != sisHandleCallback) {
                    sisHandleCallback.onFailure(call, throwable);
                }
            }

            @Override
            public void onResponse(Call call, Response response) {

                if(null != sisHandleCallback){
                    sisHandleCallback.onResponse(call,response);
                }
            }
        });
    }

}
