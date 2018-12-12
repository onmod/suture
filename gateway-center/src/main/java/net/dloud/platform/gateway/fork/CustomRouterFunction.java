package net.dloud.platform.gateway.fork;

import com.alibaba.dubbo.config.ApplicationConfig;
import com.alibaba.dubbo.config.ConsumerConfig;
import com.alibaba.dubbo.config.ReferenceConfig;
import com.alibaba.dubbo.config.RegistryConfig;
import com.alibaba.dubbo.rpc.RpcContext;
import com.alibaba.dubbo.rpc.RpcException;
import com.alibaba.dubbo.rpc.service.GenericException;
import com.alibaba.dubbo.rpc.service.GenericService;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import lombok.extern.slf4j.Slf4j;
import net.dloud.platform.common.extend.CollectionUtil;
import net.dloud.platform.common.extend.NumberUtil;
import net.dloud.platform.common.extend.StringUtil;
import net.dloud.platform.common.gateway.InjectEnum;
import net.dloud.platform.common.gateway.bean.ApiRequest;
import net.dloud.platform.common.gateway.bean.ApiResponse;
import net.dloud.platform.common.gateway.bean.InvokeKey;
import net.dloud.platform.common.gateway.bean.InvokeRequest;
import net.dloud.platform.common.gateway.bean.TokenKey;
import net.dloud.platform.common.gateway.info.InjectionInfo;
import net.dloud.platform.common.serialize.KryoBaseUtil;
import net.dloud.platform.dal.InfoComponent;
import net.dloud.platform.dal.entity.InfoMethodGateway;
import net.dloud.platform.extend.bucket.Bandwidth;
import net.dloud.platform.extend.bucket.Bucket;
import net.dloud.platform.extend.constant.PlatformConstants;
import net.dloud.platform.extend.constant.PlatformExceptionEnum;
import net.dloud.platform.extend.exception.InnerException;
import net.dloud.platform.extend.exception.PassedException;
import net.dloud.platform.extend.exception.RefundException;
import net.dloud.platform.extend.wrapper.AssertWrapper;
import net.dloud.platform.gateway.bean.InvokeCache;
import net.dloud.platform.gateway.bean.InvokeDetailCache;
import net.dloud.platform.gateway.util.ExceptionUtil;
import net.dloud.platform.gateway.util.LimitUtil;
import net.dloud.platform.gateway.util.ResultWrapper;
import net.dloud.platform.parse.module.AssistComponent;
import org.jdbi.v3.core.Jdbi;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.BoundValueOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpCookie;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static net.dloud.platform.parse.dubbo.wrapper.DubboWrapper.dubboProvider;
import static org.springframework.web.reactive.function.server.RequestPredicates.OPTIONS;
import static org.springframework.web.reactive.function.server.RequestPredicates.POST;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;

/**
 * @author QuDasheng
 * @create 2018-09-22 15:55
 **/
@Slf4j
@Configuration
public class CustomRouterFunction {
    /**
     * 默认服务前缀
     */
    private final String servicePrefix = "_";
    /**
     * 默认服务后缀
     */
    private final String serviceSuffix = "Service";

    /**
     * 缓存配置
     */
    @Value("${cache.expire-after-write:30}")
    private int expireAfterWrite;
    @Value("${cache.refresh-after-write:10}")
    private int refreshAfterWrite;
    @Value("${default.field-filter}")
    private String fieldFilter;

    /**
     * dubbo配置
     */
    @Autowired
    private ApplicationConfig applicationConfig;
    @Autowired
    private RegistryConfig registryConfig;
    @Autowired
    private ConsumerConfig consumerConfig;

    @Autowired
    private Jdbi jdbi;

    @Autowired
    private InfoComponent infoComponent;

    @Autowired
    private AssistComponent assistComponent;

    /**
     * 缓存服务和查询结果
     */
    private static Cache<InvokeKey, InvokeDetailCache> genericCache;
    /**
     * 用户信息相关
     */
    private static Cache<TokenKey, Map<String, Object>> tokenCache;
    /**
     * 限流相关
     */
    private static Cache<Long, List<Bandwidth>> bucketCache;


    @PostConstruct
    private void init() {
        if (null == genericCache) {
            genericCache = Caffeine.newBuilder().maximumSize(1_000)
                    .expireAfterWrite(expireAfterWrite, TimeUnit.MINUTES)
                    .refreshAfterWrite(refreshAfterWrite, TimeUnit.MINUTES)
                    .build(this::invokeCache);
        }
        if (null == tokenCache) {
            tokenCache = Caffeine.newBuilder().maximumSize(100_000)
                    .expireAfterWrite(expireAfterWrite, TimeUnit.MINUTES)
                    .refreshAfterWrite(refreshAfterWrite, TimeUnit.MINUTES)
                    .build(this::tokenCache);
        }
        if (null == bucketCache) {
            bucketCache = Caffeine.newBuilder().maximumSize(100_000)
                    .expireAfterWrite(expireAfterWrite, TimeUnit.MINUTES)
                    .build();
        }
        assistComponent.listener("invokeKey", (channel, msg) -> genericCache.invalidate(msg));
    }

    @Bean
    public RouterFunction<ServerResponse> optionsAll() {
        return route(OPTIONS("/**"), request -> ServerResponse.ok().build());
    }

    /**
     * 匹配路径
     */
    @Bean
    public RouterFunction<ServerResponse> apiPath() {
        return route(POST("/{system}/{clazz}/{method}")
                .or(POST("/{system}/_{method}")), request -> ServerResponse.ok().body(
                request.bodyToMono(ApiRequest.class).flatMap(input -> {
                    final String inputGroup = request.queryParam(PlatformConstants.GROUP_KEY).orElse(PlatformConstants.DEFAULT_GROUP);
                    final Map<String, String> pathVariables = request.pathVariables();
                    String system = pathVariables.get("system");
                    String clazz = pathVariables.get("clazz");
                    String method = pathVariables.get("method");
                    log.info("[GATEWAY] 分组: {} | 路径参数: {}, {}, {}", inputGroup, system, clazz, method);
                    AssertWrapper.isTrue(StringUtil.notBlank(system) && StringUtil.notBlank(method), "调用方法输入错误!");

                    if (StringUtil.isBlank(clazz)) {
                        clazz = StringUtil.firstLowerCase(system) + serviceSuffix;
                    } else {
                        if (clazz.startsWith(servicePrefix)) {
                            clazz = StringUtil.firstLowerCase(system) + StringUtil.firstUpperCase(clazz.substring(1));
                        } else {
                            clazz = StringUtil.firstLowerCase(clazz);
                        }
                        if (!clazz.endsWith(serviceSuffix)) {
                            clazz += serviceSuffix;
                        }
                    }

                    final String invokeName = system + "." + clazz + "." + method;
                    return Mono.just(doApi(request, input.getToken(), input.getTenant(), inputGroup, invokeName, input.getParam()));
                }).onErrorResume(ExceptionUtil::handleOne), ApiResponse.class));
    }

    /**
     * 匹配方法名
     */
    @Bean
    public RouterFunction<ServerResponse> apiName() {
        return route(POST("/api"), request -> ServerResponse.ok().body(
                request.bodyToMono(InvokeRequest.class).flatMap(input -> {
                    final String inputGroup = request.queryParam(PlatformConstants.GROUP_KEY).orElse(PlatformConstants.DEFAULT_GROUP);
                    final List<String> invokeNames = input.getInvoke();
                    final List<Map<String, Object>> inputParams = input.getParam();

                    AssertWrapper.notNull(invokeNames, "调用方法名不能为空");
                    AssertWrapper.notNull(inputParams, "调用方法参数不能为空");
                    AssertWrapper.isTrue(invokeNames.size() > 0, "调用方法名不能为空");
                    AssertWrapper.isTrue(inputParams.size() > 0, "调用方法名不能为空");
                    AssertWrapper.isTrue(invokeNames.size() == inputParams.size(), "调用类型不匹配");
                    return Mono.just(doApi(request, input.getToken(), input.getTenant(), inputGroup, invokeNames, inputParams));
                }).onErrorResume(ExceptionUtil::handleList), ApiResponse.class));
    }

    /**
     * 调用单个方法
     */
    private ApiResponse doApi(ServerRequest request, String token, String inputTenant, String inputGroup,
                              String invokeName, Map<String, Object> inputParam) {
        return doApi(request, token, inputTenant, inputGroup, Collections.singletonList(invokeName), Collections.singletonList(inputParam), true);
    }

    /**
     * 调用多个方法
     */
    private ApiResponse doApi(ServerRequest request, String token, String inputTenant, String inputGroup,
                              List<String> invokeNames, List<Map<String, Object>> inputParams) {
        return doApi(request, token, inputTenant, inputGroup, invokeNames, inputParams, false);
    }

    private void setRpcContext(String inputTenant, String inputGroup, String proof) {
        final RpcContext context = RpcContext.getContext();
        context.setAttachment(PlatformConstants.PROOF_KEY, proof);
        context.setAttachment(PlatformConstants.SUBGROUP_KEY, inputGroup);
        context.setAttachment(PlatformConstants.FROM_KEY, inputTenant);
        if (!PlatformConstants.DEFAULT_GROUP.equals(inputGroup) && CollectionUtil.notEmpty(dubboProvider.get(inputGroup))) {
            context.setAttachment(PlatformConstants.HANDGROUP_KEY, KryoBaseUtil.writeObjectToString(dubboProvider.get(inputGroup)));
        }
    }

    /**
     * 泛化调用方法
     */
    @SuppressWarnings("unchecked")
    private ApiResponse doApi(ServerRequest request, String token, String inputTenant, String inputGroup,
                              List<String> invokeNames, List<Map<String, Object>> inputParams, boolean fromPath) {
        final String proof = UUID.randomUUID().toString();
        ApiResponse response = new ApiResponse(true);
        log.info("[GATEWAY] 来源: {} | 分组: {} | 调用方法: {} | 输入参数: {} | 凭证: {}",
                inputTenant, inputGroup, invokeNames, inputParams, proof);
        AssertWrapper.isTrue(null != inputTenant && null != inputGroup, "调用方法输入错误!");
        setRpcContext(inputTenant, inputGroup, proof);

        int i = -1;
        int size = invokeNames.size();
        List<Object> results = Lists.newArrayListWithExpectedSize(size);
        try {
            InvokeCache paramCache = doCache(inputGroup, inputParams, invokeNames);
            //校验白名单及用户信息
            Map<String, Object> memberInfo = tokenCache(new TokenKey(token, inputTenant, inputGroup, paramCache.getInvokeName()));
            if (!paramCache.isWhitelist()) {
                if (StringUtil.isBlank(token)) {
                    throw new RefundException(PlatformExceptionEnum.LOGIN_NONE);
                }
                if (memberInfo.isEmpty() || memberInfo.containsKey("code")) {
                    throw new RefundException(PlatformExceptionEnum.LOGIN_EXPIRE);
                }
            }

            //用户或ip维度的限流
            if (!doLimit(request, memberInfo)) {
                throw new PassedException(PlatformExceptionEnum.API_ACCESS_LIMIT);
            }

            final List<InvokeDetailCache> caches = paramCache.getInvokeDetails();
            for (i = 0; i < size; i++) {
                String invokeName = invokeNames.get(i);
                String methodName = StringUtil.splitLastByDot(invokeName);
                Map<String, Object> inputParam = inputParams.get(i);
                final InvokeDetailCache invokeDetailCache = caches.get(i);

                AssertWrapper.notNull(invokeName, "调用方法名不能为空");
                if (null == inputParam) {
                    inputParam = Collections.emptyMap();
                }
                AssertWrapper.notNull(invokeDetailCache, "调用方法名未找到");
                final Map<String, InjectionInfo> injects = invokeDetailCache.getInjects();

                //拼装输入参数
                List<Object> invokeParams = new ArrayList<>();
                final String[] invokeCacheNames = invokeDetailCache.getNames();
                for (int j = 0; j < invokeCacheNames.length; j++) {
                    String getName = invokeCacheNames[j];
                    Object getParam = inputParam.get(getName);
                    if (null != injects && injects.keySet().contains(getName)) {
                        getParam = doInject(getParam, injects.get(getName), memberInfo, request);
                    }
                    invokeParams.add(getParam);
                }

                Object result = invokeDetailCache.getService().$invoke(methodName, invokeDetailCache.getTypes(), invokeParams.toArray());
                if (null == result) {
                    throw new PassedException(PlatformExceptionEnum.RESULT_ERROR);
                }
                results.add(result);
            }
            if (fromPath) {
                if (results.isEmpty()) {
                    throw new PassedException(PlatformExceptionEnum.RESULT_ERROR);
                } else {
                    response.setPreload(results.get(0));
                }
            } else {
                response.setPreload(results);
            }
        } catch (InnerException ex) {
            log.warn("[GATEWAY] 系统内部异常, 具体信息如上");
            response = new ApiResponse(PlatformExceptionEnum.SYSTEM_ERROR);
        } catch (PassedException ex) {
            log.warn("[GATEWAY] 业务内部校验不通过: {}", ex.getMessage());
            response = ResultWrapper.err(ex);
        } catch (RefundException ex) {
            log.warn("[GATEWAY] 调用了未授权的资源: {}", ex.getMessage());
            response = ResultWrapper.err(ex);
        } catch (RpcException ex) {
            log.error("[GATEWAY] DUBBO调用异常, 具体信息如上");
            response = new ApiResponse(PlatformExceptionEnum.CLIENT_TIMEOUT);
        } catch (GenericException ex) {
            log.error("[GATEWAY] DUBBO调用内部自定义异常, 具体信息如上");
            response = new ApiResponse(PlatformExceptionEnum.CLIENT_ERROR);
        } catch (NullPointerException ex) {
            log.warn("[GATEWAY] 出现空指针异常, 具体信息: ", ex);
            response = new ApiResponse(PlatformExceptionEnum.BAD_REQUEST);
        } catch (Throwable ex) {
            log.warn("[GATEWAY] 系统调用未知异常, 具体信息: ", ex);
            response = new ApiResponse(PlatformExceptionEnum.SYSTEM_BUSY);
        }

        if (!fromPath && response.getCode() != 0) {
            log.warn("[GATEWAY] 异常 [{}] 发生于第[{}]次方法调用", response.getMessage(), i + 1);
            response.setPreload(Collections.singleton(response.getPreload()));
        }

        response.setProof(proof);
        log.info("[GATEWAY] 来源: {} | 分组: {} | 调用方法: {} | 返回结果: {}",
                inputTenant, inputGroup, invokeNames, response);
        return response;
    }

    private InvokeCache doCache(String inputGroup, List<Map<String, Object>> inputParams,
                                List<String> invokeNames) {
        final int size = invokeNames.size();

        boolean whitelist = true;
        int invokeLevel = 0;
        String invokeMember = null;
        List<InvokeDetailCache> invokeDetails = Lists.newArrayListWithExpectedSize(size);

        for (int i = 0; i < size; i++) {
            String invokeName = invokeNames.get(i);
            int paramSize = 0;
            if (null != inputParams.get(i)) {
                paramSize = inputParams.get(i).size();
            }

            //获取缓存的数据
            final InvokeDetailCache invokeDetail = invokeCache(new InvokeKey(inputGroup, invokeName, paramSize));
            if (whitelist && !invokeDetail.getWhitelist()) {
                log.info("[GATEWAY] 方法 {} 没有设置白名单", invokeName);
                whitelist = false;
            }
            final Map<String, InjectionInfo> injects = invokeDetail.getInjects();
            if (null != injects) {
                for (InjectionInfo inject : injects.values()) {
                    final int newLevel = InjectEnum.getLevel(inject.getInjectType());
                    if (newLevel > invokeLevel) {
                        invokeLevel = newLevel;
                        invokeMember = inject.getInvokeName();
                    }
                }
            }

            invokeDetails.add(invokeDetail);
        }
        return new InvokeCache(whitelist, invokeMember, invokeDetails);
    }

    private InvokeDetailCache invokeCache(InvokeKey key) {
        return invokeCache(key, false);
    }

    private InvokeDetailCache invokeCache(InvokeKey key, boolean token) {
        InvokeDetailCache present = genericCache.getIfPresent(key);
        if (null == present) {
            final String inputGroup = key.getGroup();
            final String invokeName = key.getInvoke();
            final int invokeSize = key.getLength();

            final InfoMethodGateway methodSimple = jdbi.withHandle(handle ->
                    infoComponent.getGatewayMethod(handle, inputGroup, invokeName, invokeSize))
                    .orElseThrow(() -> new PassedException(PlatformExceptionEnum.NOT_FOUND));

            //拼装方法名和类型
            List<String> names = Lists.newArrayListWithExpectedSize(invokeSize + 1);
            List<String> types = Lists.newArrayListWithExpectedSize(invokeSize + 1);
            if (null != methodSimple.getSimpleParameter()) {
                //这是一个链表
                final Map<String, String> simpleParam = KryoBaseUtil.readFromByteArray(methodSimple.getSimpleParameter());
                for (Map.Entry<String, String> one : simpleParam.entrySet()) {
                    names.add(one.getKey());
                    types.add(one.getValue());
                }
            }
            Map<String, InjectionInfo> injectParam = Maps.newHashMapWithExpectedSize(invokeSize);
            if (!token) {
                log.info("[GATEWAY}] 当前输入参数对应的名称和类型: {}", names, types);

                if (null != methodSimple.getInjectionInfo()) {
                    injectParam = KryoBaseUtil.readFromByteArray(methodSimple.getInjectionInfo());
                    log.info("[GATEWAY] 当前要通过网关注入的参数: {}", injectParam);
                } else {
                    log.info("[GATEWAY] 当前没有需要通过网关注入的参数");
                }
            }

            ReferenceConfig<GenericService> reference = new ReferenceConfig<>();
            reference.setInterface(methodSimple.getClazzName());
            reference.setApplication(applicationConfig);
            reference.setRegistry(registryConfig);
            reference.setConsumer(consumerConfig);
            reference.setGeneric(true);

            present = new InvokeDetailCache(reference.get(), names.toArray(new String[0]), types.toArray(new String[0]),
                    methodSimple.getIsWhitelist(), injectParam);
            genericCache.put(key, present);
        }
        return present;
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> tokenCache(TokenKey key) {
        //如果token存在则进行之后的操作
        final String token = key.getToken();
        if (StringUtil.isBlank(token) || (token.length() != 64 && token.length() != 128)) {
            return Collections.emptyMap();
        }
        if (StringUtil.isBlank(key.getInject())) {
            key.setInject(InjectEnum.MEMBER_ID.method());
        }

        Map<String, Object> present = tokenCache.getIfPresent(key);
        if (null == present) {
            //先给定一个默认值
            present = Collections.emptyMap();
            String proof = RpcContext.getContext().getAttachment(PlatformConstants.PROOF_KEY);
            String inject = key.getInject();
            if (StringUtil.isBlank(inject)) {
                inject = InjectEnum.MEMBER_ID.method();
            }
            log.info("[GATEWAY] 当前校验等级: {}", inject);

            try {
                final InvokeDetailCache invokeDetailCache = invokeCache(new InvokeKey(key.getGroup(), inject, 1), true);
                present = (Map) invokeDetailCache.getService().$invoke(StringUtil.splitLastByDot(inject),
                        invokeDetailCache.getTypes(), new Object[]{token});
                log.info("[GATEWAY] 用户获取完毕, 信息: {}", present);

                tokenCache.put(key, present);
            } catch (Exception e) {
                log.warn("[GATEWAY] 获取用户信息出错: {}", e.getMessage());
            } finally {
                setRpcContext(key.getTenant(), key.getGroup(), proof);
            }
        }
        return present;
    }

    private boolean doLimit(ServerRequest request, Map<String, Object> member) {
        Long newKey;
        try {
            newKey = CollectionUtil.notEmpty(member) ? NumberUtil.toLong(member.get("userId")) : LimitUtil.convertIp(request);
        } catch (Exception e) {
            log.warn("[GATEWAY] 获取限流key错误: {}", member);
            newKey = LimitUtil.convertIp(request);
        }

        //从redis中初始化或获取bucket
        Bucket bucket;
        Map<Long, List<Bandwidth>> buckets = bucketCache.asMap();
        List<Bandwidth> bandwidths = buckets.get(newKey);
        if (null == bandwidths) {
            bucket = LimitUtil.localBucket(newKey);
        } else {
            bucket = LimitUtil.localBucket(bandwidths);
        }

        boolean consume = bucket.tryConsume(1);
        List<Bandwidth> present = buckets.putIfAbsent(newKey, bucket.getBandwidths());
        if (null != present) {
            bucket = LimitUtil.localBucket(present);
            consume = bucket.tryConsume(1);
            buckets.put(newKey, bucket.getBandwidths());
        }
        return consume;
    }

    @SuppressWarnings("unchecked")
    private Object doInject(Object getParam, InjectionInfo info, Map<String, Object> member, ServerRequest request) {
        if (null == info) {
            return null == getParam ? Collections.emptyMap() : getParam;
        }

        if (InjectEnum.getLevel(info.getInjectType()) > 1) {
            return member;
        } else {
            final Object userId = member.get("userId");
            if (getParam instanceof Map) {
                final ServerRequest.Headers headers = request.headers();
                final Map<String, Object> mapParam = (Map) getParam;
                if (null != userId) {
                    mapParam.put("userId", userId);
                }
                if (info.getHaveAddress()) {
                    mapParam.put("requestIp", LimitUtil.remoteAddress(request));
                }
                final Set<String> cookieNames = info.getCookieNames();
                if (null != cookieNames && cookieNames.size() > 0) {
                    final MultiValueMap<String, HttpCookie> getCookies = request.cookies();
                    final Map<String, String> retCookies = Maps.newHashMapWithExpectedSize(cookieNames.size());
                    for (String cookieName : cookieNames) {
                        final List<HttpCookie> httpCookies = getCookies.get(cookieName);
                        if (null != httpCookies && !httpCookies.isEmpty()) {
                            retCookies.put(cookieName, httpCookies.get(0).getValue());
                        }
                    }
                    mapParam.put("requestCookies", retCookies);
                }
                final Set<String> headerNames = info.getHeaderNames();
                if (null != headerNames && headerNames.size() > 0) {
                    final Map<String, byte[]> retHeaders = Maps.newHashMapWithExpectedSize(headerNames.size());
                    for (String headerName : headerNames) {
                        final List<String> httpHeader = headers.header(headerName);
                        if (!httpHeader.isEmpty()) {
                            retHeaders.put(headerName, httpHeader.get(0).getBytes());
                        }
                    }

                    mapParam.put("requestHeaders", retHeaders);
                }
                return mapParam;
            } else {
                return userId;
            }
        }
    }
}
