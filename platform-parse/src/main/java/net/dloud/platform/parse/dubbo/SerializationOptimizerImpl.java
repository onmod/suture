package net.dloud.platform.parse.dubbo;

import com.alibaba.dubbo.common.serialize.support.SerializationOptimizer;
import lombok.extern.slf4j.Slf4j;
import net.dloud.platform.common.domain.BaseEntry;
import net.dloud.platform.common.domain.BaseResult;
import net.dloud.platform.common.serialize.KryoBaseUtil;
import net.dloud.platform.extend.constant.PlatformConstants;
import net.dloud.platform.parse.utils.ResourceGet;

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/**
 * 不要注册类
 *
 * @author QuDasheng
 * @create 2018-08-30 20:38
 **/
@Slf4j
@Deprecated
public class SerializationOptimizerImpl implements SerializationOptimizer {
    @Override
    public Collection<Class> getSerializableClasses() {
        List<Class> classes = new LinkedList<Class>();
        classes.add(BaseEntry.class);
        classes.add(BaseResult.class);

        try {
            final Set<String> clazzNames = new HashSet<>();
            final byte[][] multiFiles = ResourceGet.resourceMulti2Byte(PlatformConstants.PARSE_BASE_PATH + "optimizer");

            for (byte[] file : multiFiles) {
                if (null != file && file.length > 0) {
                    clazzNames.addAll(KryoBaseUtil.readFromByteArray(file, true, false));
                }
            }
            for (String clazzName : clazzNames) {
                classes.add(Class.forName(clazzName));
            }
            log.info("[{}] 注册到kryo的类有: {}", PlatformConstants.APPNAME, clazzNames);
        } catch (Exception e) {
            log.error("[{}] 注册到kryo失败: {}", PlatformConstants.APPNAME, e.getMessage());
        }

        return classes;
    }
}