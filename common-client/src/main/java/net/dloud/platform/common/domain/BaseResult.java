package net.dloud.platform.common.domain;

import net.dloud.platform.common.annotation.Transient;

/**
 * @author QuDasheng
 * @create 2018-09-02 11:19
 **/
public class BaseResult {
    /**
     * 消息代码
     */
    @Transient
    private String code;


    public BaseResult() {
        this.code = "0";
    }

    public BaseResult(String code) {
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

    @Override
    public String toString() {
        return "BaseResult{" +
                "code='" + code + '\'' +
                '}';
    }
}
