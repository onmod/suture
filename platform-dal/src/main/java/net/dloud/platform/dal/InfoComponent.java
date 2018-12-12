package net.dloud.platform.dal;

import net.dloud.platform.common.mapper.MapperComponent;
import net.dloud.platform.dal.entity.InfoClazzEntity;
import net.dloud.platform.dal.entity.InfoClazzField;
import net.dloud.platform.dal.entity.InfoClazzSimple;
import net.dloud.platform.dal.entity.InfoClazzVersion;
import net.dloud.platform.dal.entity.InfoGroupEntity;
import net.dloud.platform.dal.entity.InfoMethodDetail;
import net.dloud.platform.dal.entity.InfoMethodEntity;
import net.dloud.platform.dal.entity.InfoMethodGateway;
import net.dloud.platform.dal.entity.InfoMethodSimple;
import net.dloud.platform.dal.entity.InfoMethodVersion;
import org.jdbi.v3.core.Handle;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.core.statement.PreparedBatch;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;


/**
 * @author QuDasheng
 * @create 2018-09-11 09:58
 **/
@Service
public class InfoComponent implements MapperComponent {
    private static final String INFO_GROUP = "info_group";
    private static final String INFO_CLAZZ = "info_clazz";
    private static final String INFO_METHOD = "info_method";

    @Autowired
    private Jdbi jdbi;


    /**
     * 分组列表
     */
    private final String findGroupSql = select(INFO_GROUP).where("group_name = :groupName").build();

    /**
     * 组详情
     */
    private final String getGroupSql = select(INFO_GROUP)
            .where("group_name = :groupName", "system_id = :systemId").build();

    private final String getGroupByVersionSql = select(INFO_GROUP)
            .where("version_info = :versionInfo").build();

    /**
     * 组更新
     */
    private final String insertGroupSql = insert(INFO_GROUP, columns(InfoGroupEntity.class),
            params(InfoGroupEntity.class)).build();
    private final String updateGroupSql = update(INFO_GROUP, "version_info = :versionInfo", "current_ip = :currentIp")
            .where("group_name = :groupName", "system_id = :systemId").build();
    private final String deleteGroupSql = delete(INFO_GROUP)
            .where("group_name = :groupName", "system_id = :systemId").build();

    /**
     * 展示用类列表
     */
    private final String findSimpleClazzSql = select(INFO_CLAZZ, columns(InfoClazzSimple.class))
            .where("group_name = :groupName", "is_interface = :isInterface", "system_id = :systemId").build();

    /**
     * 展示用方法列表
     */
    private final String findSimpleMethodSql = select(INFO_METHOD, columns(InfoMethodSimple.class))
            .where("group_name = :groupName", "clazz_name in (<clazzNames>)").build();

    /**
     * 展示用类列表
     */
    private final String findClazzFieldSql = select(INFO_CLAZZ, columns(InfoClazzField.class))
            .where("group_name = :groupName", "full_name in (<clazzNames>)").build();

    /**
     * 展示用方法列表
     */
    private final String getMethodDetailSql = select(INFO_METHOD, columns(InfoMethodDetail.class))
            .where("group_name = :groupName", "invoke_name = :invokeName", "invoke_length = :invokeLength").build();

    /**
     * 返回mock结果
     */
    private final String getMethodMockSql = select(INFO_METHOD, "return_mock")
            .where("group_name = :groupName", "invoke_name = :invokeName", "invoke_length = :invokeLength").build();

    /**
     * 更新方法缓存
     */
    private final String updateMethodCacheSql = update(INFO_METHOD,
            "method_data = :methodData", "param_mock = :paramMock", "return_mock = :returnMock")
            .where("group_name = :groupName", "invoke_name = :invokeName", "invoke_length = :invokeLength").build();

    /**
     * 类详情
     */
    private final String getClazzSql = select(INFO_CLAZZ)
            .where("group_name = :groupName", "full_name = :fullName").build();

    /**
     * 方法详情
     */
    private final String getMethodSql = select(INFO_METHOD)
            .where("group_name = :groupName", "invoke_name = :invokeName", "invoke_length = :invokeLength").build();

    /**
     * 网关确定唯一方法
     */
    private final String getSimpleMethodSql = select(INFO_METHOD, columns(InfoMethodGateway.class))
            .where("group_name = :groupName", "invoke_name = :invokeName", "invoke_length = :invokeLength").build();

    /**
     * 查询类用于更新
     */
    private final String findVersionClazzSql = select(INFO_CLAZZ, columns(InfoClazzVersion.class))
            .where("group_name = :groupName", "system_id = :systemId").build();

    /**
     * 查询方法用于更新
     */
    private final String findVersionMethodSql = select(INFO_METHOD, columns(InfoMethodVersion.class))
            .where("group_name = :groupName", "clazz_name IN (<clazzNames>)").build();

    /**
     * 批量更新相同的类
     */
    private final String upsertSelectClazzSql = upsertSelect(INFO_CLAZZ, columnsRemove(InfoClazzEntity.class, keys(DELETED_AT)),
            INFO_CLAZZ, ":newGroup, " + columnsBaseRemove(InfoClazzEntity.class, keys("groupName")) + ", now(), now()",
            valuesRemove(InfoClazzEntity.class, keys(DELETED_AT)),
            "group_name = :oldGroup", "system_id = :systemId", "full_name IN (<fullNames>)")
            .build();

    /**
     * 批量插入或更新类
     */
    private final String upsertClazzSql = upsert(INFO_CLAZZ, columns(InfoClazzEntity.class),
            params(InfoClazzEntity.class), valuesBaseRemove(InfoClazzEntity.class, keys("groupName", "fullName", CREATED_AT))).build();
    private final String batchDeleteClazzSql = delete(INFO_CLAZZ)
            .where("group_name = :groupName", "full_name IN (<clazzNames>)").build();
    /**
     * 批量更新相同的方法
     */
    private final String upsertSelectMethodSql = upsertSelect(INFO_METHOD, columnsRemove(InfoMethodEntity.class, keys(DELETED_AT)),
            INFO_METHOD, ":newGroup, " + columnsBaseRemove((InfoMethodEntity.class), keys("groupName")) + ", now(), now()",
            valuesRemove(InfoMethodEntity.class, keys(DELETED_AT)),
            "group_name = :oldGroup", "system_id = :systemId", "clazz_name IN (<clazzNames>)").build();
    /**
     * 批量插入或更新方法
     */
    private final String upsertMethodSql = upsert(INFO_METHOD, columns(InfoMethodEntity.class),
            params(InfoMethodEntity.class), valuesBaseRemove(InfoMethodEntity.class, keys("groupName", "invokeName", "invokeLength", CREATED_AT))).build();
    private final String deleteMethodSql = delete(INFO_METHOD)
            .where("group_name = :groupName", "invoke_name = :invokeName", "invoke_length = :invokeLength").build();
    private final String batchDeleteMethodSql = delete(INFO_METHOD)
            .where("group_name = :groupName", "invoke_name IN (<invokeNames>)").build();
    private final String batchDeleteMethodByClassSql = delete(INFO_METHOD)
            .where("group_name = :groupName", "clazz_name IN (<clazzNames>)").build();


    public List<InfoGroupEntity> findGroup(Handle handle, String group) {
        return handle.createQuery(findGroupSql)
                .bind("groupName", group)
                .mapToBean(InfoGroupEntity.class)
                .list();
    }

    public Optional<InfoGroupEntity> getGroup(Handle handle, String group, Integer system) {
        return handle.createQuery(getGroupSql)
                .bind("groupName", group)
                .bind("systemId", system)
                .mapToBean(InfoGroupEntity.class)
                .findFirst();
    }

    public Optional<InfoGroupEntity> getGroupByVersion(Handle handle, String version) {
        return handle.createQuery(getGroupByVersionSql)
                .bind("versionInfo", version)
                .mapToBean(InfoGroupEntity.class)
                .findFirst();
    }

    public int insertGroup(Handle handle, InfoGroupEntity entity) {
        return handle.createUpdate(insertGroupSql)
                .bindBean(entity)
                .execute();
    }

    public int updateGroup(Handle handle, InfoGroupEntity entity) {
        return handle.createUpdate(updateGroupSql)
                .bindBean(entity)
                .execute();
    }

    public int deleteGroup(Handle handle, String group, Integer ip, Integer system) {
        return handle.createUpdate(deleteGroupSql)
                .bind("groupName", group)
                .bind("ip", ip)
                .bind("systemId", system)
                .execute();
    }

    public List<InfoClazzSimple> findSimpleClazz(Handle handle, String group, Integer system, boolean isInterface) {
        return handle.createQuery(findSimpleClazzSql)
                .bind("groupName", group)
                .bind("isInterface", isInterface)
                .bind("systemId", system)
                .mapToBean(InfoClazzSimple.class)
                .list();
    }

    public List<InfoMethodSimple> findSimpleMethod(Handle handle, String group, List<String> clazz) {
        return handle.createQuery(findSimpleMethodSql)
                .bind("groupName", group)
                .bindList("clazzNames", clazz)
                .mapToBean(InfoMethodSimple.class)
                .list();
    }

    public List<InfoClazzField> findClazzField(Handle handle, String group, List<String> clazz) {
        return handle.createQuery(findClazzFieldSql)
                .bind("groupName", group)
                .bindList("clazzNames", clazz)
                .mapToBean(InfoClazzField.class)
                .list();
    }

    public Optional<InfoMethodDetail> getMethodDetail(Handle handle, String group, String invokeName, Integer invokeLength) {
        return handle.createQuery(getMethodDetailSql)
                .bind("groupName", group)
                .bind("invokeName", invokeName)
                .bind("invokeLength", invokeLength)
                .mapToBean(InfoMethodDetail.class)
                .findFirst();
    }

    public Optional<String> getMethodMock(Handle handle, String group, String invokeName, Integer invokeLength) {
        return handle.createQuery(getMethodMockSql)
                .bind("groupName", group)
                .bind("invokeName", invokeName)
                .bind("invokeLength", invokeLength)
                .mapTo(String.class)
                .findFirst();
    }

    public int updateMethodCache(Handle handle, String group, String invokeName, Integer invokeLength,
                                 byte[] methodData, String paramMock, String returnMock) {
        return handle.createUpdate(updateMethodCacheSql)
                .bind("groupName", group)
                .bind("invokeName", invokeName)
                .bind("invokeLength", invokeLength)
                .bind("methodData", methodData)
                .bind("paramMock", paramMock)
                .bind("returnMock", returnMock)
                .execute();
    }

    public Optional<InfoClazzEntity> getClazz(Handle handle, String group, String fullName) {
        return handle.createQuery(getClazzSql)
                .bind("groupName", group)
                .bind("fullName", fullName)
                .mapToBean(InfoClazzEntity.class).findFirst();
    }

    public Optional<InfoMethodEntity> getMethod(Handle handle, String group, String invokeName, Integer invokeLength) {
        return handle.createQuery(getMethodSql)
                .bind("groupName", group)
                .bind("invokeName", invokeName)
                .bind("invokeLength", invokeLength)
                .mapToBean(InfoMethodEntity.class).findFirst();
    }

    public Optional<InfoMethodGateway> getGatewayMethod(Handle handle, String group, String invokeName, Integer invokeLength) {
        return handle.createQuery(getSimpleMethodSql)
                .bind("groupName", group)
                .bind("invokeName", invokeName)
                .bind("invokeLength", invokeLength)
                .mapToBean(InfoMethodGateway.class)
                .findFirst();
    }

    public List<InfoClazzVersion> findVersionClazz(Handle handle, String group, Integer system) {
        return handle.createQuery(findVersionClazzSql)
                .bind("groupName", group)
                .bind("systemId", system)
                .mapToBean(InfoClazzVersion.class)
                .list();
    }

    public List<InfoMethodVersion> findVersionMethod(Handle handle, String group, List<String> clazzNames) {
        return handle.createQuery(findVersionMethodSql)
                .bind("groupName", group)
                .bindList("clazzNames", clazzNames)
                .mapToBean(InfoMethodVersion.class)
                .list();
    }

    public int insertSelectClazz(Handle handle, String oldGroup, String newGroup, Integer system, List<String> fullNames) {
        return handle.createUpdate(upsertSelectClazzSql)
                .bind("oldGroup", oldGroup)
                .bind("newGroup", newGroup)
                .bind("systemId", system)
                .bindList("fullNames", fullNames)
                .execute();
    }

    public int mergeClazz(Handle handle, List<InfoClazzEntity> entities) {
        PreparedBatch batch = handle.prepareBatch(upsertClazzSql);
        for (InfoClazzEntity one : entities) {
            batch.bindBean(one).add();
        }
        return batchNum(batch.execute());
    }

    public int batchDeleteClazz(Handle handle, String group, List<String> clazzNames) {
        return handle.createUpdate(batchDeleteClazzSql)
                .bind("groupName", group)
                .bindList("clazzNames", clazzNames)
                .execute();
    }

    public int insertSelectMethod(Handle handle, String oldGroup, String newGroup, Integer system, List<String> clazzNames) {
        return handle.createUpdate(upsertSelectMethodSql)
                .bind("oldGroup", oldGroup)
                .bind("newGroup", newGroup)
                .bind("systemId", system)
                .bindList("clazzNames", clazzNames)
                .execute();
    }

    public int mergeMethod(Handle handle, List<InfoMethodEntity> entities) {
        PreparedBatch batch = handle.prepareBatch(upsertMethodSql);
        for (InfoMethodEntity one : entities) {
            batch.bindBean(one).add();
        }
        return batchNum(batch.execute());
    }

    public int deleteMethod(Handle handle, String group, String invokeName, int invokeLength) {
        return handle.createUpdate(deleteMethodSql)
                .bind("groupName", group)
                .bind("invokeName", invokeName)
                .bind("invokeLength", invokeLength)
                .execute();
    }

    public int batchDeleteMethod(Handle handle, String group, List<String> invokeNames) {
        return handle.createUpdate(batchDeleteMethodSql)
                .bind("groupName", group)
                .bindList("invokeNames", invokeNames)
                .execute();
    }

    public int batchDeleteMethodByClass(Handle handle, String group, List<String> clazzNames) {
        return handle.createUpdate(batchDeleteMethodByClassSql)
                .bind("groupName", group)
                .bindList("clazzNames", clazzNames)
                .execute();
    }
}
