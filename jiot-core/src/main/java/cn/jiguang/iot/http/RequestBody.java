package cn.jiguang.iot.http;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

/**
 * @author: ouyangshengduo
 * e-mail: ouysd@jiguang.cn
 * date  : 2019/5/7 17:01
 * desc  :
 */

public class RequestBody {

    /**
     * 表单提交 使用urlencoded编码,这里也可以使用json方式
     */
    private static final String CONTENT_TYPE = "application/x-www-form-urlencoded";

    private static final String CHARSET = "UTF-8";

    Map<String,String> encodeBodys = new HashMap<>();

    public String getContentType() {
        return CONTENT_TYPE;
    }

    public int getContentLength(){
        return getBody().getBytes().length;
    }

    public String getBody(){

        StringBuffer sb = new StringBuffer();
        for(Map.Entry<String,String> entry : encodeBodys.entrySet()){
            sb.append(entry.getKey())
                    .append("=")
                    .append(entry.getValue())
                    .append("&");
        }
        if(sb.length() != 0){
            sb.deleteCharAt(sb.length() - 1);
        }
        return sb.toString();
    }

    //通过JDK的url编码
    public RequestBody add(String key,String value){
        try {
            encodeBodys.put(URLEncoder.encode(key,CHARSET),URLEncoder.encode(value,CHARSET));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return this;
    }
}
