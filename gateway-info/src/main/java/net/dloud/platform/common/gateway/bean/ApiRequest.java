package net.dloud.platform.common.gateway.bean;

import lombok.Data;

import java.util.Map;

/**
 * 调用单个方法
 *
 * @author QuDasheng
 * @create 2018-08-30 16:48
 **/
@Data
public class ApiRequest {
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
     * 具体参数，必填，参与签名
     */
    private Map<String, Object> param;

    /**
     * 来源，必填，参与签名
     */
    private String tenant;

    /**
     * 附加信息，非必填，不参与签名
     */
    private Map<String, String> attach;
}
