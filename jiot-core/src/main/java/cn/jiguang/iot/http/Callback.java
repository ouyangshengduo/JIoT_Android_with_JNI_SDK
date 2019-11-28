package cn.jiguang.iot.http;

/**
 * @author: ouyangshengduo
 * e-mail: ouysd@jiguang.cn
 * date  : 2019/5/7 17:01
 * desc  :
 */

public interface Callback {

    void onFailure(Call call, Throwable throwable);

    void onResponse(Call call, Response response);
}
