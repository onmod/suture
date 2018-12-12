package net.dloud.platform.parse.module;

import com.alibaba.dubbo.rpc.RpcContext;
import com.alibaba.dubbo.rpc.RpcException;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import net.dloud.platform.common.extend.StringUtil;
import net.dloud.platform.common.gateway.bean.ApiRequest;
import net.dloud.platform.common.gateway.bean.ApiResponse;
import net.dloud.platform.common.gateway.bean.InvokeRequest;
import net.dloud.platform.extend.constant.PlatformConstants;
import net.dloud.platform.extend.constant.PlatformExceptionEnum;
import net.dloud.platform.extend.exception.InnerException;
import net.dloud.platform.extend.exception.PassedException;
import net.dloud.platform.extend.exception.RefundException;
import net.dloud.platform.extend.tuple.PairTuple;
import net.dloud.platform.extend.tuple.ThirdTuple;
import net.dloud.platform.extend.wrapper.AssertWrapper;
import net.dloud.platform.parse.utils.AopTargetUtil;
import net.dloud.platform.parse.utils.ApiTestUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.ApplicationContext;
import org.springframework.core.LocalVariableTableParameterNameDiscoverer;
import org.springframework.core.ParameterNameDiscoverer;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * @author QuDasheng
 * @create 2018-11-16 10:41
 **/
@Slf4j
@CrossOrigin
@RestController
@RequestMapping("/run")
@ConditionalOnProperty(value = "run.mode", havingValue = "dev")
public class GatewayController {
    private static final ParameterNameDiscoverer discoverer = new LocalVariableTableParameterNameDiscoverer();
    /**
     * 默认服务前缀
     */
    private final String servicePrefix = "_";
    /**
     * 默认服务后缀
     */
    private final String serviceSuffix = "Service";
    /**
     * 实现类后缀
     */
    private final String implSuffix = "Impl";
    @Autowired
    private ApplicationContext context;


    @PostMapping("/api")
    public ApiResponse invoke(@RequestBody InvokeRequest request) {
        AssertWrapper.notNull(request, PlatformExceptionEnum.BAD_REQUEST);
        AssertWrapper.notBlank(request.getTenant(), PlatformExceptionEnum.BAD_REQUEST);
        AssertWrapper.notEmpty(request.getInvoke(), PlatformExceptionEnum.BAD_REQUEST);
        AssertWrapper.notEmpty(request.getParam(), PlatformExceptionEnum.BAD_REQUEST);

        final String proof = UUID.randomUUID().toString();
        final List<Object> result = Lists.newArrayListWithExpectedSize(request.getInvoke().size());
        int i = 0;
        for (String invokeName : request.getInvoke()) {
            final String[] getNames = invokeName.split("\\.");
            if (getNames.length != 3) {
                throw new PassedException("调用方法有误");
            }

            result.add(handle(proof, getNames[1], getNames[2], request.getParam().get(i)));
            i++;
        }

        return buildResult(proof, result);
    }

    @PostMapping("/{system}/{clazz}/{method}")
    public ApiResponse fullPath(@PathVariable String system, @PathVariable String clazz, @PathVariable String method,
                                @RequestBody ApiRequest request) {
        AssertWrapper.notNull(request, PlatformExceptionEnum.BAD_REQUEST);
        AssertWrapper.notBlank(request.getTenant(), PlatformExceptionEnum.BAD_REQUEST);
        AssertWrapper.notEmpty(request.getParam(), PlatformExceptionEnum.BAD_REQUEST);

        final String proof = UUID.randomUUID().toString();
        if (clazz.startsWith(servicePrefix)) {
            clazz = StringUtil.firstLowerCase(system) + StringUtil.firstLowerCase(clazz.substring(1));
        } else {
            clazz = StringUtil.firstLowerCase(clazz);
        }
        if (!clazz.endsWith(serviceSuffix)) {
            clazz += serviceSuffix;
        }
        return buildResult(proof, handle(proof, clazz, method, request.getParam()));
    }

    @PostMapping("/{system}/_{method}")
    public ApiResponse simplePath(@PathVariable String system, @PathVariable String method,
                                  @RequestBody ApiRequest request) {
        AssertWrapper.notNull(request, PlatformExceptionEnum.BAD_REQUEST);
        AssertWrapper.notBlank(request.getTenant(), PlatformExceptionEnum.BAD_REQUEST);
        AssertWrapper.notEmpty(request.getParam(), PlatformExceptionEnum.BAD_REQUEST);

        final String proof = UUID.randomUUID().toString();
        final String clazz = StringUtil.firstLowerCase(system) + serviceSuffix;
        return buildResult(proof, handle(proof, clazz, method, request.getParam()));
    }

    private Object handle(String proof, String clazz, String method, Map<String, Object> param) {
        RpcContext.getContext().setAttachment(PlatformConstants.PROOF_KEY, proof);

        final PairTuple<Object, Object> targetInfo = getTarget(clazz);
        final ThirdTuple<List<String>, Parameter[], MethodType> methodInfo = getMethod(targetInfo.getLast(), method, param.size());

        try {
            //获取方法句柄
            final MethodHandles.Lookup lookup = MethodHandles.lookup();
            final MethodHandle handle = lookup.bind(targetInfo.getFirst(), method, methodInfo.getLast());
            final Object[] objects = ApiTestUtil.sortInputByMethod(param, methodInfo.getFirst(), methodInfo.getMiddle());

            return handle.invokeWithArguments(objects);
        } catch (Throwable ex) {
            if (ex instanceof InnerException) {
                log.warn("[PLATFORM] 系统内部异常: ", ex);
                throw new InnerException(ex.getMessage());
            } else if (ex instanceof PassedException) {
                log.info("[PLATFORM] 业务内部校验不通过: {}", ex.getMessage());
                throw new InnerException(ex.getMessage());
            } else if (ex instanceof RefundException) {
                log.info("[PLATFORM] 调用了未授权的资源: {}", ex.getMessage());
                throw new InnerException(ex.getMessage());
            } else if (ex instanceof RpcException) {
                log.error("[PLATFORM] DUBBO调用异常: ", ex);
                throw new InnerException(PlatformExceptionEnum.CLIENT_TIMEOUT.getMessage());
            } else if (ex instanceof IOException) {
                log.error("[PLATFORM] IO使用异常: ", ex);
                throw new InnerException(PlatformExceptionEnum.BAD_REQUEST.getMessage());
            } else {
                log.warn("[PLATFORM] 调用方法未知异常: ", ex);
                throw new InnerException(ex.getMessage());
            }
        }
    }

    private ApiResponse buildResult(String proof, Object result) {
        final ApiResponse response = new ApiResponse();
        response.setProof(proof);
        response.setPreload(result);
        return response;
    }

    private PairTuple<Object, Object> getTarget(String clazz) {
        try {
            //获取spring中的bean
            if (!clazz.endsWith(implSuffix)) {
                clazz += implSuffix;
            }
            final Object bean = context.getBean(clazz);
            return new PairTuple<>(bean, AopTargetUtil.getTarget(bean));
        } catch (Exception ex) {
            log.info("[PLATFORM] 未找到输入的服务: {}, {}", clazz, ex.getMessage());
            throw new PassedException("未找到输入的服务");
        }
    }

    private ThirdTuple<List<String>, Parameter[], MethodType> getMethod(Object target, String methodName, int inputParamSize) {
        final Class<?> clazz = target.getClass();
        log.info("[PLATFORM] 调用目标: {}, 方法名: {}, 输入参数长度: {}", target.getClass(), methodName, inputParamSize);

        //获取spring中的方法参数名
        for (Method method : clazz.getDeclaredMethods()) {
            if (method.getName().equals(methodName)) {
                final List<String> paramNames = new ArrayList<>();
                final String[] discovererParamNames = discoverer.getParameterNames(method);

                int discovererParamSize = 0;
                if (null != discovererParamNames) {
                    Collections.addAll(paramNames, discovererParamNames);
                    discovererParamSize = discovererParamNames.length;
                }
                log.info("[PLATFORM] 查找到合适的方法: {}, 方法参数长度: {}", method, discovererParamSize);

                final Parameter[] parameters = method.getParameters();
                if (discovererParamSize == inputParamSize) {
                    List<Class<?>> parameterTypeList = Arrays.stream(parameters).map(Parameter::getType)
                            .collect(Collectors.toList());
                    return new ThirdTuple<>(paramNames, parameters, MethodType.methodType(method.getReturnType(), parameterTypeList));
                }
            }
        }

        throw new PassedException("未找到输入的方法");
    }
}
