package net.dloud.platform.common.gateway.bean;

import lombok.Data;
import net.dloud.platform.common.platform.BaseExceptionEnum;

/**
 * @author QuDasheng
 * @create 2018-08-30 16:48
 **/
@Data
public class ApiResponse {
    /**
     * 返回码
     * <p>
     * -1	用户授权过期
     * <p>
     * 0	正常返回值
     * <p>
     * 1	业务错误
     */
    private int code;

    /**
     * 消息
     */
    private String message;

    /**
     * 凭证
     */
    private String proof;

    /**
     * 具体结果
     */
    private Object preload;


    public ApiResponse() {
        this.code = 0;
        this.message = "请求成功...";
    }

    public ApiResponse(boolean success) {
        if (success) {
            this.code = 0;
            this.message = "请求成功...";
        } else {
            this.code = 1;
            this.message = "请求失败, 请稍后重试...";
            this.preload = new SimpleResult("101");
        }
    }

    public ApiResponse(BaseExceptionEnum base) {
        this(1, base);
    }

    public ApiResponse(int code, BaseExceptionEnum base) {
        this.code = code;
        this.message = base.getMessage();
        this.preload = new SimpleResult(base.getCode());
    }

    public ApiResponse(Exception ex) {
        this.code = 1;
        this.message = ex.getMessage();
        this.preload = new SimpleResult("101");
    }

    public ApiResponse(int code, Exception ex) {
        this.code = code;
        this.message = ex.getMessage();
        this.preload = new SimpleResult("101");
    }
}
