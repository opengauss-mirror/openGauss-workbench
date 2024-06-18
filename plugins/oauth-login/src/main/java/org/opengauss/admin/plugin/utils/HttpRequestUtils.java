/*
 * Copyright (c) 2024 Huawei Technologies Co.,Ltd.
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
 * HttpRequestUtils.java
 *
 * IDENTIFICATION
 * oauth-login/src/main/java/org/opengauss/admin/plugin/utils/HttpRequestUtils.java
 *
 * -------------------------------------------------------------------------
 */

package org.opengauss.admin.plugin.utils;

import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.opengauss.admin.plugin.constants.MyConstants;
import org.springframework.util.StringUtils;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * @date 2024/6/13 14:38
 * @since 0.0
 */
@Slf4j
public class HttpRequestUtils {

    public static final String POST = "POST";
    public static final String DELETE = "DELETE";

    /**
     * send an HTTP request of the POST type
     *
     * @param url url
     * @param params params
     * @return String httpResponse
     */
    public static String postRequest(String url, Map<String, String> params) {
        return httpRequest(POST, url, null, null, null, params);
    }

    /**
     * send an HTTP request of the POST type
     *
     * @param url url
     * @param authorization Authorization
     * @param params params
     * @return String httpResponse
     */
    public static String postRequest(String url, String authorization, Map<String, String> params) {
        return httpRequest(POST, url, null, null, authorization, params);
    }

    /**
     * send an HTTP request of the DELETE type
     *
     * @param url url
     * @param authorization Authorization
     * @param params params
     * @return String httpResponse
     */
    public static String deleteRequest(String url, String authorization, Map<String, String> params) {
        return httpRequest(DELETE, url, null, null, authorization, params);
    }

    /**
     * send an HTTP request
     *
     * @param method http method
     * @param url url
     * @param contentType ContentType
     * @param accept Accept
     * @param authorization Authorization
     * @param params params
     * @return String httpResponse
     */
    public static String httpRequest(String method, String url, String contentType,
                                     String accept, String authorization, Map<String, String> params) {
        String responseResult = "";
        try {
            try {
                HttpSslUtils.setSSLCertByString();
                log.info("Get ssl trust by ssl-key.");
            } catch (RuntimeException exception) {
                log.error("Failed to establish mutual trust using the ssl-key.", exception);
                try {
                    HttpSslUtils.setSSLCert();
                } catch (FileNotFoundException e) {
                    log.error("Failed to establish mutual trust using the SSL certificate.", e);
                    HttpSslUtils.ignoreSslVerify();
                }
            }

            // Create a URL object
            URL obj = new URL(url);
            // Open connection
            HttpURLConnection con = (HttpURLConnection) obj.openConnection();

            // Set request method.
            con.setRequestMethod(method);

            // Add request header information.
            if (StringUtils.isEmpty(contentType)) {
                con.setRequestProperty("Content-Type", MyConstants.CONTENT_TYPE_JSON);
            } else {
                con.setRequestProperty("Content-Type", contentType);
            }
            if (StringUtils.isEmpty(accept)) {
                con.setRequestProperty("Accept", MyConstants.CONTENT_TYPE_JSON);
            } else {
                con.setRequestProperty("Accept", accept);
            }
            if (!StringUtils.isEmpty(authorization)) {
                con.setRequestProperty(MyConstants.AUTHORIZATION, authorization);
            }

            String requestBody = JSON.toJSONString(params);

            // Enable output stream.
            con.setDoOutput(true);

            // Gets the output stream object.
            try (DataOutputStream wr = new DataOutputStream(con.getOutputStream())) {
                // Write request body.
                wr.write(requestBody.getBytes(StandardCharsets.UTF_8));
            }

            // Gets the response status code.
            int responseCode = con.getResponseCode();
            log.info("Post Request to : " + url + ". Response Code : " + responseCode + ".");

            // Read response data.
            try (BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()))) {
                String inputLine;
                StringBuilder response = new StringBuilder();
                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                // Print response log.
                responseResult = response.toString();
                log.info("The request to : " + url + " has received response.");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return responseResult;
    }
}
