package cn.jiguang.iot.sis;

import cn.jiguang.iot.http.Call;
import cn.jiguang.iot.http.Response;

/**
 * @author: ouyangshengduo
 * e-mail: ouysd@jiguang.cn
 * date  : 2019/5/7 17:29
 * desc  :
 */
public abstract class SisHandleCallback {

    public abstract void onFailure(Call call, Throwable throwable);

    public abstract void onResponse(Call call, Response response);
}
