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
 * HttpUtils.java
 *
 * IDENTIFICATION
 * openGauss-visualtool/visualtool-common/src/main/java/org/opengauss/admin/common/utils/http/HttpUtils.java
 *
 * -------------------------------------------------------------------------
 */


package org.opengauss.admin.common.utils.http;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import lombok.Data;
import org.opengauss.admin.common.constant.Constants;
import org.opengauss.admin.common.core.domain.AjaxResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;

import javax.net.ssl.*;
import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.security.cert.X509Certificate;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Http Tool
 *
 * @author xielibo
 */
public class HttpUtils {
    private static final Logger log = LoggerFactory.getLogger(HttpUtils.class);

    private static Proxy httpProxy = null;

    /**
     * http proxy
     *
     * @param proxy proxy
     */
    public static void setProxy(Proxy proxy) {
        httpProxy = proxy;
    }

    /**
     * sendGet
     *
     * @param url   URL
     * @param param param，like name1=value1&name2=value2.
     */
    public static String sendGet(String url, String param) {
        return sendGet(url, param, Constants.UTF8);
    }

    /**
     * sendGet
     *
     * @param url   URL
     * @param param param，like name1=value1&name2=value2.
     * @param contentType contentType
     */
    public static String sendGet(String url, String param, String contentType) {
        StringBuilder result = new StringBuilder();
        BufferedReader in = null;
        InputStreamReader isr = null;
        try {
            String urlNameString = url + "?" + param;
            log.info("sendGet - {}", urlNameString);
            URL realUrl = new URL(urlNameString);
            URLConnection connection = realUrl.openConnection();
            connection.setRequestProperty("accept", "*/*");
            connection.setRequestProperty("connection", "Keep-Alive");
            connection.setRequestProperty("user-agent", "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1;SV1)");
            connection.setConnectTimeout(5000);
            connection.connect();
            isr = new InputStreamReader(connection.getInputStream(), contentType);
            in = new BufferedReader(isr);
            String line;
            while ((line = in.readLine()) != null) {
                result.append(line);
            }
            log.info("recv - {}", result);
        } catch (ConnectException e) {
            log.error("sendGet ConnectException, url=" + url + ",param=" + param, e);
        } catch (SocketTimeoutException e) {
            log.error("sendGet SocketTimeoutException, url=" + url + ",param=" + param, e);
        } catch (IOException e) {
            log.error("sendGet IOException, url=" + url + ",param=" + param, e);
        } catch (Exception e) {
            log.error("sendGet Exception, url=" + url + ",param=" + param, e);
        } finally {
            try {
                if (in != null) {
                    in.close();
                }
            } catch (Exception ex) {
                log.error("close Exception, url=" + url + ",param=" + param, ex);
            }
            if (isr != null) {
                try {
                    isr.close();
                } catch (IOException e) {
                    log.error("close input stream reader failed: " + e.getMessage());
                }
            }
        }
        return result.toString();
    }

    /**
     * sendPost
     *
     * @param url   URL
     * @param param param，like name1=value1&name2=value2.
     */
    public static PostResponse sendPost(String url, String param) {
        PrintWriter out = null;
        BufferedReader in = null;
        StringBuilder result = new StringBuilder();
        PostResponse response = new PostResponse();
        InputStreamReader isr = null;
        try {
            log.info("sendPost - {}", url);
            URL realUrl = new URL(url);
            HttpURLConnection conn = (HttpURLConnection) realUrl.openConnection();
            conn.setRequestProperty(HttpHeaders.ACCEPT, "*/*");
            conn.setRequestProperty(HttpHeaders.CONNECTION, "Keep-Alive");
            conn.setRequestProperty(HttpHeaders.USER_AGENT, "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1;SV1)");
            conn.setRequestProperty(HttpHeaders.ACCEPT_CHARSET, "utf-8");
            conn.setRequestProperty(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
            conn.setRequestMethod(HttpMethod.POST.name());
            conn.setConnectTimeout(0);
            conn.setReadTimeout(0);
            conn.setDoOutput(true);
            conn.setDoInput(true);
            out = new PrintWriter(conn.getOutputStream());
            out.print(param);
            out.flush();
            isr = new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8);
            in = new BufferedReader(isr);
            String line;
            while ((line = in.readLine()) != null) {
                result.append(line);
            }
            response.setBody(result.toString());
            response.setHeaders(conn.getHeaderFields());
            log.info("recv - {}", result);
        } catch (ConnectException e) {
            log.error("sendPost ConnectException, url=" + url + ",param=" + param, e);
        } catch (SocketTimeoutException e) {
            log.error("sendPost SocketTimeoutException, url=" + url + ",param=" + param, e);
        } catch (IOException e) {
            log.error("sendPost IOException, url=" + url + ",param=" + param, e);
        } catch (Exception e) {
            log.error("sendPost Exception, url=" + url + ",param=" + param, e);
        } finally {
            try {
                if (out != null) {
                    out.close();
                }
                if (in != null) {
                    in.close();
                }
                if (isr != null) {
                    isr.close();
                }
            } catch (IOException ex) {
                log.error("close Exception, url=" + url + ",param=" + param, ex);
            }
        }
        return response;
    }

    public static String sendSslPost(String url, String param) {
        StringBuilder result = new StringBuilder();
        String urlNameString = url + "?" + param;
        InputStreamReader isr = null;
        try {
            log.info("sendSSLPost - {}", urlNameString);
            SSLContext sc = SSLContext.getInstance("SSL");
            sc.init(null, new TrustManager[]{new TrustAnyTrustManager()}, new java.security.SecureRandom());
            URL console = new URL(urlNameString);
            HttpsURLConnection conn = (HttpsURLConnection) console.openConnection();
            conn.setRequestProperty("accept", "*/*");
            conn.setRequestProperty("connection", "Keep-Alive");
            conn.setRequestProperty("user-agent", "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1;SV1)");
            conn.setRequestProperty("Accept-Charset", "utf-8");
            conn.setRequestProperty("contentType", "utf-8");
            conn.setDoOutput(true);
            conn.setDoInput(true);

            conn.setSSLSocketFactory(sc.getSocketFactory());
            conn.setHostnameVerifier(new TrustAnyHostnameVerifier());
            conn.connect();
            InputStream is = conn.getInputStream();
            isr = new InputStreamReader(is);
            BufferedReader br = new BufferedReader(isr);
            String ret = "";
            while ((ret = br.readLine()) != null) {
                if (ret != null && !"".equals(ret.trim())) {
                    result.append(new String(ret.getBytes("ISO-8859-1"), "utf-8"));
                }
            }
            log.info("recv - {}", result);
            conn.disconnect();
            br.close();
        } catch (ConnectException e) {
            log.error("sendSSLPost ConnectException, url=" + url + ",param=" + param, e);
        } catch (SocketTimeoutException e) {
            log.error("sendSSLPost SocketTimeoutException, url=" + url + ",param=" + param, e);
        } catch (IOException e) {
            log.error("sendSSLPost IOException, url=" + url + ",param=" + param, e);
        } catch (Exception e) {
            log.error("sendSSLPost Exception, url=" + url + ",param=" + param, e);
        } finally {
            if (isr != null) {
                try {
                    isr.close();
                } catch (IOException e) {
                    log.error("close input stream reader failed: " + e.getMessage());
                }
            }
        }
        return result.toString();
    }

    /**
     * check url can be connected
     *
     * @param urlAddr url address
     * @return AjaxResult
     */
    public static AjaxResult checkUrl(String urlAddr) {
        try {
            if (StrUtil.isEmpty(urlAddr)) {
                return AjaxResult.error("The URL must not empty.");
            }
            URL url = new URL(urlAddr);
            URLConnection urlConnection = openConnectionWithProxy(url);
            if (urlConnection instanceof HttpsURLConnection) {
                HttpsURLConnection connection = (HttpsURLConnection) urlConnection;
                connection.setRequestMethod("GET");
                connection.connect();
                int responseCode = connection.getResponseCode();
                if (responseCode == 200) {
                    return AjaxResult.success("The URL is connected successfully.");
                } else {
                    return AjaxResult.error("The URL is not connected.");
                }
            } else {
                return AjaxResult.error("The URL is not https connected.");
            }
        } catch (IOException e) {
            log.error("connected {} error ", urlAddr, e);
            return AjaxResult.error("The URL is not connected : " + e.getMessage());
        }
    }

    private static URLConnection openConnectionWithProxy(URL url) throws IOException {
        if (Objects.isNull(httpProxy)) {
            return url.openConnection();
        } else {
            return url.openConnection(httpProxy);
        }
    }

    private static class TrustAnyTrustManager implements X509TrustManager {
        @Override
        public void checkClientTrusted(X509Certificate[] chain, String authType) {
        }

        @Override
        public void checkServerTrusted(X509Certificate[] chain, String authType) {
        }

        @Override
        public X509Certificate[] getAcceptedIssuers() {
            return new X509Certificate[]{};
        }
    }

    private static class TrustAnyHostnameVerifier implements HostnameVerifier {
        @Override
        public boolean verify(String hostname, SSLSession session) {
            return true;
        }
    }

    @Data
    public static class PostResponse {
        private String body;
        private Map<String, List<String>> headers;

        public String getHeader(String key) {
            if (CollUtil.isEmpty(headers)) {
                return "";
            }
            List<String> value = headers.get(key);
            if (CollUtil.isEmpty(value)) {
                return "";
            }
            return value.get(0);
        }
    }
}
