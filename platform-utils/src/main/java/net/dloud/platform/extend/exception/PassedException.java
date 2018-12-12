package net.dloud.platform.extend.exception;

import net.dloud.platform.common.domain.result.ExceptionResult;
import net.dloud.platform.common.platform.BaseExceptionEnum;
import net.dloud.platform.extend.constant.PlatformConstants;
import net.dloud.platform.extend.constant.PlatformExceptionEnum;

/**
 * 识别信息的异常
 *
 * @author dor
 */
public class PassedException extends RuntimeException {
    private static final long serialVersionUID = -8858133460129491933L;

    private String code;

    private BaseExceptionEnum cons;

    public PassedException(String message) {
        super(message);
        this.code = PlatformExceptionEnum.SYSTEM_BUSY.getCode();
    }

    public PassedException(String code, String message) {
        super(message);
        this.code = code;
    }

    public PassedException(BaseExceptionEnum cons) {
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
        return new ExceptionResult(PlatformConstants.EXCEPTION_CODE_PASSED, getCode(), getMessage());
    }

    @Override
    public String toString() {
        return "PassedException{" +
                "code='" + code + '\'' +
                ", cons=" + cons +
                '}';
    }
}
