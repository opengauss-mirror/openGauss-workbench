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
 * SSHChannelManager.java
 *
 * IDENTIFICATION
 * openGauss-visualtool/visualtool-common/src/main/java/org/opengauss/admin/common/core/handler/ops/cache/SSHChannelManager.java
 *
 * -------------------------------------------------------------------------
 */


package org.opengauss.admin.common.core.handler.ops.cache;

import com.jcraft.jsch.ChannelShell;

import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author lhf
 * @date 2022/8/11 20:37
 **/
public class SSHChannelManager {
    private static final ConcurrentHashMap<String, ChannelShell> CHANNEL_CONTEXT = new ConcurrentHashMap<>();

    public static Optional<ChannelShell> getChannelShell(String businessId) {
        return Optional.ofNullable(CHANNEL_CONTEXT.get(businessId));
    }

    public static void registerChannelShell(String businessId, ChannelShell channelShell) {
        CHANNEL_CONTEXT.put(businessId,channelShell);
    }
}
