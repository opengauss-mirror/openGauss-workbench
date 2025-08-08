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

package org.opengauss.jsch.pool.config;

import com.jcraft.jsch.Session;

import lombok.Getter;

import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.opengauss.jsch.pool.enums.AuthType;
import org.opengauss.jsch.pool.enums.ChannelType;
import org.opengauss.jsch.pool.records.HostUserKey;

import java.util.Objects;

/**
 * SessionConfig
 *
 * @author: wangchao
 * @Date: 2025/8/4 14:04
 * @since 7.0.0-RC2
 **/
public class SessionConfig {
    @Getter
    private final String host;
    @Getter
    private final int port;
    @Getter
    private final String username;
    @Getter
    private final String password;
    @Getter
    private String privateKey;
    @Getter
    private String knownHosts;

    /**
     * default timeout 10s
     */
    @Getter
    private int timeout = 10000;
    @Getter
    private GenericObjectPoolConfig<Session> poolConfig;

    /**
     * default channel type exec
     */
    private ChannelType channelType = ChannelType.EXEC;
    private boolean isReuseSessionForMultipleChannels = false;

    /**
     * build session config , default port 22
     *
     * @param host host
     * @param username username
     * @param password password
     */
    public SessionConfig(String host, String username, String password) {
        this(host, 22, username, password);
    }

    /**
     * build session config
     *
     * @param host host
     * @param port port
     * @param username username
     * @param password password
     */
    public SessionConfig(String host, int port, String username, String password) {
        this.host = host;
        this.port = port;
        this.username = username;
        this.password = password;
    }

    /**
     * set private key
     *
     * @param privateKey private key
     * @return this
     */
    public SessionConfig withPrivateKey(String privateKey) {
        this.privateKey = privateKey;
        return this;
    }

    /**
     * set known hosts
     *
     * @param knownHosts known hosts
     * @return this
     */
    public SessionConfig withKnownHosts(String knownHosts) {
        this.knownHosts = knownHosts;
        return this;
    }

    /**
     * set timeout
     *
     * @param timeout timeout
     * @return this
     */
    public SessionConfig withTimeout(int timeout) {
        this.timeout = timeout;
        return this;
    }

    /**
     * set channel type
     *
     * @param channelType channel type
     * @return this
     */
    public SessionConfig withChannelType(ChannelType channelType) {
        if (!Objects.equals(channelType, ChannelType.EXEC) && !Objects.equals(channelType, ChannelType.SFTP)
            && !Objects.equals(channelType, ChannelType.SHELL)) {
            throw new IllegalArgumentException("Invalid channel type: " + channelType);
        }
        this.channelType = channelType;
        return this;
    }

    /**
     * get key
     *
     * @return key
     */
    public HostUserKey getKey() {
        AuthType authType = privateKey != null ? AuthType.KEY : AuthType.PASSWORD;
        return new HostUserKey(host, port, username, authType, channelType);
    }

    /**
     * set isAllow session reuse
     *
     * @param isAllow if true, session can be reused for multiple channels
     * @return this
     */
    public SessionConfig allowSessionReuse(boolean isAllow) {
        this.isReuseSessionForMultipleChannels = isAllow;
        return this;
    }

    /**
     * get allow session reuse
     *
     * @return if true, session can be reused for multiple channels
     */
    public boolean isSessionReuseAllowed() {
        return isReuseSessionForMultipleChannels;
    }
}
