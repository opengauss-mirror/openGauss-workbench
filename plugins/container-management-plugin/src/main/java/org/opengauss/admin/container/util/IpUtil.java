/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2024-2024. All rights reserved.
 *
 *  openGauss DataKit is licensed under Mulan PSL v2.
 *  You can use this software according to the terms and conditions of the Mulan PSL v2.
 *  You may obtain a copy of Mulan PSL v2 at:
 *
 *  http://license.coscl.org.cn/MulanPSL2
 *
 *  THIS SOFTWARE IS PROVIDED ON AN "AS IS" BASIS, WITHOUT WARRANTIES OF ANY KIND,
 *  EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO NON-INFRINGEMENT,
 *  MERCHANTABILITY OR FITFOR A PARTICULAR PURPOSE.
 *  See the Mulan PSL v2 for more details.
 *  -------------------------------------------------------------------------
 *
 *  IpUtil.java
 *
 *  IDENTIFICATION
 *  plugins/container-management-plugin/src/main/java/org/opengauss/admin/container/util/IpUtil.java
 *
 *  -------------------------------------------------------------------------
 */

package org.opengauss.admin.container.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.regex.Pattern;

/**
 * IP 操作，获取对象的IP地址等信息
 *
 * @author ix
 * @since 2024-5-16
 */
public class IpUtil {
    private static final Logger logger = LoggerFactory.getLogger(IpUtil.class);
    private static final Pattern PATTERN;
    private static final Pattern PT_IPV4;

    private static final String LOCAL_IP = "localhost";

    static {
        // ipv6
        PATTERN = Pattern.compile("^((([0-9A-Fa-f]{1,4}:){7}[0-9A-Fa-f]{1,4})|(([0-9A-Fa-f]{1,4}:){1,7}:)|("
                + "([0-9A-Fa-f]{1,4}:){6}:[0-9A-Fa-f]{1,4})|(([0-9A-Fa-f]{1,4}:){5}(:[0-9A-Fa-f]{1,4}){1,2})|("
                + "([0-9A-Fa-f]{1,4}:){4}(:[0-9A-Fa-f]{1,4}){1,3})|(([0-9A-Fa-f]{1,4}:){3}(:[0-9A-Fa-f]{1,4}){1,4})|("
                + "([0-9A-Fa-f]{1,4}:){2}(:[0-9A-Fa-f]{1,4}){1,5})|([0-9A-Fa-f]{1,4}:(:[0-9A-Fa-f]{1,4}){1,6})|(:"
                + "(:[0-9A-Fa-f]{1,4}){1,7})|(([0-9A-Fa-f]{1,4}:){6}(\\d|[1-9]\\d|1\\d{2}|2[0-4]\\d|25[0-5])(\\."
                + "(\\d|[1-9]\\d|1\\d{2}|2[0-4]\\d|25[0-5])){3})|(([0-9A-Fa-f]{1,4}:){5}:"
                + "(\\d|[1-9]\\d|1\\d{2}|2[0-4]\\d|25[0-5])(\\.(\\d|[1-9]\\d|1\\d{2}|2[0-4]\\d|25[0-5])){3})|("
                + "([0-9A-Fa-f]{1,4}:){4}(:[0-9A-Fa-f]{1,4}){0,1}:(\\d|[1-9]\\d|1\\d{2}|2[0-4]\\d|25[0-5])(\\."
                + "(\\d|[1-9]\\d|1\\d{2}|2[0-4]\\d|25[0-5])){3})|(([0-9A-Fa-f]{1,4}:){3}(:[0-9A-Fa-f]{1,4}){0,2}:"
                + "(\\d|[1-9]\\d|1\\d{2}|2[0-4]\\d|25[0-5])(\\.(\\d|[1-9]\\d|1\\d{2}|2[0-4]\\d|25[0-5])){3})|("
                + "([0-9A-Fa-f]{1,4}:){2}(:[0-9A-Fa-f]{1,4}){0,3}:(\\d|[1-9]\\d|1\\d{2}|2[0-4]\\d|25[0-5])(\\."
                + "(\\d|[1-9]\\d|1\\d{2}|2[0-4]\\d|25[0-5])){3})|([0-9A-Fa-f]{1,4}:(:[0-9A-Fa-f]{1,4}){0,4}:"
                + "(\\d|[1-9]\\d|1\\d{2}|2[0-4]\\d|25[0-5])(\\.(\\d|[1-9]\\d|1\\d{2}|2[0-4]\\d|25[0-5])){3})|(:"
                + "(:[0-9A-Fa-f]{1,4}){0,5}:(\\d|[1-9]\\d|1\\d{2}|2[0-4]\\d|25[0-5])(\\."
                + "(\\d|[1-9]\\d|1\\d{2}|2[0-4]\\d|25[0-5])){3}))$");
        // ipv4
        PT_IPV4 = Pattern.compile("^(\\d|[1-9]\\d|1\\d{2}|2[0-4]\\d|25[0-5])(\\."
                + "(\\d|[1-9]\\d|1\\d{2}|2[0-4]\\d|25[0-5])){3}$");
    }

    /**
     * 获取主机IP
     *
     * @return ip
     */
    public static String getHostIp() {
        InetAddress localAddress = null;
        try {
            localAddress = InetAddress.getLocalHost();
            return localAddress.getHostAddress();
        } catch (UnknownHostException e) {
            logger.warn("Failed to retriving local host ip address, cause: ", e);
        }
        logger.error(String.format("Could not get local host ip address, will user %s instead.", LOCAL_IP));
        return LOCAL_IP;
    }

    /**
     * 判断是否为IPv4
     *
     * @param addr address
     * @return boolean
     */
    public static boolean isIpv4Address(String addr) {
        try {
            final InetAddress inetAddress = InetAddress.getByName(addr);
            return inetAddress instanceof Inet4Address;
        } catch (UnknownHostException e) {
            return false;
        }
    }
}
