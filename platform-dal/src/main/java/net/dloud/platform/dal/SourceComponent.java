package net.dloud.platform.dal;

import net.dloud.platform.common.mapper.MapperComponent;
import net.dloud.platform.dal.entity.CenterEntity;
import net.dloud.platform.extend.constant.PlatformConstants;
import org.jdbi.v3.core.Jdbi;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.Optional;

/**
 * @author QuDasheng
 * @create 2018-09-28 21:41
 **/
@Component
public class SourceComponent implements MapperComponent {
    @Autowired
    private Jdbi jdbi;

    @PostConstruct
    public void init() {
        String center = "system_" + PlatformConstants.MODE;
        insertCenterSql = upsert(center, columns(CenterEntity.class),
                params(CenterEntity.class), valuesBaseRemove(CenterEntity.class, keys("systemId"))).build();
        getCenterSql = select(center, columns(CenterEntity.class)).where("system_id = :systemId").build();
    }

    /**
     * 插入资源数据
     */
    private String insertCenterSql;

    public int insertCenter(CenterEntity entity) {
        return jdbi.withHandle(handle ->
                handle.createUpdate(insertCenterSql)
                        .bindBean(entity)
                        .execute());
    }

    /**
     * 读取资源数据
     */
    private String getCenterSql;

    public Optional<CenterEntity> getCenter(int systemId) {
        return jdbi.withHandle(handle ->
                handle.createQuery(getCenterSql)
                        .bind("systemId", systemId)
                        .mapToBean(CenterEntity.class)
                        .findFirst());
    }
}
