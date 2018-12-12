package net.dloud.platform.gateway.util;


import lombok.extern.slf4j.Slf4j;
import net.dloud.platform.common.network.IPConvert;
import net.dloud.platform.extend.bucket.Bandwidth;
import net.dloud.platform.extend.bucket.Bucket;
import net.dloud.platform.extend.bucket.SimpleBucket;
import net.dloud.platform.extend.constant.RequestHeaderEnum;
import org.springframework.web.reactive.function.server.ServerRequest;

import java.net.InetSocketAddress;
import java.time.Duration;
import java.util.List;

import static net.dloud.platform.extend.constant.PlatformConstants.CONFIG;

/**
 * @author QuDasheng
 * @create 2018-11-22 14:00
 **/
@Slf4j
public class LimitUtil {

    public static long convertIp(ServerRequest request) {
        return -((((long) Integer.MAX_VALUE) << 8) + remoteAddress(request));
    }

    public static Bucket localBucket(List<Bandwidth> bandwidths) {
        return SimpleBucket.build(bandwidths);
    }

    public static Bucket localBucket(Long key) {
        if (key <= 0) {
            return SimpleBucket.build(
                    Bandwidth.simple(CONFIG.getIntProperty(
                            "throttle.ip.second.limit", 10), Duration.ofSeconds(1)),
                    Bandwidth.simple(CONFIG.getIntProperty(
                            "throttle.ip.minute.limit", 100), Duration.ofMinutes(1)));
        } else {
            return SimpleBucket.build(
                    Bandwidth.simple(CONFIG.getIntProperty(
                            "throttle.user.second.limit", 10), Duration.ofSeconds(1)),
                    Bandwidth.simple(CONFIG.getIntProperty(
                            "throttle.user.minute.limit", 200), Duration.ofMinutes(1)));
        }
    }

    public static int remoteAddress(ServerRequest request) {
        final ServerRequest.Headers headers = request.headers();
        final List<String> realIP = headers.header(RequestHeaderEnum.X_REAL_IP.value());
        if (!realIP.isEmpty()) {
            return IPConvert.ip2Num(realIP.get(0));
        } else {
            final InetSocketAddress remoteAddress = headers.host();
            if (null == remoteAddress || null == remoteAddress.getAddress()) {
                return 0;
            } else {
                return IPConvert.ip2Num(remoteAddress.getAddress().getHostAddress());
            }
        }
    }
}