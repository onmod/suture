package net.dloud.platform.extend.constant;

import net.dloud.platform.common.platform.BaseExceptionEnum;

/**
 * @author QuDasheng
 * @create 2018-09-22 14:07
 **/
public enum PlatformExceptionEnum implements BaseExceptionEnum {
    /**
     * 错误码 后三位是业务系统自定义 前面是appid
     */
    SYSTEM_BUSY("101", "服务器忙, 请稍后重试..."),
    SYSTEM_ERROR("102", "系统异常, 请联系管理员..."),
    RESULT_ERROR("104", "结果异常, 请联系管理员..."),
    API_ACCESS_LIMIT("109", "您访问过于快，请稍后重试!"),
    API_ACCESS_ERROR("199", "当前接口不允许被外部调用!"),
    CLIENT_ERROR("300", "连接异常, 请稍后重试"),
    CLIENT_TIMEOUT("301", "连接超时, 请稍后重试"),
    RPC_CONSUMER_ERROR("320", "RPC消费异常"),
    RPC_PROVIDER_ERROR("321", "RPC提供异常"),
    MQ_CONSUMER_ERROR("330", "RPC消费异常"),
    MQ_PROVIDER_ERROR("331", "RPC提供异常"),
    BAD_REQUEST("400", "输入错误, 请检查"),
    UNAUTHORIZED("401", "授权失败, 请重试"),
    FORBIDDEN("403", "当前资源禁止访问"),
    NOT_FOUND("404", "当前资源未找到"),
    SOURCE_INIT_FAILED("501", "资源初始化失败"),
    SOURCE_NOT_EXIST("504", "未找资源, 无法注入"),
    SAVE_ERROR("555", "保存失败, 请稍后重试..."),
    SERIALIZE_ERROR("700", "序列化异常"),
    LOGIN_NONE("790", "您未登录, 请先登录"),
    LOGIN_ERROR("791", "您的登录信息异常, 请先重新登录"),
    LOGIN_EXPIRE("792", "您的登录信息已过期, 请重新登录"),
    GENERATION_SEQUENCE("901", "生成序列号中，请稍后"),
    OPERATION_NOT_SUPPORTED("998", "当前操作不被支持"),
    SIGN_ERROR("999", "签名异常, 请核对"),
    ;

    private String code;

    private String message;

    PlatformExceptionEnum(String code, String message) {
        this.code = code;
        this.message = message;
    }
    
    @Override
    public String getCode() {
        return code;
    }

    @Override
    public String getMessage() {
        return message;
    }
}
