package net.dloud.platform.parse.boot;

import com.ctrip.framework.apollo.spring.config.PropertySourcesConstants;
import com.google.common.collect.ImmutableMap;
import lombok.extern.slf4j.Slf4j;
import net.dloud.platform.common.extend.StringUtil;
import net.dloud.platform.extend.constant.PlatformConstants;
import net.dloud.platform.extend.constant.StartupConstants;
import net.dloud.platform.extend.exception.InnerException;
import net.dloud.platform.extend.wrapper.AssertWrapper;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.CompositePropertySource;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.PropertySource;

import java.io.IOException;
import java.net.ServerSocket;
import java.nio.charset.StandardCharsets;

/**
 * @author QuDasheng
 * @create 2018-10-03 14:39
 **/
@Slf4j
public class DloudApplicationContextInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {

    @Override
    public void initialize(ConfigurableApplicationContext context) {
        // set file encoding
        System.setProperty("file.encoding", StandardCharsets.UTF_8.name());

        final ConfigurableEnvironment environment = context.getEnvironment();
        final String env = environment.getProperty("env");
        final String idc = environment.getProperty("idc");
        if (StringUtil.notBlank(env)) {
            System.setProperty("env", env);
        }
        if (StringUtil.notBlank(idc)) {
            System.setProperty("idc", idc);
        }

        if (environment.getPropertySources().contains(PropertySourcesConstants.APOLLO_BOOTSTRAP_PROPERTY_SOURCE_NAME)) {
            // apollo already initialized
            final String appPort = environment.getProperty("app.port");
            final Integer serverPort = environment.getProperty("server.port", Integer.class);
            final Integer dubboPort = environment.getProperty("dubbo.port", Integer.class);
            if (null != serverPort && null != dubboPort) {
                StartupConstants.SERVER_PORT = serverPort;
                StartupConstants.DUBBO_PORT = dubboPort;
                return;
            }
            AssertWrapper.notNull(appPort, "App port is not set");

            final String[] portSplit = appPort.split("\\.\\.");
            if (portSplit.length < 1) {
                throw new InnerException("App port range set error");
            }
            // port range default is 100
            final int portStart = Integer.parseInt(portSplit[0]);
            final int portEnd = portSplit.length == 2 ? Integer.parseInt(portSplit[1]) : portStart + 100;
            log.info("App port range from {} to {}", portStart, portEnd);

            int availableCount = 0;
            int[] availablePorts = new int[3];
            for (int port = portStart; port < portEnd; port++) {
                try {
                    final ServerSocket server = new ServerSocket(port);
                    availablePorts[availableCount] = server.getLocalPort();
                    availableCount += 1;
                    server.close();
                    if (availableCount >= 3) {
                        break;
                    }
                } catch (IOException e) {
                    log.debug("Port {} is already used", port);
                }
            }

            log.info("This time server.port={} | management.port={} | dubbo.port = {}", availablePorts[0], availablePorts[1], availablePorts[2]);
            final PropertySource<?> apolloSource = environment.getPropertySources().get(PropertySourcesConstants.APOLLO_BOOTSTRAP_PROPERTY_SOURCE_NAME);
            if (apolloSource instanceof CompositePropertySource) {
                ((CompositePropertySource) apolloSource).addFirstPropertySource(new MapPropertySource(PlatformConstants.BOOTSTRAP_PROPERTY_SOURCE_NAME, ImmutableMap.of(
                        "server.port", availablePorts[0], "management.port", availablePorts[1], "dubbo.port", availablePorts[2])));
            }
            StartupConstants.SERVER_PORT = availablePorts[0];
            StartupConstants.DUBBO_PORT = availablePorts[2];
        } else {
            //测试时候使用
            log.info("Start Initializer Config env = {}, idc = {}", env, idc);
            StartupConstants.SERVER_PORT = Integer.parseInt(environment.getProperty("server.port", "20000"));
            StartupConstants.DUBBO_PORT = Integer.parseInt(environment.getProperty("dubbo.port", "20880"));
        }
    }
}
