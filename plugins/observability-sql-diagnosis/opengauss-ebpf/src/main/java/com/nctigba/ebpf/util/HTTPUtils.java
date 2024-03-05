/*
 *  Copyright (c) GBA-NCTI-ISDC. 2022-2024.
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
 *  HTTPUtils.java
 *
 *  IDENTIFICATION
 *  plugins/observability-sql-diagnosis/opengauss-ebpf/src/main/java/com/nctigba/ebpf/util/HTTPUtils.java
 *
 *  -------------------------------------------------------------------------
 */

package com.nctigba.ebpf.util;


import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.io.IOException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;

/**
 * http util
 *
 * @author luomeng@ncti-gba.cn
 * @since 2022/10/17 09:00
 */
@Slf4j
public class HTTPUtils {
    /**
     * http httpUrlPost
     *
     * @param url  String
     * @param file FileSystemResource
     * @param type String
     * @return boolean
     */
    public boolean httpUrlPost(String url, FileSystemResource file,
            String type) {
        TrustManager[] trustAllCerts = new TrustManager[]{new X509TrustManager() {
            /**
             * Get accepted issuers
             *
             * @return X509Certificate
             */
            public X509Certificate[] getAcceptedIssuers() {
                return new X509Certificate[0];
            }

            /**
             * Check client trusted
             *
             * @param certs  X509Certificate
             * @param authType String
             */
            public void checkClientTrusted(X509Certificate[] certs, String authType) {
                log.info("Check client trusted");
            }

            /**
             * Check server trusted
             *
             * @param certs  X509Certificate
             * @param authType String
             */
            public void checkServerTrusted(X509Certificate[] certs, String authType) {
                log.info("Check server trusted");
            }
        }};
        SSLContext sc = null;
        try {
            sc = SSLContext.getInstance("SSL");
            sc.init(null, trustAllCerts, new java.security.SecureRandom());
        } catch (NoSuchAlgorithmException | KeyManagementException e) {
            log.error(e.getMessage());
        }
        HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
        HttpsURLConnection.setDefaultHostnameVerifier((hostname, session) -> true);
        final RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        MultiValueMap<String, Object> map = new LinkedMultiValueMap<>();
        map.add("type", type);
        map.add("file", file);
        HttpEntity<MultiValueMap<String, Object>> request = new HttpEntity<>(map, headers);
        ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);
        try {
            log.info(type + ":" + response.getStatusCode() + ":" + response.getBody() + ":" + file.contentLength());
            return true;
        } catch (IOException e) {
            log.error(e.getMessage());
            return true;
        }
    }
}

