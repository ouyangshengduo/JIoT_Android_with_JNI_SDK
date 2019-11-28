package cn.jiguang.iot.bean;

/**
 * @author: ouyangshengduo
 * e-mail: ouysd@jiguang.cn
 * date  : 2019/5/7 10:29
 * desc  :
 */
public class PropertyReportRsp {

    private int code;
    private long verion;
    private long seqNo;
    private Property[] properties;

    public Property[] getProperties() {
        return properties;
    }

    public void setProperties(Property[] properties) {
        this.properties = properties;
    }

    public long getSeqNo() {
        return seqNo;
    }

    public void setSeqNo(long seqNo) {
        this.seqNo = seqNo;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public long getVerion() {
        return verion;
    }

    public void setVerion(long verion) {
        this.verion = verion;
    }
}
