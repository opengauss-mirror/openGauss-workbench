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
