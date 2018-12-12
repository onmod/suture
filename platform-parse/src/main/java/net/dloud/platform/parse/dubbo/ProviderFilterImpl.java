package net.dloud.platform.parse.dubbo;

import com.alibaba.dubbo.common.Constants;
import com.alibaba.dubbo.common.extension.Activate;
import com.alibaba.dubbo.common.utils.StringUtils;
import com.alibaba.dubbo.rpc.Filter;
import com.alibaba.dubbo.rpc.Invocation;
import com.alibaba.dubbo.rpc.Invoker;
import com.alibaba.dubbo.rpc.Result;
import com.alibaba.dubbo.rpc.RpcException;
import com.alibaba.dubbo.rpc.RpcResult;
import lombok.extern.slf4j.Slf4j;
import net.dloud.platform.common.domain.result.ExceptionResult;
import net.dloud.platform.extend.constant.PlatformConstants;
import net.dloud.platform.extend.constant.PlatformExceptionEnum;
import net.dloud.platform.extend.exception.InnerException;
import net.dloud.platform.extend.exception.PassedException;
import net.dloud.platform.extend.exception.RefundException;

/**
 * @author QuDasheng
 * @create 2018-09-02 13:08
 **/
@Slf4j
@Activate(group = Constants.PROVIDER, value = "providerFilter", order = 100)
public class ProviderFilterImpl implements Filter {
    @Override
    public Result invoke(Invoker<?> invoker, Invocation invocation) throws RpcException {
        log.info("[{}] 提供[DUBBO]服务: 方法名: {}, 附加参数: {}, 当前分组: {}", PlatformConstants.APPNAME,
                invocation.getMethodName(), invocation.getAttachments(), PlatformConstants.GROUP);
        try {
            final Result invoke = invoker.invoke(invocation);
            if (invoke.hasException()) {
                throw invoke.getException();
            }
            return invoke;
        } catch (PassedException ex) {
            log.warn("[DUBBO] 业务内部校验不通过: {}", ex.getMessage());
            return new RpcResult(ex.toResult());
        } catch (RefundException ex) {
            log.warn("[DUBBO] 调用了未授权的资源: {}", ex.getMessage());
            return new RpcResult(ex.toResult());
        } catch (InnerException ex) {
            log.warn("[DUBBO] 内部调用异常: ", ex.getException());
            return new RpcResult(ex.toResult());
        } catch (Throwable ex) {
            log.error("[DUBBO] 调用未知异常: ", ex);
            return new RpcResult(new ExceptionResult(PlatformConstants.EXCEPTION_CODE_UNKNOWN,
                    PlatformExceptionEnum.SYSTEM_BUSY.getCode(), ex.getMessage(), StringUtils.toString(ex)));
        }
    }
}
