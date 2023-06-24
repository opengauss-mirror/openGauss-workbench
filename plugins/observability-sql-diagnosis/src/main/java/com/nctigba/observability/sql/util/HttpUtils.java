/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package com.nctigba.observability.sql.util;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;

/**
 * General http sending method
 *
 * @author xielibo
 */
public class HttpUtils {
    private static final Logger log = LoggerFactory.getLogger(HttpUtils.class);

    /**
     * Send a request for the GET method to the specified URL
     *
     * @param url   URL
     * @param param Request parameters, which should be in the form of name1=value1&name2=value2.
     * @return Response result of the remote resource represented
     */
    public static String sendGet(String url, String param) {
        return sendGet(url, param, "UTF-8");
    }

    /**
     * Send a request for the GET method to the specified URL
     *
     * @param url     URL
     * @param param   Request parameters, which should be in the form of name1=value1&name2=value2.
     * @param contentType Encoding type
     * @return Response result of the remote resource represented
     */
    public static String sendGet(String url, String param, String contentType) {
        StringBuilder result = new StringBuilder();
        BufferedReader in = null;
        try {
            String urlNameString = StringUtils.isEmpty(param) ? url : url + "?" + param;
            log.info("sendGet - {}", urlNameString);
            URL realUrl = new URL(urlNameString);
            URLConnection connection = realUrl.openConnection();
            connection.setRequestProperty("accept", "*/*");
            connection.setRequestProperty("connection", "Keep-Alive");
            connection.setRequestProperty("user-agent", "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1;SV1)");
            connection.connect();
            in = new BufferedReader(new InputStreamReader(connection.getInputStream(), contentType));
            String line;
            while ((line = in.readLine()) != null) {
                result.append(line);
            }
            log.info("recv - {}", result);
        } catch (IOException e) {
            log.error("use HttpsUtil.sendGet Exception, url=" + url + ",param=" + param, e);
        } finally {
            try {
                if (in != null) {
                    in.close();
                }
            } catch (IOException ex) {
                log.error("use in.close Exception, url=" + url + ",param=" + param, ex);
            }
        }
        return result.toString();
    }
}