package org.opengauss.admin.plugin.domain.model.ops.cache;

import com.gitee.starblues.bootstrap.annotation.AutowiredType;
import org.opengauss.admin.common.core.domain.entity.ops.OpsHostEntity;
import org.opengauss.admin.common.core.domain.entity.ops.OpsHostUserEntity;
import org.opengauss.admin.common.exception.ops.OpsException;
import org.opengauss.admin.plugin.domain.model.ops.SSHBody;
import org.opengauss.admin.plugin.utils.JschUtil;
import org.opengauss.admin.system.service.ops.impl.EncryptionUtils;
import com.jcraft.jsch.ChannelShell;
import com.jcraft.jsch.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author lhf
 * @date 2022/8/11 20:37
 **/
@Component
public class SSHChannelManager {
    @Autowired
    private JschUtil jschUtil;
    @Autowired
    @AutowiredType(AutowiredType.Type.PLUGIN_MAIN)
    private EncryptionUtils encryptionUtils;

    private static final ConcurrentHashMap<String, ChannelShell> CHANNEL_CONTEXT = new ConcurrentHashMap<>();


    public Optional<ChannelShell> initChannelShell(SSHBody sshBody, OpsHostEntity opsHostEntity, OpsHostUserEntity rootUserEntity) {
        String key =  sshBody.getBusinessId();

        Session session = jschUtil.getSession(opsHostEntity.getPublicIp(), opsHostEntity.getPort(), rootUserEntity.getUsername(), encryptionUtils.decrypt(rootUserEntity.getPassword()))
                .orElseThrow(() -> new OpsException("Failed to establish session with host"));

        CHANNEL_CONTEXT.remove(key);
        return Optional.ofNullable(CHANNEL_CONTEXT.computeIfAbsent(key, p -> jschUtil.openChannelShell(session)));
    }

    public Optional<ChannelShell> getChannelShell(String businessId) {
        return Optional.ofNullable(CHANNEL_CONTEXT.get(businessId));
    }
}
