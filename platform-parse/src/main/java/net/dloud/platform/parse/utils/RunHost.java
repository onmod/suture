package net.dloud.platform.parse.utils;

import net.dloud.platform.extend.constant.PlatformConstants;

import java.net.InetAddress;

/**
 * @author QuDasheng
 * @create 2018-09-07 15:18
 **/
public class RunHost {
    public static InetAddress localHost = AddressGet.getLocal();

    public static boolean canUseDomain(String input) {
        if (null != input && input.equalsIgnoreCase(PlatformConstants.HOST)) {
            return true;
        } else {
            return false;
        }
    }

    public static boolean canUseDomain(InetAddress local, String hostname) {
        if (null != local && null != local.getHostName() && null != hostname
                && hostname.equalsIgnoreCase(local.getHostName())) {
            return true;
        } else {
            return false;
        }
    }
}
