package net.dloud.platform.center.core;

import com.alibaba.dubbo.config.annotation.Service;
import lombok.extern.slf4j.Slf4j;
import net.dloud.platform.common.client.InitialService;
import net.dloud.platform.common.domain.entry.GroupEntry;
import net.dloud.platform.common.domain.result.PairResult;
import net.dloud.platform.dal.SourceComponent;
import net.dloud.platform.dal.entity.CenterEntity;
import net.dloud.platform.extend.constant.PlatformExceptionEnum;
import net.dloud.platform.extend.exception.PassedException;
import org.jdbi.v3.core.Jdbi;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Optional;

/**
 * @author QuDasheng
 * @create 2018-08-30 16:54
 **/
@Slf4j
@Service
public class InitialServiceImpl implements InitialService {
    @Autowired
    private Jdbi jdbi;

    @Autowired
    private SourceComponent sourceComponent;


    @Override
    public PairResult<byte[], byte[]> getSource(GroupEntry info) {
        final Optional<CenterEntity> center = sourceComponent.getCenter(info.getSystemId());
        if (center.isPresent()) {
            final CenterEntity entity = center.get();
            return new PairResult<>(entity.getSystemSecret(), entity.getSourceInit());
        } else {
            throw new PassedException(PlatformExceptionEnum.SOURCE_NOT_EXIST);
        }
    }
}
