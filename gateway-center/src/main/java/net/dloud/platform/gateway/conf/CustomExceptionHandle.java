package net.dloud.platform.gateway.conf;

import lombok.extern.slf4j.Slf4j;
import net.dloud.platform.common.gateway.bean.ApiResponse;
import net.dloud.platform.gateway.util.ExceptionUtil;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import reactor.core.publisher.Mono;

/**
 * @author QuDasheng
 * @create 2018-09-21 22:20
 **/
@Slf4j
@RestControllerAdvice
@ConditionalOnClass(org.springframework.web.reactive.DispatcherHandler.class)
public class CustomExceptionHandle {
    /**
     * 异常处理
     */
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.OK)
    public Mono<ApiResponse> serverExceptionHandler(Exception ex) {
        return ExceptionUtil.handleOne(ex);
    }
}
