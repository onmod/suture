package net.dloud.platform.common.client;

import net.dloud.platform.common.annotation.Permission;
import net.dloud.platform.common.domain.entry.GroupEntry;
import net.dloud.platform.common.domain.result.GatewayGroupResult;
import net.dloud.platform.common.domain.result.GatewayMethodResult;

import java.util.Map;

/**
 * 网关初始化
 *
 * @author QuDasheng
 * @title 网关服务
 * @time 2018-08-27 09:28
 **/
@Permission(false)
public interface GatewayService {
    /**
     * 判断当前分组是否需要更新
     *
     * @return
     */
    GatewayGroupResult groupInfo(GroupEntry groupInfo);

    /**
     * 返回需要更新的方法列表
     *
     * @return
     */
    GatewayMethodResult clazzInfo(GroupEntry groupInfo, boolean newGroup, byte[] clazzInfo);

    /**
     * 更新并返回更新失败的方法列表
     *
     * @return
     */
    GatewayMethodResult methodInfo(GroupEntry groupInfo, Map<String, String> clazzVersion, byte[][] methodInfo);
}
