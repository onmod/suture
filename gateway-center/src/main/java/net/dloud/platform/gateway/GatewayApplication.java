package net.dloud.platform.gateway;

import com.alibaba.dubbo.config.spring.context.annotation.DubboComponentScan;
import net.dloud.platform.extend.constant.PlatformConstants;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

/**
 * @author QuDasheng
 * @create 2018-06-17 14:45
 **/
@RestController
@DubboComponentScan(basePackages = PlatformConstants.PLATFORM_PACKAGE)
@SpringBootApplication(scanBasePackages = PlatformConstants.PLATFORM_PACKAGE)
public class GatewayApplication {

    public static void main(String[] args) {
        SpringApplication.run(GatewayApplication.class, args);
    }


    @RequestMapping({"", "/"})
    public Mono<String> index() {
        return Mono.just("hello");
    }
}
