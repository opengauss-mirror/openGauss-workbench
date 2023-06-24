package com.nctigba.observability.sql.controller;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.gitee.starblues.bootstrap.annotation.AutowiredType;
import com.gitee.starblues.bootstrap.annotation.AutowiredType.Type;
import com.nctigba.observability.sql.mapper.NctigbaEnvMapper;
import com.nctigba.observability.sql.model.NctigbaEnv;
import com.nctigba.observability.sql.service.ClusterManager;
import org.opengauss.admin.common.core.domain.entity.ops.OpsHostEntity;
import org.opengauss.admin.common.core.domain.model.ops.OpsClusterVO;
import org.opengauss.admin.system.plugin.facade.HostFacade;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/observability/v1/environment")
public class EnvironmentController {
    @Autowired
    private NctigbaEnvMapper envMapper;
    @Autowired
    @AutowiredType(Type.PLUGIN_MAIN)
    private HostFacade hostFacade;
    @Autowired
    private ClusterManager clusterManager;

    @GetMapping("/basePath")
    public String basePath() {
        String path = System.getProperty("java.class.path");
        int firstIndex = path.lastIndexOf(System.getProperty("path.separator")) + 1;
        int lastIndex = path.lastIndexOf(File.separator) + 1;
        return path.substring(firstIndex, lastIndex);
    }

    @GetMapping("/agent")
    public List<OpsClusterVO> listAgent() {
        var env = envMapper.selectList(Wrappers.<NctigbaEnv>lambdaQuery().in(NctigbaEnv::getType, List.of(
                NctigbaEnv.envType.AGENT)));
        var clusters = clusterManager.getAllOpsCluster();
        env.forEach(e -> {
            if (e.getNodeid() == null) {
                clusters.forEach(c -> {
                    for (var node : c.getClusterNodes()) {
                        if (node.getHostId().equals(e.getHostid())) {
                            e.setNodeid(node.getNodeId());
                            return;
                        }
                    }
                });
                if (e.getNodeid() != null) {
                    envMapper.updateById(e);
                }
            }
        });
        var hosts = env.stream().map(NctigbaEnv::getNodeid).collect(Collectors.toSet());
        return clusters.stream().filter(c -> {
            var nodes = c.getClusterNodes().stream().filter(n -> {
                return hosts.contains(n.getNodeId());
            }).collect(Collectors.toList());
            c.setClusterNodes(nodes);
            return nodes.size() > 0;
        }).collect(Collectors.toList());
    }

    @GetMapping("/hosts")
    public List<OpsHostEntity> hosts() {
        return hostFacade.listAll();
    }
}