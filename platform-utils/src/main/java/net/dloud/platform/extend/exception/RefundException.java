package net.dloud.platform.extend.exception;

import net.dloud.platform.common.domain.result.ExceptionResult;
import net.dloud.platform.common.platform.BaseExceptionEnum;
import net.dloud.platform.extend.constant.PlatformConstants;

/**
 * 拒绝连接，无权限连接
 *
 * @author dor
 */
public class RefundException extends RuntimeException {
    private static final long serialVersionUID = -1639913344221635202L;

    private String code;

    private BaseExceptionEnum cons;

    public RefundException(String code, String message) {
        super(message);
        this.code = code;
    }

    public RefundException(BaseExceptionEnum cons) {
        super(cons.getMessage());
        this.code = cons.getCode();
        this.cons = cons;
    }

    public String getCode() {
        return code;
    }

    public BaseExceptionEnum getEnum() {
        return cons;
    }

    public ExceptionResult toResult() {
        return new ExceptionResult(PlatformConstants.EXCEPTION_CODE_REFUND, getCode(), getMessage());
    }

    @Override
    public String toString() {
        return "RefundException{" +
                "code='" + code + '\'' +
                ", cons=" + cons +
                '}';
    }
}
