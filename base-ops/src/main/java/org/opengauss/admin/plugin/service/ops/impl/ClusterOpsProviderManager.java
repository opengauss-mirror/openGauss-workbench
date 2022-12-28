package org.opengauss.admin.plugin.service.ops.impl;

import org.opengauss.admin.plugin.enums.ops.OpenGaussSupportOSEnum;
import org.opengauss.admin.plugin.enums.ops.OpenGaussVersionEnum;
import org.opengauss.admin.plugin.service.ops.ClusterOpsProvider;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Cluster Installation Service Provider Manager
 *
 * @author lhf
 * @date 2022/8/12 09:08
 **/
@Component
public class ClusterOpsProviderManager {
    private static final ConcurrentHashMap<String, ClusterOpsProvider> REGISTRY = new ConcurrentHashMap<>();

    public static void registry(OpenGaussVersionEnum version, ClusterOpsProvider provider) {
        registry(version,OpenGaussSupportOSEnum.CENTOS_X86_64,provider);

    }

    public static void registry(OpenGaussVersionEnum version, OpenGaussSupportOSEnum os, ClusterOpsProvider provider) {
        REGISTRY.put(os.name() + version.name(), provider);
    }

    public Optional<ClusterOpsProvider> provider(OpenGaussVersionEnum version, OpenGaussSupportOSEnum os) {
        if (os == null){
            os = OpenGaussSupportOSEnum.CENTOS_X86_64;
        }
        return Optional.ofNullable(REGISTRY.get(os.name() + version.name()));
    }
}
