package net.dloud.platform.common.gateway.bean;

import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * 调用多个方法
 *
 * @author QuDasheng
 * @create 2018-08-30 16:48
 **/
@Data
public class InvokeRequest {
    /**
     * 用户token，必填，参与签名
     */
    private String token;

    /**
     * 当前时间戳，非必填，不参与签名
     */
    private Long timestamp;

    /**
     * 签名值，必填，参与签名
     */
    private String sign;

    /**
     * 方法的调用名
     */
    private List<String> invoke;

    /**
     * 具体参数，必填，参与签名
     */
    private List<Map<String, Object>> param;

    /**
     * 来源，必填，参与签名
     */
    private String tenant;
}
