package net.dloud.platform.parse.dubbo;

import com.alibaba.dubbo.common.URL;
import com.alibaba.dubbo.common.extension.Activate;
import com.alibaba.dubbo.rpc.Invocation;
import com.alibaba.dubbo.rpc.Invoker;
import com.alibaba.dubbo.rpc.RpcContext;
import com.alibaba.dubbo.rpc.RpcException;
import com.alibaba.dubbo.rpc.cluster.LoadBalance;
import com.alibaba.dubbo.rpc.cluster.loadbalance.ConsistentHashLoadBalance;
import com.alibaba.dubbo.rpc.cluster.loadbalance.LeastActiveLoadBalance;
import com.alibaba.dubbo.rpc.cluster.loadbalance.RandomLoadBalance;
import com.alibaba.dubbo.rpc.cluster.loadbalance.RoundRobinLoadBalance;
import com.google.common.collect.Maps;
import lombok.extern.slf4j.Slf4j;
import net.dloud.platform.common.extend.CollectionUtil;
import net.dloud.platform.common.serialize.KryoBaseUtil;
import net.dloud.platform.extend.constant.PlatformConstants;
import org.apache.curator.shaded.com.google.common.collect.Lists;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static net.dloud.platform.parse.dubbo.wrapper.DubboWrapper.dubboProvider;

/**
 * @author QuDasheng
 * @create 2018-09-06 17:24
 **/
@Slf4j
@Activate(value = "soft", order = 10)
public class SoftLoadBalanceImpl implements LoadBalance {
    private RandomLoadBalance random;

    private RoundRobinLoadBalance roundRobin;

    private LeastActiveLoadBalance leastActive;

    private ConsistentHashLoadBalance consistentHash;

    @Override
    @SuppressWarnings("unchecked")
    public <T> Invoker<T> select(List<Invoker<T>> invokers, URL url, Invocation invocation) throws RpcException {
        int length = invokers.size();
        boolean isSame = false;
        String methodName = invocation.getMethodName();
        String thisGroup = RpcContext.getContext().getAttachment(PlatformConstants.SUBGROUP_KEY);
        if (null == thisGroup) {
            isSame = true;
            thisGroup = PlatformConstants.GROUP;
        }
        if (!isSame && PlatformConstants.GROUP.equals(thisGroup)) {
            isSame = true;
        }

        Set<String> availableProvider = null;
        //当前分组和输入分组相同则直接从util中取值，否则从context中取值
        if (isSame) {
            availableProvider = dubboProvider.get(thisGroup);
        } else {
            String handGroup = RpcContext.getContext().getAttachment(PlatformConstants.HANDGROUP_KEY);
            if (null != handGroup) {
                try {
                    availableProvider = KryoBaseUtil.readObjectFromString(handGroup, Set.class);
                    log.info("[PLATFORM] 获取附加分组列表: {}", availableProvider);
                } catch (Exception e) {
                    log.warn("[PLATFORM] 获取附加分组列表失败: {}", e.getMessage());
                }
            }
        }

        List<Invoker<T>> invokerList = Lists.newArrayListWithExpectedSize(length);
        //如果输入分组无可用，调用当前服务分组
        if (CollectionUtil.isEmpty(availableProvider)) {
            log.info("[PLATFORM] 路由[DUBBO]服务: {}, 输入分组[{}]无可用服务，调用当前服务分组[{}]", methodName, thisGroup, PlatformConstants.GROUP);
            thisGroup = PlatformConstants.GROUP;
            availableProvider = dubboProvider.get(PlatformConstants.GROUP);
        }

        if (CollectionUtil.isEmpty(availableProvider)) {
            invokerList = invokers;
            log.info("[PLATFORM] 路由[DUBBO]服务: {}, 分组[{}]无可用服务，使用所有提供者", methodName, thisGroup);
        } else {
            if (length > 1) {
                final Map<String, Invoker<T>> dubboInvokers = Maps.newHashMapWithExpectedSize(length);
                for (int i = 0; i < length; i++) {
                    final Invoker<T> invoker = invokers.get(i);
                    final URL invokeUrl = invoker.getUrl();
                    dubboInvokers.put(invokeUrl.getAddress(), invoker);
                }
                final Set<String> dubboProvider = dubboInvokers.keySet();
                dubboProvider.retainAll(availableProvider);
                log.info("[PLATFORM] 路由[DUBBO]服务: {}, 使用分组[{}], 分组可用列表: {}", methodName, thisGroup, dubboProvider);

                if (dubboProvider.isEmpty()) {
                    invokerList.addAll(invokers);
                    log.info("[PLATFORM] 路由[DUBBO]服务: {}, 分组[{}]无可用服务，使用所有提供者", methodName, thisGroup);
                } else {
                    for (Iterator<String> it = dubboProvider.iterator(); it.hasNext(); ) {
                        final String next = it.next();
                        final Invoker<T> invoker = dubboInvokers.get(next);
                        if (null != invoker) {
                            invokerList.add(invoker);
                        }
                    }
                }
                if (invokerList.isEmpty()) {
                    invokerList.addAll(invokers);
                    log.info("[PLATFORM] 路由[DUBBO]服务: {}, 分组[{}]无可用服务，使用所有提供者", methodName, thisGroup);
                }
            } else if (length == 1) {
                invokerList = invokers;
                log.info("[PLATFORM] 路由[DUBBO]服务: {}, 只有这一个可用服务", methodName);
            } else {
                log.error("[PLATFORM] 路由[DUBBO]服务失败: {}, 无提供者", methodName);
            }
        }

        //不实用软引用了，没必要
        switch (PlatformConstants.DUBBO_LOAD_BALANCE) {
            case RoundRobinLoadBalance.NAME:
                if (null == roundRobin)
                    roundRobin = new RoundRobinLoadBalance();
                return roundRobin.select(invokerList, url, invocation);
            case LeastActiveLoadBalance.NAME:
                if (null == leastActive)
                    leastActive = new LeastActiveLoadBalance();
                return leastActive.select(invokerList, url, invocation);
            default:
                if (null == random)
                    random = new RandomLoadBalance();
                return random.select(invokerList, url, invocation);
        }
    }
}
