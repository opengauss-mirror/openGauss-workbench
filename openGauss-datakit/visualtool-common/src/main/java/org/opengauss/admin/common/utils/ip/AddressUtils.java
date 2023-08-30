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
 * AddressUtils.java
 *
 * IDENTIFICATION
 * openGauss-visualtool/visualtool-common/src/main/java/org/opengauss/admin/common/utils/ip/AddressUtils.java
 *
 * -------------------------------------------------------------------------
 */


package org.opengauss.admin.common.utils.ip;

import com.alibaba.fastjson.JSONObject;
import org.opengauss.admin.common.constant.Constants;
import org.opengauss.admin.common.utils.StringUtils;
import org.opengauss.admin.common.utils.http.HttpUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Address Tool
 *
 * @author xielibo
 */
public class AddressUtils {
    private static final Logger log = LoggerFactory.getLogger(AddressUtils.class);

    public static final String IP_URL = "http://whois.pconline.com.cn/ipJson.jsp";

    public static final String UNKNOWN = "XX XX";

    public static String getRealAddressByIp(String ip) {
        String address = UNKNOWN;

        if (IpUtils.internalIp(ip)) {
            return "Intranet IP";
        }
        try {
            String rspStr = HttpUtils.sendGet(IP_URL, "ip=" + ip + "&json=true", Constants.GBK);
            if (StringUtils.isEmpty(rspStr)) {
                log.error("Get geographic location exception {}", ip);
                return UNKNOWN;
            }
            JSONObject obj = JSONObject.parseObject(rspStr);
            String region = obj.getString("pro");
            String city = obj.getString("city");
            return String.format("%s %s", region, city);
        } catch (Exception e) {
            log.error("Get geographic location exception {}", ip);
        }
        return address;
    }
}
