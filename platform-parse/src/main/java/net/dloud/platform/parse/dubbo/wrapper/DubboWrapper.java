package net.dloud.platform.parse.dubbo.wrapper;

import com.alibaba.dubbo.common.utils.ConcurrentHashSet;
import lombok.extern.slf4j.Slf4j;
import net.dloud.platform.extend.constant.PlatformConstants;
import net.dloud.platform.extend.constant.StartupConstants;
import net.dloud.platform.extend.tuple.PairTuple;
import net.dloud.platform.parse.curator.wrapper.CuratorWrapper;
import org.apache.curator.framework.recipes.cache.ChildData;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheListener;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * @author QuDasheng
 * @create 2018-09-25 14:57
 **/
@Slf4j
public class DubboWrapper {
    public static final String DUBBO_ZK_PATH = "/dubbo_group";
    public static final String DUBBO_GROUP_PATH = DUBBO_ZK_PATH + "/" + PlatformConstants.GROUP;
    private static final String DEFAULT_SPLIT = " || ";
    public static Map<String, Set<String>> dubboProvider = new HashMap<>();

    public static String currentPath() {
        return DUBBO_GROUP_PATH + "/" + CuratorWrapper.currentPath(PlatformConstants.HOST, StartupConstants.DUBBO_PORT);
    }

    /**
     * 异常信息合并
     */
    public static String getMessage(String code, String message) {
        return code + DEFAULT_SPLIT + message;
    }

    /**
     * 异常信息合并
     */
    public static PairTuple<String, String> parseMessage(String message) {
        if (null == message) {
            return new PairTuple<>("", "");
        }

        final String[] split = message.split(DEFAULT_SPLIT);
        if (split.length == 0) {
            return new PairTuple<>("", "");
        }

        if (split.length == 1) {
            return new PairTuple<>(split[0], "");
        } else {
            return new PairTuple<>(split[0], split[1]);
        }
    }

    public static PathChildrenCacheListener dubboListener(String group, String basePath) {
        final Set<String> availableProvider = dubboProvider.getOrDefault(group, new ConcurrentHashSet<>());
        if (!availableProvider.contains(group)) {
            dubboProvider.put(group, availableProvider);
        }

        return (client, event) -> {
            final ChildData data = event.getData();
            switch (event.getType()) {
                case INITIALIZED:
                    log.info("[PLATFORM] 初始化GROUP[{}]", group);
                    break;
                case CHILD_ADDED:
                    final String add = data.getPath().replaceFirst(basePath + "/", "");
                    availableProvider.add(add);
                    log.info("[PLATFORM] 节点[{}]加入GROUP[{}]", add, group);
                    break;
                case CHILD_REMOVED:
                    final String del = data.getPath().replaceFirst(basePath + "/", "");
                    log.info("[PLATFORM] 节点[{}]移出GROUP[{}]", del, group);
                    break;
                default:
                    log.info("[PLATFORM] 当前节点状态: {}", event.getType());
            }
        };
    }
}
