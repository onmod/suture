package net.dloud.platform.gateway.conf;

import com.alibaba.dubbo.common.utils.ConcurrentHashSet;
import lombok.extern.slf4j.Slf4j;
import net.dloud.platform.extend.constant.PlatformConstants;
import net.dloud.platform.parse.curator.wrapper.CuratorWrapper;
import net.dloud.platform.parse.dubbo.wrapper.DubboWrapper;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.cache.ChildData;
import org.apache.curator.framework.recipes.cache.PathChildrenCache;
import org.apache.zookeeper.data.Stat;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

import java.util.Set;

import static net.dloud.platform.parse.dubbo.wrapper.DubboWrapper.DUBBO_GROUP_PATH;
import static net.dloud.platform.parse.dubbo.wrapper.DubboWrapper.DUBBO_ZK_PATH;
import static net.dloud.platform.parse.dubbo.wrapper.DubboWrapper.dubboProvider;

/**
 * @author QuDasheng
 * @create 2018-09-07 11:30
 **/
@Slf4j
@Component
public class CuratorListener implements ApplicationListener<ContextRefreshedEvent> {
    private CuratorFramework curatorClient = CuratorWrapper.getClient();

    @Override
    public void onApplicationEvent(ContextRefreshedEvent refreshedEvent) {
        try {
            PathChildrenCache cache = new PathChildrenCache(curatorClient, DUBBO_ZK_PATH, false);
            cache.start();
            cache.getListenable().addListener((client, event) -> {
                final ChildData data = event.getData();
                switch (event.getType()) {
                    case CHILD_ADDED:
                        final String add = data.getPath().replaceFirst(DUBBO_ZK_PATH + "/", "");
                        final String basePath = DUBBO_ZK_PATH + "/" + add;
                        if (!PlatformConstants.GROUP.equals(add)) {
                            CuratorWrapper.addListeners(() -> CuratorWrapper.childrenCache(DubboWrapper.dubboListener
                                    (add, basePath), null, basePath));
                        }
                        log.info("[GATEWAY] 添加DUBBO GROUP: {}", add);
                        break;
                    case CHILD_REMOVED:
                        final String del = data.getPath().replaceFirst(DUBBO_ZK_PATH + "/", "");
                        dubboProvider.remove(del);
                        log.info("[GATEWAY] 移除DUBBO GROUP: {}", del);
                        break;
                    default:
                        log.info("[GATEWAY] DUBBO GROUP节点状态: {}", event.getType());
                }
            });
        } catch (Exception e) {
            log.error("[GATEWAY] 监听DUBBO GROUP节点失败: {}", e.getMessage());
        }
    }
}
