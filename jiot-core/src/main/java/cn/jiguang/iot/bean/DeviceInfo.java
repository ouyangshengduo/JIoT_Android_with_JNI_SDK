package cn.jiguang.iot.bean;

/**
 * @author: ouyangshengduo
 * e-mail: ouysd@jiguang.cn
 * date  : 2019/5/7 10:29
 * desc  :
 */
public class DeviceInfo {

    /**
     * 设备名称
     */
    private String deviceName;
    /**
     * 设备密钥
     */
    private String deviceSecret;
    /**
     * 产品key
     */
    private String productKey;

    public DeviceInfo(){}

    public DeviceInfo(String deviceName,String deviceSecret,String productKey){
        this.deviceName = deviceName;
        this.deviceSecret = deviceSecret;
        this.productKey = productKey;
    }

    public String getDeviceName() {
        return deviceName;
    }

    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }

    public String getDeviceSecret() {
        return deviceSecret;
    }

    public void setDeviceSecret(String deviceSecret) {
        this.deviceSecret = deviceSecret;
    }

    public String getProductKey() {
        return productKey;
    }

    public void setProductKey(String productKey) {
        this.productKey = productKey;
    }
}
