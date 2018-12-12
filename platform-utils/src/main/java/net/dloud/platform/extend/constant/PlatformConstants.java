package net.dloud.platform.extend.constant;

import com.alibaba.dubbo.rpc.RpcContext;
import com.ctrip.framework.apollo.Config;
import com.ctrip.framework.apollo.ConfigService;
import net.dloud.platform.common.network.IPConvert;

/**
 * @author QuDasheng
 * @create 2018-09-02 16:23
 **/
public class PlatformConstants {
    public static final String BOOTSTRAP_PROPERTY_SOURCE_NAME = "DloudBootstrapPropertySources";

    public static final Config CONFIG = ConfigService.getAppConfig();

    public static final Config COMMON = ConfigService.getConfig("DEV.COMM");

    public static final int APPID = CONFIG.getIntProperty("app.id", 0);

    public static final String APPNAME = CONFIG.getProperty("app.name", "");

    public static final String DEFAULT_GROUP = "stable";

    public static final String GROUP = CONFIG.getProperty("app.group", DEFAULT_GROUP);

    public static final String SECRET= CONFIG.getProperty("app.secret", "");

    public static final String MODE_DEV = "dev";

    public static final String MODE = COMMON.getProperty("run.mode", "");

    public static final String HOST = COMMON.getProperty("run.host", "");

    public static final String LOCAL_HOST_IP = RpcContext.getContext().getLocalHost();

    public static final int LOCAL_HOST_IP_INT = IPConvert.ip2Num(LOCAL_HOST_IP);

    public static final int PROCESSOR_NUMBER = Runtime.getRuntime().availableProcessors();

    public static final String ZK_ADDRESS = COMMON.getProperty("zookeeper.address", "");

    public static final int ZK_SLEEP_TIME = COMMON.getIntProperty("zookeeper.sleep-time-in-millis", 1000);

    public static final int ZK_MAX_RETRIES = COMMON.getIntProperty("zookeeper.max-retries", 3);

    public static final String DUBBO_LOAD_BALANCE = COMMON.getProperty("dubbo.provider.loadbalance", "random");

    public static final String KAFKA_TOPIC = PlatformConstants.APPID + "-" + PlatformConstants.MODE;

    public static final String KAFKA_CONSUMER_GROUP = PlatformConstants.KAFKA_TOPIC + "-" + PlatformConstants.GROUP;

    public static final String PLATFORM_PACKAGE = "net.dloud.platform";

    public static final String PARSE_BASE_PATH = "classpath*:PARSE-INF/";

    public static final String SOURCE_MYSQL = "mysql";
    public static final String SOURCE_MYSQL_PUBLIC = "mysql_public";
    public static final String SOURCE_REDIS = "redis";
    public static final String SOURCE_REDIS_CORE = "redis_core";
    public static final String SOURCE_IGNITE = "ignite";
    public static final String SOURCE_ELASTIC = "elastic";
    public static final String SOURCE_DRUID = "druid";

    public static final String FROM_KEY = "tenant";

    public static final String GROUP_KEY = "group";

    public static final String PROOF_KEY = "proof";

    public static final String SUBGROUP_KEY = "subgroup";

    public static final String HANDGROUP_KEY = "handgroup";

    public static final int EXCEPTION_CODE_PASSED = 1;
    public static final int EXCEPTION_CODE_INNER = 2;
    public static final int EXCEPTION_CODE_REFUND = -1;
    public static final int EXCEPTION_CODE_UNKNOWN = 9;
}
