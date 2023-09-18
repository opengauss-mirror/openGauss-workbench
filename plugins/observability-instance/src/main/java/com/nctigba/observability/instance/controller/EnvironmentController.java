/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package com.nctigba.observability.instance.controller;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
import com.nctigba.observability.instance.entity.NctigbaEnv;
import com.nctigba.observability.instance.entity.NctigbaEnv.envType;
import com.nctigba.observability.instance.mapper.NctigbaEnvMapper;
import com.nctigba.observability.instance.service.ClusterManager;
import com.nctigba.observability.instance.service.PrometheusService;

import cn.hutool.core.collection.CollectionUtil;

@RestController
@RequestMapping("/observability/v1/environment")
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

    @GetMapping("/hostUser/{hostId}")
    public List<OpsHostUserEntity> hostUser(@PathVariable String hostId) {
        return hostUserFacade.listHostUserByHostId(hostId);
    }

    @GetMapping("/basePath")
    public String basePath() {
        var full = EnvironmentController.class.getResource(EnvironmentController.class.getSimpleName() + ".class");
        var path = full.getPath();
        int jarIndex = path.indexOf(".jar");
        int lastSlashIndex = path.lastIndexOf(File.separator, jarIndex);
        int preSlashIndex = path.lastIndexOf(File.separator, lastSlashIndex - 1);
        return path.substring("file:".length(), preSlashIndex + 1);
    }

    @GetMapping("/prometheus")
    public List<NctigbaEnv> listPrometheus() {
        List<NctigbaEnv> env = envMapper
                .selectList(Wrappers.<NctigbaEnv>lambdaQuery().eq(NctigbaEnv::getType, envType.PROMETHEUS));
        env.forEach(e -> {
            e.setHost(hostFacade.getById(e.getHostid()));
        });
        return env;
    }

    @GetMapping("/exporter")
    public List<OpsClusterVO> listExporter() {
        var env = envMapper.selectList(Wrappers.<NctigbaEnv>lambdaQuery().in(NctigbaEnv::getType,
                List.of(envType.NODE_EXPORTER, envType.OPENGAUSS_EXPORTER, envType.EXPORTER)));
        var installedNodes = env.stream().map(NctigbaEnv::getNodeid).collect(Collectors.toSet());
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
        return clusters.stream().filter(c -> {
            var nodes = c.getClusterNodes().stream().filter(n -> {
                return installedNodes.contains(n.getNodeId());
            }).collect(Collectors.toList());
            c.setClusterNodes(nodes);
            return nodes.size() > 0;
        }).collect(Collectors.toList());
    }

    @GetMapping("/hosts")
    public List<OpsHostEntity> hosts() {
        return hostFacade.listAll();
    }

    @GetMapping("/pkg")
    public Map<String, Object> listPkg(String key, String hostId) {
        Map<String, Object> map = new HashMap<>();
        var host = hostFacade.getById(hostId);
        LambdaQueryWrapper<NctigbaEnv> wrapper = Wrappers.<NctigbaEnv>lambdaQuery();
        boolean isPrometheus = "prometheus".equals(key);
        if (isPrometheus)
            wrapper.eq(NctigbaEnv::getType, envType.PROMETHEUS_PKG);
        else
            wrapper.in(NctigbaEnv::getType, envType.NODE_EXPORTER_PKG, envType.OPENGAUSS_EXPORTER_PKG);
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

    @PostMapping("/upload")
    public String upload(@RequestParam String name, MultipartFile pkg) throws IOException {
        var file = new File("pkg/" + name);
        var parent = file.getParentFile();
        if (!parent.exists())
            parent.mkdirs();
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

    @PostMapping("/merge")
    public void mergeFiles(@RequestParam String name, Integer total) throws IOException {
        var env = new NctigbaEnv();
        env.setPath("localhost");
        var file = new File("pkg/" + name);
        var parent = file.getParentFile();
        if (!parent.exists())
            parent.mkdirs();
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
            if (name.startsWith("prometheus"))
                env.setType(envType.PROMETHEUS_PKG);
            else if (name.startsWith("node_exporter"))
                env.setType(envType.NODE_EXPORTER_PKG);
            else if (name.startsWith("opengauss_exporter"))
                env.setType(envType.OPENGAUSS_EXPORTER_PKG);
            prometheusService.save(env);
        } catch (IllegalStateException | IOException e) {
            throw new org.opengauss.admin.common.exception.CustomException("merge fail", e);
        }
    }
}