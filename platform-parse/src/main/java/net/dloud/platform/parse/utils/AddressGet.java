package net.dloud.platform.parse.utils;

import lombok.extern.slf4j.Slf4j;

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * @author QuDasheng
 * @create 2018-09-02 18:25
 **/
@Slf4j
public class AddressGet {
    public static InetAddress getLocal() {
        try {
            final InetAddress address = InetAddress.getLocalHost();
            log.info("获取本机网络信息: {}", address);
            if (null != address) {
                return address;
            }
        } catch (UnknownHostException e) {
            log.info("获取本机网络信息失败: {}", e.getMessage());
        }
        return null;
    }

    public static InetAddress getByName(String domain) {
        try {
            final InetAddress address = InetAddress.getByAddress(domain.getBytes());
            log.info("获取[{}]网络信息: {}", domain, address);
            return address;
        } catch (UnknownHostException e) {
            log.info("获取[{}]网络信息失败: {}", domain, e.getMessage());
        }
        return null;
    }
}
