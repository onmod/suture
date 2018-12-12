package net.dloud.platform.common.client;

import net.dloud.platform.common.annotation.Permission;
import net.dloud.platform.common.domain.entry.GroupEntry;
import net.dloud.platform.common.domain.result.PairResult;

/**
 * 申请初始化数据库连接等资源
 *
 * @author QuDasheng
 * @title 初始化服务
 * @time 2018-08-27 09:39
 **/
@Permission(false)
public interface InitialService {
    /**
     * 获取资源
     *
     * @return
     */
    PairResult<byte[], byte[]> getSource(GroupEntry info);
}
