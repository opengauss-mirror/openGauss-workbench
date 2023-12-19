/*
 *  Copyright (c) GBA-NCTI-ISDC. 2022-2024.
 *
 *  openGauss DataKit is licensed under Mulan PSL v2.
 *  You can use this software according to the terms and conditions of the Mulan PSL v2.
 *  You may obtain a copy of Mulan PSL v2 at:
 *
 *  http://license.coscl.org.cn/MulanPSL2
 *
 *  THIS SOFTWARE IS PROVIDED ON AN "AS IS" BASIS, WITHOUT WARRANTIES OF ANY KIND,
 *  EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO NON-INFRINGEMENT,
 *  MERCHANTABILITY OR FITFOR A PARTICULAR PURPOSE.
 *  See the Mulan PSL v2 for more details.
 *  -------------------------------------------------------------------------
 *
 *  EnvironmentController.java
 *
 *  IDENTIFICATION
 *  plugins/observability-instance/
 *  src/main/java/com/nctigba/observability/instance/controller/EnvironmentController.java
 *
 *  -------------------------------------------------------------------------
 */

package com.nctigba.observability.instance.controller;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import com.nctigba.observability.instance.model.entity.AgentNodeRelationDO;
import com.nctigba.observability.instance.model.vo.InstalledAgentVO;
import com.nctigba.observability.instance.service.AgentNodeRelationService;
import org.opengauss.admin.common.core.domain.entity.ops.OpsHostEntity;
import org.opengauss.admin.common.core.domain.entity.ops.OpsHostUserEntity;
import org.opengauss.admin.common.core.domain.model.ops.OpsClusterVO;
import org.opengauss.admin.system.plugin.facade.HostFacade;
import org.opengauss.admin.system.plugin.facade.HostUserFacade;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.gitee.starblues.bootstrap.annotation.AutowiredType;
import com.gitee.starblues.bootstrap.annotation.AutowiredType.Type;
import com.nctigba.observability.instance.model.entity.NctigbaEnvDO;
import com.nctigba.observability.instance.model.entity.NctigbaEnvDO.envType;
import com.nctigba.observability.instance.mapper.NctigbaEnvMapper;
import com.nctigba.observability.instance.service.ClusterManager;
import com.nctigba.observability.instance.service.PrometheusService;

import cn.hutool.core.collection.CollectionUtil;

@RestController
@RequestMapping("/observability")
public class EnvironmentController {
    @Autowired
    private NctigbaEnvMapper envMapper;
    @Autowired
    @AutowiredType(Type.PLUGIN_MAIN)
    private HostFacade hostFacade;
    @Autowired
    @AutowiredType(Type.PLUGIN_MAIN)
    private HostUserFacade hostUserFacade;
    @Autowired
    private ClusterManager clusterManager;
    @Autowired
    private PrometheusService prometheusService;
    @Autowired
    private AgentNodeRelationService agentNodeRelationService;

    @GetMapping("/v1/environment/hostUser/{hostId}")
    public List<OpsHostUserEntity> hostUser(@PathVariable String hostId) {
        return hostUserFacade.listHostUserByHostId(hostId);
    }

    @GetMapping("/v1/environment/basePath")
    public String basePath() {
        var full = EnvironmentController.class.getResource(
                EnvironmentController.class.getSimpleName() + ".class");
        var path = full.getPath();
        int jarIndex = path.indexOf(".jar");
        int lastSlashIndex = path.lastIndexOf(File.separator, jarIndex);
        int preSlashIndex = path.lastIndexOf(File.separator, lastSlashIndex - 1);
        return path.substring("file:".length(), preSlashIndex + 1);
    }

    @GetMapping("/v1/environment/prometheus")
    public List<NctigbaEnvDO> listPrometheus() {
        List<NctigbaEnvDO> env =
                envMapper.selectList(Wrappers.<NctigbaEnvDO>lambdaQuery().eq(NctigbaEnvDO::getType, envType.PROMETHEUS));
        env.forEach(e -> {
            e.setHost(hostFacade.getById(e.getHostid()));
        });
        return env;
    }

    @GetMapping("/environment/api/v1/exporters")
    public List<InstalledAgentVO> listExporter() {
        List<InstalledAgentVO> result = new ArrayList<>();

        // get installed agents
        var envs = envMapper.selectList(Wrappers.<NctigbaEnvDO>lambdaQuery().in(NctigbaEnvDO::getType,
                List.of(envType.NODE_EXPORTER,
                        envType.OPENGAUSS_EXPORTER,
                        envType.EXPORTER)));
        result = envs.stream().map(env -> {
            InstalledAgentVO resultItem = new InstalledAgentVO();
            resultItem.setEnvId(env.getId());
            resultItem.setHostId(env.getHostid());
            resultItem.setUsername(env.getUsername());
            resultItem.setExporterPort(String.valueOf(env.getPort()));
            resultItem.setPath(env.getPath());
            return resultItem;
        }).collect(Collectors.toList());

        // get agent host info
        List<OpsHostEntity> hosts = hostFacade.listAll();
        result.forEach(installedAgentsVO -> {
            Optional<OpsHostEntity> host =
                    hosts.stream().filter(hostTemp -> hostTemp.getHostId().equals(installedAgentsVO.getHostId()))
                            .findFirst();
            if (host.isPresent()) {
                installedAgentsVO.setHostName(host.get().getName());
                installedAgentsVO.setHostPublicIp(host.get().getPublicIp());
            }
        });

        // get related cluster
        List<AgentNodeRelationDO> relations = agentNodeRelationService.list();
        List<OpsClusterVO> clusters = clusterManager.getAllOpsCluster();
        result.forEach(installedAgentsVO -> {
            List<String> agentRelatedNodeIds = relations.stream()
                    .filter(relationTemp -> relationTemp.getEnvId().equals(installedAgentsVO.getEnvId()))
                    .map(z -> z.getNodeId()).collect(Collectors.toList());

            List<OpsClusterVO> relatedClusters = clusters.stream()
                    .filter(z -> z.getClusterNodes()
                            .stream().anyMatch(node -> agentRelatedNodeIds.contains(node.getNodeId())))
                    .collect(Collectors.toList());

            // clear not related nodes
            relatedClusters.forEach(cluster -> {
                List relatedNodes = cluster.getClusterNodes().stream()
                        .filter(node -> agentRelatedNodeIds.contains(node.getNodeId()))
                        .collect(Collectors.toList());
                cluster.setClusterNodes(relatedNodes);
            });

            installedAgentsVO.getClusters().addAll(relatedClusters);
        });

        // clear no cluster result
        result.removeIf(z -> z.getClusters().isEmpty());

        return result;
    }

    @GetMapping("/v1/environment/hosts")
    public List<OpsHostEntity> hosts() {
        return hostFacade.listAll();
    }

    @GetMapping("/v1/environment/pkg")
    public Map<String, Object> listPkg(String key, String hostId) {
        Map<String, Object> map = new HashMap<>();
        var host = hostFacade.getById(hostId);
        LambdaQueryWrapper<NctigbaEnvDO> wrapper = Wrappers.<NctigbaEnvDO>lambdaQuery();
        boolean isPrometheus = "prometheus".equals(key);
        if (isPrometheus) wrapper.eq(NctigbaEnvDO::getType, envType.PROMETHEUS_PKG);
        else wrapper.in(NctigbaEnvDO::getType, envType.NODE_EXPORTER_PKG, envType.OPENGAUSS_EXPORTER_PKG);
        var envs = envMapper.selectList(wrapper);
        if (isPrometheus) {
            var pkg = PrometheusService.NAME + PrometheusService.arch(host.getCpuArch()) + PrometheusService.TAR;
            map.put("pkg", pkg);
            map.put("url", PrometheusService.PATH + pkg);
            if (CollectionUtil.isNotEmpty(envs)) {
                var env = envs.stream().filter(e -> e.getPath().endsWith(pkg)).findFirst().orElse(null);
                map.put("flag", env != null);
                if (env != null) {
                    map.put("src", env.getPath());
                }
            } else {
                map.put("flag", false);
            }
        }
        return map;
    }

    @PostMapping("/v1/environment/upload")
    public String upload(@RequestParam String name, MultipartFile pkg) throws IOException {
        var file = new File("pkg/" + name);
        var parent = file.getParentFile();
        if (!parent.exists()) parent.mkdirs();
        if (file.exists()) {
            Files.delete(file.toPath());
        }
        try {
            pkg.transferTo(file.getCanonicalFile());
            return "success";
        } catch (IllegalStateException | IOException e) {
            throw new org.opengauss.admin.common.exception.CustomException("upload fail", e);
        }
    }

    @PostMapping("/v1/environment/merge")
    public void mergeFiles(@RequestParam String name, Integer total) throws IOException {
        var env = new NctigbaEnvDO();
        env.setPath("localhost");
        var file = new File("pkg/" + name);
        var parent = file.getParentFile();
        if (!parent.exists()) parent.mkdirs();
        if (file.exists()) {
            Files.delete(file.toPath());
        }
        try {
            FileOutputStream outputStream = new FileOutputStream(file, true);
            byte[] byt = new byte[10 * 1024 * 1024];
            int len;
            for (int i = 0; i < total; i++) {
                try (var temp = new FileInputStream("pkg/" + name + "-" + i);) {
                    while ((len = temp.read(byt)) != -1) {
                        outputStream.write(byt, 0, len);
                    }
                }
            }
            outputStream.close();
            for (int i = 0; i < total; i++) {
                File clearFile = new File("pkg/" + name + "-" + i);
                clearFile.delete();
            }
            env.setPath(file.getCanonicalPath());
            if (name.startsWith("prometheus")) env.setType(envType.PROMETHEUS_PKG);
            else if (name.startsWith("node_exporter")) env.setType(envType.NODE_EXPORTER_PKG);
            else if (name.startsWith("opengauss_exporter")) env.setType(envType.OPENGAUSS_EXPORTER_PKG);
            prometheusService.save(env);
        } catch (IllegalStateException | IOException e) {
            throw new org.opengauss.admin.common.exception.CustomException("merge fail", e);
        }
    }
}