/*
 * Copyright (c) 2022 Huawei Technologies Co.,Ltd.
 *
 * openGauss is licensed under Mulan PSL v2.
 * You can use this software according to the terms and conditions of the Mulan PSL v2.
 * You may obtain a copy of Mulan PSL v2 at:
 *
 * http://license.coscl.org.cn/MulanPSL2
 *
 * THIS SOFTWARE IS PROVIDED ON AN "AS IS" BASIS, WITHOUT WARRANTIES OF ANY KIND,
 * EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO NON-INFRINGEMENT,
 * MERCHANTABILITY OR FITFOR A PARTICULAR PURPOSE.
 * See the Mulan PSL v2 for more details.
 * -------------------------------------------------------------------------
 *
 * IpUtils.java
 *
 * IDENTIFICATION
 * openGauss-visualtool/visualtool-common/src/main/java/org/opengauss/admin/common/utils/ip/IpUtils.java
 *
 * -------------------------------------------------------------------------
 */


package org.opengauss.admin.common.utils.ip;

import org.apache.commons.net.util.SubnetUtils;
import org.apache.commons.validator.routines.InetAddressValidator;
import org.opengauss.admin.common.utils.StringUtils;
import org.opengauss.admin.common.utils.html.EscapeUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.servlet.http.HttpServletRequest;
import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * IP Tool
 *
 * @author xielibo
 */
public class IpUtils {
    private static final String IPV4 = "ipv4";
    private static final String IPV6 = "ipv6";

    private static final Logger log = LoggerFactory.getLogger(IpUtils.class);

    /**
     * Format the IP string to ensure the correct format ipv4 or ipv6 used in ip:port.
     *
     * @param ip The IP character string
     * @return The IP:port is formatted as [ip]:port for ipv6 and ip:port for ipv4
     */
    public static String formatIp(String ip) {
        String ipType = getIpType(ip);

        if (ipType.isEmpty()) {
            return "";
        }

        if (IPV4.equals(ipType)) {
            return ip;
        } else if (IPV6.equals(ipType)) {
            return "[" + ip + "]";
        } else {
            log.warn(ip + " is neither an IPv4 nor an IPv6 address.");
            return "";
        }
    }

    /**
     * getIpType
     *
     * @param ip ip
     * @return String
     */
    public static String getIpType(String ip) {
        try {
            InetAddress inetAddress = InetAddress.getByName(ip);
            if (inetAddress instanceof Inet4Address) {
                return IPV4;
            } else if (inetAddress instanceof Inet6Address) {
                return IPV6;
            } else {
                log.warn(ip + " is neither an IPv4 nor an IPv6 address.");
            }
        } catch (UnknownHostException e) {
            log.warn(ip + " is not a valid IP address.");
            return "";
        }
        return "";
    }

    public static String getIpAddr(HttpServletRequest request) {
        if (request == null) {
            return "unknown";
        }
        String ip = request.getHeader("x-forwarded-for");
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Forwarded-For");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }

        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        return "0:0:0:0:0:0:0:1".equals(ip) ? "127.0.0.1" : EscapeUtil.clean(ip);
    }

    public static boolean internalIp(String ip) {
        byte[] addr = textToNumericFormatV4(ip);
        return internalIp(addr) || "127.0.0.1".equals(ip);
    }

    private static boolean internalIp(byte[] addr) {
        if (StringUtils.isNull(addr) || addr.length < 2) {
            return false;
        }
        final byte b0 = addr[0];
        final byte b1 = addr[1];
        // 10.x.x.x/8
        final byte sectionOne = 0x0A;
        // 172.16.x.x/12
        final byte sectionTwo = (byte) 0xAC;
        final byte sectionThree = (byte) 0x10;
        final byte SectionFour = (byte) 0x1F;
        // 192.168.x.x/16
        final byte sectionFive = (byte) 0xC0;
        final byte sectionSix = (byte) 0xA8;
        switch (b0) {
            case sectionOne:
                return true;
            case sectionTwo:
                if (b1 >= sectionThree && b1 <= SectionFour) {
                    return true;
                }
            case sectionFive:
                switch (b1) {
                    case sectionSix:
                        return true;
                    default:
                        return false;
                }
            default:
                return false;
        }
    }

    /**
     * Convert IPv4 address to bytes
     *
     * @return byte
     */
    public static byte[] textToNumericFormatV4(String text) {
        if (text.length() == 0) {
            return null;
        }

        byte[] bytes = new byte[4];
        String[] elements = text.split("\\.", -1);
        try {
            long l;
            int i;
            switch (elements.length) {
                case 1:
                    l = Long.parseLong(elements[0]);
                    if ((l < 0L) || (l > 4294967295L)) {
                        return null;
                    }
                    bytes[0] = (byte) (int) (l >> 24 & 0xFF);
                    bytes[1] = (byte) (int) ((l & 0xFFFFFF) >> 16 & 0xFF);
                    bytes[2] = (byte) (int) ((l & 0xFFFF) >> 8 & 0xFF);
                    bytes[3] = (byte) (int) (l & 0xFF);
                    break;
                case 2:
                    l = Integer.parseInt(elements[0]);
                    if ((l < 0L) || (l > 255L)) {
                        return null;
                    }
                    bytes[0] = (byte) (int) (l & 0xFF);
                    l = Integer.parseInt(elements[1]);
                    if ((l < 0L) || (l > 16777215L)) {
                        return null;
                    }
                    bytes[1] = (byte) (int) (l >> 16 & 0xFF);
                    bytes[2] = (byte) (int) ((l & 0xFFFF) >> 8 & 0xFF);
                    bytes[3] = (byte) (int) (l & 0xFF);
                    break;
                case 3:
                    for (i = 0; i < 2; ++i) {
                        l = Integer.parseInt(elements[i]);
                        if ((l < 0L) || (l > 255L)) {
                            return null;
                        }
                        bytes[i] = (byte) (int) (l & 0xFF);
                    }
                    l = Integer.parseInt(elements[2]);
                    if ((l < 0L) || (l > 65535L)) {
                        return null;
                    }
                    bytes[2] = (byte) (int) (l >> 8 & 0xFF);
                    bytes[3] = (byte) (int) (l & 0xFF);
                    break;
                case 4:
                    for (i = 0; i < 4; ++i) {
                        l = Integer.parseInt(elements[i]);
                        if ((l < 0L) || (l > 255L)) {
                            return null;
                        }
                        bytes[i] = (byte) (int) (l & 0xFF);
                    }
                    break;
                default:
                    return null;
            }
        } catch (NumberFormatException e) {
            return null;
        }
        return bytes;
    }

    public static String getHostIp() {
        try {
            return InetAddress.getLocalHost().getHostAddress();
        } catch (UnknownHostException e) {
        }
        return "127.0.0.1";
    }

    public static String getHostName() {
        try {
            return InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException e) {
        }
        return "Unknown";
    }

    public static boolean mayBeIPAddress(String hostIp) {
        return InetAddressValidator.getInstance().isValid(hostIp);
    }

    /**
     * <pre>
     * Get local IP address and mask (Ip,Mask).
     * if the local IP address is invalid, return empty. multiple IPs are supported.
     * "192.168.1.20", "255.255.255.0"
     * "2001:db8:abcd:0012::2", "64"
     * </pre>
     *
     * @return Map<String, String>
     */
    public static Map<String, String> getLocalIpAddressMap() {
        Map<String, String> ipMaskMap = new HashMap<>();
        try {
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            while (interfaces.hasMoreElements()) {
                NetworkInterface ni = interfaces.nextElement();
                if (ni.isLoopback() || !ni.isUp()) {
                    continue;
                }
                for (InterfaceAddress ia : ni.getInterfaceAddresses()) {
                    InetAddress addr = ia.getAddress();
                    if (addr instanceof Inet4Address) {
                        int prefix = ia.getNetworkPrefixLength();
                        String mask = calculateIPv4Mask(prefix);
                        ipMaskMap.put(addr.getHostAddress(), mask);
                    }
                    if (addr instanceof Inet6Address && !addr.isLinkLocalAddress()) {
                        ipMaskMap.put(addr.getHostAddress(), String.valueOf(ia.getNetworkPrefixLength()));
                    }
                }
            }
        } catch (SocketException e) {
            log.error("Failed to load DataKit Server Network IP", e);
        }
        return ipMaskMap;
    }

    private static String calculateIPv4Mask(int prefix) {
        int mask = 0xffffffff << (32 - prefix);
        return String.format(Locale.getDefault(), "%d.%d.%d.%d", (mask >> 24) & 0xff, (mask >> 16) & 0xff,
            (mask >> 8) & 0xff, mask & 0xff);
    }

    /**
     * <pre>
     * check ip1 and ip2 is in the same subnet
     * // IPv4 测试
     * (isIpInSubnet("192.168.1.10", "192.168.1.20", "255.255.255.0")); // 同子网
     * (isIpInSubnet("192.168.1.10", "192.168.2.20", "255.255.255.0")); // 不同子网
     *
     * // IPv6 测试
     * (isIpInSubnet("2001:db8:abcd:0012::1", "2001:db8:abcd:0012::2", "64"));
     * (isIpInSubnet("2001:db8:abcd:0012::1", "2001:db8:1234:5678::1", "64"));
     * </pre>
     *
     * @param ip1 ip1 eg. 192.168.1.10
     * @param ip2 ip2 eg. 192.168.1.20
     * @param mask mask eg. 255.255.255.0
     * @return true or false
     * @throws UnknownHostException UnknownHostException
     */
    public static boolean isIpInSubnet(String ip1, String ip2, String mask) throws UnknownHostException {
        if (mask.contains(".")) {
            SubnetUtils subnet = new SubnetUtils(ip1, mask);
            return subnet.getInfo().isInRange(ip2);
        } else {
            int prefixLength = Integer.parseInt(mask);
            return isSameSubnetV6(ip1, ip2, prefixLength);
        }
    }

    private static boolean isSameSubnetV6(String ip1, String ip2, int prefixLength) throws UnknownHostException {
        if (Inet6Address.getByName(ip1) instanceof Inet6Address addr1 && Inet6Address.getByName(
            ip2) instanceof Inet6Address addr2) {
            byte[] a1 = addr1.getAddress();
            byte[] a2 = addr2.getAddress();
            int bytes = prefixLength / 8;
            int bits = prefixLength % 8;
            for (int i = 0; i < bytes; i++) {
                if (a1[i] != a2[i]) {
                    return false;
                }
            }
            if (bits > 0) {
                int mask = (0xFF << (8 - bits)) & 0xFF;
                int b1 = a1[bytes] & 0xFF; // 转换为无符号
                int b2 = a2[bytes] & 0xFF;
                return (b1 & mask) == (b2 & mask);
            } else {
                return true;
            }
        } else {
            return false;
        }
    }
}
