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
 * MyClientConfigLoader.java
 *
 * IDENTIFICATION
 * oauth-login/src/main/java/org/opengauss/admin/plugin/config/main/MyClientConfigLoader.java
 *
 * -------------------------------------------------------------------------
 */

package org.opengauss.admin.plugin.config.main;

import com.gitee.starblues.spring.environment.EnvironmentProvider;
import org.opengauss.admin.common.utils.StringUtils;
import org.opengauss.admin.plugin.constants.MyClientConfigConstants;
import org.opengauss.admin.plugin.exception.OauthLoginException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

/**
 * @date 2024/6/14 15:31
 * @since 0.0
 */
@Component
public class MyClientConfigLoader {
    @Autowired
    private EnvironmentProvider environmentProvider;

    /**
     * load my client config
     */
    @PostConstruct
    public void loadMyClientConfig() {
        String clientId = environmentProvider.getString("plugins.oauth-login.client-id");
        String clientSecret = environmentProvider.getString("plugins.oauth-login.client-secret");
        String datakitUrl = environmentProvider.getString("plugins.oauth-login.datakit-url");
        String devkitUrl = environmentProvider.getString("plugins.oauth-login.devkit-url");
        String sslKey = environmentProvider.getString("plugins.oauth-login.ssl-key");
        if (StringUtils.isEmpty(clientId) || StringUtils.isEmpty(clientSecret)
                || StringUtils.isEmpty(datakitUrl) || StringUtils.isEmpty(devkitUrl) || StringUtils.isEmpty(sslKey)) {
            throw new OauthLoginException(
                    "The configuration items required by the oauth-login plugin are not configured, "
                            + "or some configuration items are empty.");
        }

        MyClientConfigConstants.clientId = clientId;
        MyClientConfigConstants.clientSecret = clientSecret;
        MyClientConfigConstants.dataKitUrl = datakitUrl;
        MyClientConfigConstants.devKitUrl = devkitUrl;
        MyClientConfigConstants.sslKey = sslKey;
    }
}
