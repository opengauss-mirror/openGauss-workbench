/*
 *  Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 *
 *   openGauss is licensed under Mulan PSL v2.
 *   You can use this software according to the terms and conditions of the Mulan PSL v2.
 *   You may obtain a copy of Mulan PSL v2 at:
 *
 *   http://license.coscl.org.cn/MulanPSL2
 *
 *   THIS SOFTWARE IS PROVIDED ON AN "AS IS" BASIS, WITHOUT WARRANTIES OF ANY KIND,
 *   EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO NON-INFRINGEMENT,
 *   MERCHANTABILITY OR FITFOR A PARTICULAR PURPOSE.
 *   See the Mulan PSL v2 for more details.
 */

package org.opengauss.agent.config;

import org.opengauss.agent.exception.AgentException;

import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

/**
 * AgentSSLContext
 *
 * @author: wangchao
 * @Date: 2025/10/21 16:09
 * @since 7.0.0-RC3
 **/
public class AgentSslContext {
    private static final TrustManager[] TRUST_MANAGERS = {new InsecureTrustManager()};

    /**
     * config and init ssl context
     *
     * @return ssl context
     * @throws NoSuchAlgorithmException NoSuchAlgorithmException
     * @throws KeyManagementException KeyManagementException
     */
    public static SSLContext configureSslContext() throws NoSuchAlgorithmException, KeyManagementException {
        SSLContext sslContext = SSLContext.getInstance("TLS");
        sslContext.init(null, TRUST_MANAGERS, new java.security.SecureRandom());
        return sslContext;
    }

    /**
     * get x509 trust manager
     *
     * @return X509TrustManager
     */
    public static X509TrustManager getX509TrustManager() {
        if (TRUST_MANAGERS != null && TRUST_MANAGERS[0] instanceof X509TrustManager x509TrustManager) {
            return x509TrustManager;
        }
        throw new AgentException("init trust manager failed");
    }

    private static class InsecureTrustManager implements X509TrustManager {
        @Override
        public void checkClientTrusted(X509Certificate[] chain, String authType) {
        }

        @Override
        public void checkServerTrusted(X509Certificate[] chain, String authType) {
        }

        @Override
        public X509Certificate[] getAcceptedIssuers() {
            return new X509Certificate[0];
        }
    }
}
