/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
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
 */

package org.opengauss.jsch.pool.records;

import org.opengauss.jsch.pool.enums.AuthType;
import org.opengauss.jsch.pool.enums.ChannelType;

import java.util.Objects;

/**
 * host user key
 *
 * @param host host
 * @param port port
 * @param username username
 * @param authType auth type
 * @param channelType channel type
 * @author: wangchao
 * @Date: 2025/8/4 14:04
 * @since 7.0.0-RC2
 */
public record HostUserKey(String host, int port, String username, AuthType authType, ChannelType channelType) {
    private static final int DEFAULT_SSH_PORT = 22;

    /**
     * constructor
     *
     * @param host host
     * @param port port
     * @param username username
     * @param authType auth type
     * @param channelType channel type
     */
    public HostUserKey {
        Objects.requireNonNull(host, "Host cannot be null");
        Objects.requireNonNull(username, "Username cannot be null");
        if (port < 1 || port > 65535) {
            throw new IllegalArgumentException("Port must be 1-65535");
        }
        if (authType == null) {
            authType = AuthType.UNKNOWN;
        }
        if (channelType == null) {
            channelType = ChannelType.UNKNOWN;
        }
    }

    /**
     * constructor
     *
     * @param host host
     * @param username username
     * @param authType auth type
     * @param channelType channel type
     */
    public HostUserKey(String host, String username, AuthType authType, ChannelType channelType) {
        this(host, DEFAULT_SSH_PORT, username, authType, channelType);
    }
}
