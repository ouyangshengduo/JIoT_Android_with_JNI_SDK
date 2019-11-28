package cn.jiguang.iot.http;

import java.util.HashMap;
import java.util.Map;

/**
 * @author: ouyangshengduo
 * e-mail: ouysd@jiguang.cn
 * date  : 2019/5/7 17:01
 * desc  :
 */

public class Response {

    int code;//状态码
    int contentLength = -1;//返回包的长度
    Map<String,String> headers = new HashMap<>();//返回包的头信息
    String body;//包的内容
    boolean isKeepAlive;//是否保持连接

    public Response(){
    }

    public Response(int code, int contentLength, Map<String, String> headers, String body, boolean isKeepAlive) {
        this.code = code;
        this.contentLength = contentLength;
        this.headers = headers;
        this.body = body;
        this.isKeepAlive = isKeepAlive;
    }

    public int getCode() {
        return code;
    }

    public int getContentLength() {
        return contentLength;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public String getBody() {
        return body;
    }

    public boolean isKeepAlive() {
        return isKeepAlive;
    }


}
