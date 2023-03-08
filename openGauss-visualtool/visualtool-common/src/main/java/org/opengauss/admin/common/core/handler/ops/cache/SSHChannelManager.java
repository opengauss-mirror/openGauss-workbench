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
