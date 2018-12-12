package net.dloud.platform.common.gateway.bean;

/**
 * @author QuDasheng
 * @create 2018-09-02 11:19
 **/
public class SimpleResult {
    /**
     * 消息代码
     */
    private String code;


    public SimpleResult() {
        this.code = "0";
    }

    public SimpleResult(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public boolean isSuccess() {
        return "0".equals(code);
    }
}
