package net.dloud.platform.extend.constant;

/**
 * @author QuDasheng
 * @create 2018-10-02 09:09
 **/
public enum RequestHeaderEnum {
    /**
     * 请求来源，用来代理层级ip，可能出现逗号
     */
    X_FORWARDED_FOR("X-Forwarded-For"),

    /**
     * 请求来源，用来真实ip
     */
    X_REAL_IP("X-Real-IP");


    private String value;

    RequestHeaderEnum(String value) {
        this.value = value;
    }

    public String value() {
        return value;
    }
}
