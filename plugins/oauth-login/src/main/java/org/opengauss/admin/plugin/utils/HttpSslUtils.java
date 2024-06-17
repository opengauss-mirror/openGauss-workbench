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
 * HttpSslUtils.java
 *
 * IDENTIFICATION
 * oauth-login/src/main/java/org/opengauss/admin/plugin/utils/HttpSslUtils.java
 *
 * -------------------------------------------------------------------------
 */

package org.opengauss.admin.plugin.utils;

import org.opengauss.admin.plugin.constants.MyClientConfigConstants;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;
import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;

/**
 * @date 2024/5/31 16:41
 * @since 0.0
 */
public class HttpSslUtils {
    /**
     * ignore ssl authentication
     */
    public static void ignoreSslVerify() {
        try {
            // Ignore SSL authentication.
            // Create a TrustManager that trusts all certificates.
            TrustManager[] trustAllCerts = new TrustManager[]{
                    new X509TrustManager() {
                        public X509Certificate[] getAcceptedIssuers() {
                            return null;
                        }

                        public void checkClientTrusted(X509Certificate[] certs, String authType) {
                        }

                        public void checkServerTrusted(X509Certificate[] certs, String authType) {
                        }
                    }
            };

            // Setting SSL context.
            SSLContext sc = null;
            sc = SSLContext.getInstance("SSL");
            sc.init(null, trustAllCerts, new SecureRandom());
            HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
        } catch (NoSuchAlgorithmException | KeyManagementException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * set ssl certificate
     */
    public static void setSSLCert() throws FileNotFoundException {
        try {
            // Configuring a Certificate (Public Key).
            CertificateFactory cf = CertificateFactory.getInstance("X.509");
            Certificate cert = cf.generateCertificate(new FileInputStream("./oauth.crt"));

            // Store certificate.
            KeyStore ks = KeyStore.getInstance(KeyStore.getDefaultType());
            // Load the existing keyStore factory.
            ks.load(null, null);
            ks.setCertificateEntry("cert", cert);

            // Configuring a trusted Certificate.
            TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            tmf.init(ks);

            SSLContext sc = SSLContext.getInstance("TLS");
            sc.init(null, tmf.getTrustManagers(), null);

            HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
        } catch (FileNotFoundException e) {
            throw new FileNotFoundException("The certificate does not exist.");
        } catch (CertificateException | KeyManagementException | KeyStoreException | IOException |
                 NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * set ssl certificate by certificate string
     */
    public static void setSSLCertByString() {
        try {
            // Configuring a Certificate (Public Key).
            CertificateFactory cf = CertificateFactory.getInstance("X.509");
            Certificate cert = cf.generateCertificate(
                    new ByteArrayInputStream(parseSslKey().getBytes(StandardCharsets.UTF_8)));

            // Store certificate.
            KeyStore ks = KeyStore.getInstance(KeyStore.getDefaultType());
            // Load the existing keyStore factory.
            ks.load(null, null);
            ks.setCertificateEntry("cert", cert);

            // Configuring a trusted Certificate.
            TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            tmf.init(ks);

            SSLContext sc = SSLContext.getInstance("TLS");
            sc.init(null, tmf.getTrustManagers(), null);

            HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
        } catch (CertificateException | KeyManagementException | KeyStoreException | IOException |
                 NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * parse certificate string
     *
     * @return String certificate string
     */
    private static String parseSslKey() {
        String sslKey = MyClientConfigConstants.sslKey;
        StringBuilder formattedCertStr = new StringBuilder();
        formattedCertStr.append("-----BEGIN CERTIFICATE-----").append((char) 10);

        int index = 0;
        while (index < sslKey.length()) {
            // If the number of remaining characters is greater than 64, add 64 characters and add a new line.
            if (sslKey.length() - index > 64) {
                formattedCertStr.append(sslKey, index, index + 64).append((char) 10);
            } else {
                // Otherwise, only the remaining characters are added.
                formattedCertStr.append(sslKey.substring(index)).append((char) 10);
            }
            index += 64;
        }

        formattedCertStr.append("-----END CERTIFICATE-----");
        return formattedCertStr.toString();
    }
}
