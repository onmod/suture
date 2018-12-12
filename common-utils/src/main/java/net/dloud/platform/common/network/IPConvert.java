package net.dloud.platform.common.network;

public class IPConvert {
    /**
     * 把IP按点号分4段，每段一整型就一个字节来表示，通过左移位来实现。
     * 第一段放到最高的8位，需要左移24位，依此类推即可
     *
     * @param ipStr ip地址
     * @return 整形
     */
    public static int ip2Num(String ipStr) {
        if (ipStr == null || "".equals(ipStr)) {
            return -1;
        }

        if (ipStr.contains(":")) {
            //ipv6的地址，不解析，返回127.0.0.1
            ipStr = "127.0.0.1";
        }

        String[] ips = ipStr.split("\\.");

        return (Integer.parseInt(ips[0]) << 24) + (Integer.parseInt(ips[1]) << 16) + (Integer.parseInt(ips[2]) << 8) + Integer.parseInt(ips[3]);
    }

    /**
     * 把整数分为4个字节，通过右移位得到IP地址中4个点分段的值
     *
     * @param ipNum ip int value
     * @return ip str
     */
    public static String num2Ip(int ipNum) {
        return ((ipNum >> 24) & 0xFF) + "." + ((ipNum >> 16) & 0xFF) + "." + ((ipNum >> 8) & 0xFF) + "." + (ipNum & 0xFF);
    }
}
