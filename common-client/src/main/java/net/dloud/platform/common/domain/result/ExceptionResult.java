package net.dloud.platform.common.domain.result;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import net.dloud.platform.common.domain.BaseResult;
import net.dloud.platform.common.platform.BaseExceptionEnum;

/**
 * @author QuDasheng
 * @create 2018-09-27 17:34
 **/
@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class ExceptionResult extends BaseResult {
    private boolean failed = true;

    private int type;

    private String message;

    private String exception;

    public ExceptionResult(BaseExceptionEnum exception) {
        super(exception.getCode());
        this.type = 1;
        this.message = exception.getMessage();
    }

    public ExceptionResult(String code, String message) {
        super(code);
        this.type = 1;
        this.message = message;
    }

    public ExceptionResult(int type, String code, String message) {
        super(code);
        this.type = type;
        this.message = message;
    }

    public ExceptionResult(int type, String code, String message, String exception) {
        super(code);
        this.type = type;
        this.message = message;
        this.exception = exception;
    }
}
