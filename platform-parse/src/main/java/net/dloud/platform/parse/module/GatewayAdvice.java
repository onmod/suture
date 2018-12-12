package net.dloud.platform.parse.module;

import net.dloud.platform.common.gateway.bean.ApiResponse;
import net.dloud.platform.extend.exception.InnerException;
import net.dloud.platform.extend.exception.PassedException;
import net.dloud.platform.extend.exception.RefundException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.UUID;

/**
 * @author QuDasheng
 * @create 2018-11-16 13:57
 **/
@RestControllerAdvice(value = "net.dloud.platform.parse")
public class GatewayAdvice {

    @ExceptionHandler(value = PassedException.class)
    public ApiResponse passedException(PassedException ex) {
        if (null == ex.getEnum()) {
            return proof(new ApiResponse(ex));
        } else {
            return proof(new ApiResponse(ex.getEnum()));
        }
    }

    @ExceptionHandler(value = InnerException.class)
    public ApiResponse innerException(InnerException ex) {
        return proof(new ApiResponse(ex));
    }

    @ExceptionHandler(value = RefundException.class)
    public ApiResponse refundException(RefundException ex) {
        if (null == ex.getEnum()) {
            return proof(new ApiResponse(ex));
        } else {
            return proof(new ApiResponse(-1, ex.getEnum()));
        }
    }

    @ExceptionHandler(value = Exception.class)
    public ApiResponse exception(Exception ex) {
        return proof(new ApiResponse(ex));
    }

    private ApiResponse proof(ApiResponse response) {
        if (null == response.getProof()) {
            response.setProof(UUID.randomUUID().toString());
        }
        return response;
    }
}
