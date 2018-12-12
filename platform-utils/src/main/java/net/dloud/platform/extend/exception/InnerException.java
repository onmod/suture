package net.dloud.platform.extend.exception;

import com.alibaba.dubbo.common.utils.StringUtils;
import net.dloud.platform.common.domain.result.ExceptionResult;
import net.dloud.platform.extend.constant.PlatformConstants;
import net.dloud.platform.extend.constant.PlatformExceptionEnum;

/**
 * 内部错误
 *
 * @author dor
 */
public class InnerException extends RuntimeException {
    private static final long serialVersionUID = 5183796390707935808L;

    private String exception;

    public InnerException(String message) {
        super(message);
    }

    public InnerException(String message, String exception) {
        super(message);
        this.exception = exception;
    }

    public InnerException(String message, Throwable exception) {
        super(message);
        this.exception = StringUtils.toString(exception);
    }

    public String getException() {
        return exception;
    }

    public void setException(Exception exception) {
        this.exception = StringUtils.toString(exception);
    }

    public ExceptionResult toResult() {
        return new ExceptionResult(PlatformConstants.EXCEPTION_CODE_INNER, PlatformExceptionEnum.SYSTEM_ERROR.getCode(), getMessage(), getException());
    }

    @Override
    public String toString() {
        return "InnerException{" + exception + '}';
    }
}
