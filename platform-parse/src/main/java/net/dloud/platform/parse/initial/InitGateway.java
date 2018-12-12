package net.dloud.platform.parse.initial;

import com.alibaba.dubbo.config.annotation.Reference;
import lombok.extern.slf4j.Slf4j;
import net.dloud.platform.common.client.GatewayService;
import net.dloud.platform.common.domain.entry.GroupEntry;
import net.dloud.platform.common.domain.result.GatewayGroupResult;
import net.dloud.platform.common.domain.result.GatewayMethodResult;
import net.dloud.platform.extend.constant.PlatformConstants;
import net.dloud.platform.extend.constant.StartupConstants;
import net.dloud.platform.parse.utils.ResourceGet;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.ContextRefreshedEvent;

import java.util.List;

/**
 * @author QuDasheng
 * @create 2018-08-27 09:30
 **/
@Slf4j
@Configuration
@ConditionalOnProperty(name = "gateway.notice.enable", matchIfMissing = true, havingValue = "true")
@AutoConfigureAfter(InitSource.class)
public class InitGateway {
    private static final String PARSE_PATH = PlatformConstants.PARSE_BASE_PATH + PlatformConstants.APPID + "/";

    @Reference
    private GatewayService gateway;

    @Bean("gatewayListener")
    public ApplicationListener<ContextRefreshedEvent> gatewayListener() {
        return (event) -> {
            try {
                final String version = new String(ResourceGet.resourceFile2Byte(PARSE_PATH + "version"));
                log.info("[{}] 开始初始化网关, 当前版本({})", PlatformConstants.APPNAME, version);
                final GroupEntry groupInfo = new GroupEntry(PlatformConstants.APPID, PlatformConstants.APPNAME, PlatformConstants.MODE,
                        PlatformConstants.GROUP, PlatformConstants.LOCAL_HOST_IP + ":" + StartupConstants.SERVER_PORT, version);
                final GatewayGroupResult groupResult = gateway.groupInfo(groupInfo);
                if (null != groupResult && groupResult.isSuccess() && !groupResult.isConsistent()) {
                    log.info("[{}] 开始初始化网关, 是否是新组({})", PlatformConstants.APPNAME, groupResult.isNewgroup());
                    final byte[] index = ResourceGet.resourceFile2Byte(PARSE_PATH + "index");
                    methodResult(groupInfo, gateway.clazzInfo(groupInfo, groupResult.isNewgroup(), index));
                } else {
                    log.warn("[{}] 当前不需要初始化网关: {}", PlatformConstants.APPNAME, groupResult);
                }
            } catch (Exception e) {
                log.warn("[" + PlatformConstants.APPNAME + "] 初始化网关失败: ", e);
            }
        };
    }

    private void methodResult(GroupEntry groupInfo, GatewayMethodResult methodResult) {
        if (methodResult.isSuccess()) {
            final List<String> methodList = methodResult.getClassList();
            final int methodSize = methodList.size();
            if (methodSize <= 0) {
                log.info("[{}] 没有需要初始化的方法", PlatformConstants.APPNAME);
                return;
            }

            byte[][] dataList = new byte[methodSize][];
            for (int i = 0; i < methodSize; i++) {
                dataList[i] = ResourceGet.resourceFile2Byte(PARSE_PATH + methodList.get(i));
            }

            final GatewayMethodResult newResult = gateway.methodInfo(groupInfo, methodResult.getClassVersion(), dataList);
            if (newResult.isSuccess()) {
                if (null == newResult.getClassList() || newResult.getClassList().isEmpty()) {
                    log.info("[{}] 初始化网关成功", PlatformConstants.APPNAME);
                } else {
                    methodResult(groupInfo, methodResult);
                }
            } else {
                log.warn("[{}] 初始化网关失败, 方法列表: {}", PlatformConstants.APPNAME, methodList);
            }
        }
    }
}
