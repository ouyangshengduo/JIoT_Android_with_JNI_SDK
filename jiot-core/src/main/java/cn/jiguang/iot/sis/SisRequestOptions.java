package cn.jiguang.iot.sis;

import cn.jiguang.iot.util.JiotConstant;

/**
 * @author: ouyangshengduo
 * e-mail: ouysd@jiguang.cn
 * date  : 2019/5/8 9:44
 * desc  :
 */
public class SisRequestOptions {

    private int requestProtocolType;

    private String host;

    private String productKey;

    private boolean shouldRetry;

    private String httpUrl;

    public boolean isShouldRetry() {
        return shouldRetry;
    }

    public void setShouldRetry(boolean shouldRetry) {
        this.shouldRetry = shouldRetry;
    }

    public int getRequestProtocolType() {
        return requestProtocolType;
    }

    public void setRequestProtocolType(int requestProtocolType) {
        this.requestProtocolType = requestProtocolType;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public String getProductKey() {
        return productKey;
    }

    public void setProductKey(String productKey) {
        this.productKey = productKey;
    }

    public String getHttpUrl() {

        if(null == host) {
            host = SisContants.HOST;
        }

        //https://113.31.131.59/v1/addrget?product_key=''&protocol_type=1
        if(requestProtocolType == JiotConstant.SIS_PROTOCOL_TYPE_TCP){
            return "http://" + host + SisContants.URI + "?product_key=" + productKey + "&protocol_type=" + requestProtocolType;
        }else{
            return "https://" + host + SisContants.URI + "?product_key=" + productKey + "&protocol_type=" + requestProtocolType;
        }
    }
}
