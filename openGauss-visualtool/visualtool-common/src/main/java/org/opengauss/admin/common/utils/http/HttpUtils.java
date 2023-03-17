package org.opengauss.admin.common.utils.http;

import org.opengauss.admin.common.constant.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;

import javax.net.ssl.*;
import java.io.*;
import java.net.*;
import java.security.cert.X509Certificate;

/**
 * Http Tool
 *
 * @author xielibo
 */
public class HttpUtils {
    private static final Logger log = LoggerFactory.getLogger(HttpUtils.class);

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
        try {
            String urlNameString = url + "?" + param;
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
        }
        return result.toString();
    }

    /**
     * sendPost
     *
     * @param url   URL
     * @param param param，like name1=value1&name2=value2.
     */
    public static String sendPost(String url, String param) {
        PrintWriter out = null;
        BufferedReader in = null;
        StringBuilder result = new StringBuilder();
        try {
            String urlNameString = url;
            log.info("sendPost - {}", urlNameString);
            URL realUrl = new URL(urlNameString);
            HttpURLConnection conn = (HttpURLConnection) realUrl.openConnection();
            conn.setRequestProperty(HttpHeaders.ACCEPT, "*/*");
            conn.setRequestProperty(HttpHeaders.CONNECTION, "Keep-Alive");
            conn.setRequestProperty(HttpHeaders.USER_AGENT, "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1;SV1)");
            conn.setRequestProperty(HttpHeaders.ACCEPT_CHARSET, "utf-8");
            conn.setRequestProperty(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
            conn.setRequestMethod(HttpMethod.POST.name());
            conn.setDoOutput(true);
            conn.setDoInput(true);
            out = new PrintWriter(conn.getOutputStream());
            out.print(param);
            out.flush();
            in = new BufferedReader(new InputStreamReader(conn.getInputStream(), "utf-8"));
            String line;
            while ((line = in.readLine()) != null) {
                result.append(line);
            }
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
            } catch (IOException ex) {
                log.error("close Exception, url=" + url + ",param=" + param, ex);
            }
        }
        return result.toString();
    }

    public static String sendSslPost(String url, String param) {
        StringBuilder result = new StringBuilder();
        String urlNameString = url + "?" + param;
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
            BufferedReader br = new BufferedReader(new InputStreamReader(is));
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
        }
        return result.toString();
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
}