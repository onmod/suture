package net.dloud.platform.parse.curator.wrapper;

import com.alibaba.dubbo.common.utils.ConcurrentHashSet;
import com.alibaba.dubbo.rpc.RpcContext;
import lombok.extern.slf4j.Slf4j;
import net.dloud.platform.common.extend.StringUtil;
import net.dloud.platform.extend.constant.PlatformConstants;
import net.dloud.platform.extend.exception.InnerException;
import net.dloud.platform.parse.curator.listener.ListenerExecutor;
import net.dloud.platform.parse.utils.RunHost;
import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.imps.CuratorFrameworkState;
import org.apache.curator.framework.recipes.cache.PathChildrenCache;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheListener;
import org.apache.curator.framework.state.ConnectionStateListener;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.data.Stat;

import java.util.Set;

/**
 * @author QuDasheng
 * @create 2018-09-11 10:12
 **/
@Slf4j
public class CuratorWrapper {
    private static CuratorFramework curatorClient;

    private static Set<ListenerExecutor> listenerExecutors;


    public static String currentPath(String inputHost, Integer inputPort) {
        final RpcContext context = RpcContext.getContext();
        String localHost = context.getLocalHost();
        if (RunHost.canUseDomain(RunHost.localHost, inputHost)) {
            localHost = inputHost;
        }
        return localHost + ":" + inputPort;
    }

    public static synchronized CuratorFramework initClient() {
        if (null == curatorClient) {
            final RetryPolicy retryPolicy = new ExponentialBackoffRetry(PlatformConstants.ZK_SLEEP_TIME, PlatformConstants.ZK_MAX_RETRIES);
            try (CuratorFramework client = CuratorFrameworkFactory.newClient(PlatformConstants.ZK_ADDRESS, retryPolicy)) {
                curatorClient = client;
            } catch (Exception e) {
                log.error("[{}] ZK初始化失败: {}, {}", PlatformConstants.APPNAME, e.getMessage(), e);
                throw new InnerException("初始化资源失败");
            }
        }

        curatorClient.getConnectionStateListenable().addListener(stateListener());
        if (curatorClient.getState() != CuratorFrameworkState.STARTED) {
            curatorClient.start();
        }

        return curatorClient;
    }

    private static ConnectionStateListener stateListener() {
        return (client, event) -> {
            switch (event) {
                case CONNECTED:
                    log.info("[{}] 当前正在进行连接...", PlatformConstants.APPNAME);
                    setListeners();
                    break;
                case RECONNECTED:
                    log.info("[{}] 当前正在进行重连...", PlatformConstants.APPNAME);
                    setListeners();
                    break;
                case SUSPENDED:
                    log.info("[{}] 当前连接中断...等待重连...", PlatformConstants.APPNAME);
                    break;
                case LOST:
                    log.info("[{}] 当前连接丢失...等待重连...", PlatformConstants.APPNAME);
                    break;
                default:
                    log.info("[{}] 当前连接状态[{}]", PlatformConstants.APPNAME, event);
            }
        };
    }

    public static void childrenCache(PathChildrenCacheListener cacheListener, String currentPath, String basePath) {
        try {
            if (StringUtil.notBlank(currentPath)) {
                final Stat stat = curatorClient.checkExists().forPath(currentPath);
                if (null != stat) {
                    log.info("[{}] 当前节点({})已存在, 删除重建", PlatformConstants.APPNAME, currentPath);
                    curatorClient.delete().forPath(currentPath);
                }
                curatorClient.create().creatingParentContainersIfNeeded().withMode(CreateMode.EPHEMERAL)
                        .forPath(currentPath, PlatformConstants.APPNAME.getBytes());
            }
            PathChildrenCache cache = new PathChildrenCache(curatorClient, basePath, false);
            cache.start();
            cache.getListenable().addListener(cacheListener);
        } catch (Exception e) {
            log.error("[{}] 新建CURATOR节点或监听器失败: {}, {}, {}", PlatformConstants.APPNAME, currentPath, basePath, e.getMessage());
        }
    }

    private static void setListeners() {
        if (null == listenerExecutors || listenerExecutors.isEmpty()) {
            return;
        }

        for (ListenerExecutor executor : listenerExecutors) {
            executor.execute();
        }
    }

    public static void addListeners(ListenerExecutor executor) {
        if (null == listenerExecutors) {
            listenerExecutors = new ConcurrentHashSet<>();
        }
        //先执行一次
        executor.execute();
        final boolean add = listenerExecutors.add(executor);
        if (!add) {
            log.error("[{}] ZK添加监听器失败", PlatformConstants.APPNAME);
        }
    }

    public static CuratorFramework getClient() {
        if (null == curatorClient) {
            curatorClient = initClient();
        }

        if (curatorClient.getState() != CuratorFrameworkState.STARTED) {
            curatorClient.start();
        }
        return curatorClient;
    }
}
