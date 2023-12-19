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
 *  plugins/observability-log-search/src/main/java/com/nctigba/observability/log/controller/EnvironmentController.java
 *
 *  -------------------------------------------------------------------------
 */

package com.nctigba.observability.log.controller;

import cn.hutool.core.collection.CollectionUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.gitee.starblues.bootstrap.annotation.AutowiredType;
import com.gitee.starblues.bootstrap.annotation.AutowiredType.Type;
import com.nctigba.observability.log.model.entity.NctigbaEnvDO;
import com.nctigba.observability.log.model.entity.NctigbaEnvDO.type;
import com.nctigba.observability.log.mapper.NctigbaEnvMapper;
import com.nctigba.observability.log.service.ClusterManager;
import com.nctigba.observability.log.service.ElasticsearchService;
import com.nctigba.observability.log.service.FilebeatService;
import org.opengauss.admin.common.core.domain.entity.ops.OpsHostEntity;
import org.opengauss.admin.common.core.domain.entity.ops.OpsHostUserEntity;
import org.opengauss.admin.common.core.domain.model.ops.OpsClusterVO;
import org.opengauss.admin.common.exception.CustomException;
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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
    @AutowiredType(Type.PLUGIN_MAIN)
    private HostUserFacade hostUserFacade;
    @Autowired
    private ClusterManager clusterManager;
    @Autowired
    private ElasticsearchService elasticsearchService;

    @GetMapping("/basePath")
    public String basePath() {
        var full = EnvironmentController.class.getResource(EnvironmentController.class.getSimpleName() + ".class");
        var path = full.getPath();
        int jarIndex = path.indexOf(".jar");
        int lastSlashIndex = path.lastIndexOf(File.separator, jarIndex);
        int preSlashIndex = path.lastIndexOf(File.separator, lastSlashIndex - 1);
        return path.substring("file:".length(), preSlashIndex + 1);
    }

    /**
     * Get log path
     *
     * @param nodeId log nodeId
     * @return String
     */
    @GetMapping("/logPath/{nodeId}")
    public String logPath(@PathVariable String nodeId) {
        String logPathSql = "select name,setting from pg_settings where name in ('data_directory','log_directory');";
        try (var conn = clusterManager.getConnectionByNodeId(nodeId);
             var st = conn.createStatement(); var rs = st.executeQuery(logPathSql)) {
            String prefixPath = "";
            String suffixPath = "";
            while (rs.next()) {
                if ("data_directory".equals(rs.getString(1))) {
                    prefixPath = rs.getString(2);
                } else {
                    suffixPath = rs.getString(2);
                }
            }
            return prefixPath + "/" + suffixPath;
        } catch (SQLException e) {
            e.printStackTrace();
            return e.getMessage();
        }
    }

    @GetMapping("/hostUser/{hostId}")
    public List<OpsHostUserEntity> hostUser(@PathVariable String hostId) {
        return hostUserFacade.listHostUserByHostId(hostId);
    }

    @GetMapping("/elasticsearch")
    public List<NctigbaEnvDO> listPrometheus() {
        List<NctigbaEnvDO> env = envMapper
                .selectList(Wrappers.<NctigbaEnvDO>lambdaQuery().eq(NctigbaEnvDO::getType, type.ELASTICSEARCH));
        env.forEach(e -> {
            e.setHost(hostFacade.getById(e.getHostid()));
        });
        return env;
    }

    @GetMapping("/filebeat")
    public List<OpsClusterVO> listExporter() {
        var env = envMapper
                .selectList(Wrappers.<NctigbaEnvDO>lambdaQuery().in(NctigbaEnvDO::getType, List.of(type.FILEBEAT)));
        var hosts = env.stream().map(NctigbaEnvDO::getNodeid).collect(Collectors.toSet());
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

    @GetMapping("/pkg")
    public Map<String, Object> listPkg(String key, String hostId) {
        Map<String, Object> map = new HashMap<>();
        var host = hostFacade.getById(hostId);
        LambdaQueryWrapper<NctigbaEnvDO> wrapper = Wrappers.<NctigbaEnvDO>lambdaQuery();
        boolean isElasticsearch = "elasticsearch".equals(key);
        wrapper.eq(NctigbaEnvDO::getType, isElasticsearch ? type.ELASTICSEARCH_PKG : type.FILEBEAT_PKG);
        var envs = envMapper.selectList(wrapper);
        String pkg;
        if (isElasticsearch) {
            pkg = ElasticsearchService.NAME + host.getCpuArch() + ElasticsearchService.TAR;
            map.put("pkg", pkg);
            map.put("url", ElasticsearchService.PATH + pkg);
        } else {
            String arch;
            switch (host.getCpuArch()) {
                case "aarch64":
                    arch = "arm64";
                    break;
                case "x86_64":
                    arch = "x86_64";
                    break;
                default:
                    arch = "x86_64";
            }
            pkg = FilebeatService.NAME + arch + FilebeatService.TAR;
            map.put("pkg", pkg);
            map.put("url", FilebeatService.PATH + pkg);
        }
        if (CollectionUtil.isNotEmpty(envs)) {
            var env = envs.stream().filter(e -> e.getPath().endsWith(pkg)).findFirst().orElse(null);
            map.put("flag", env != null);
            if (env != null) {
                map.put("src", env.getPath());
            }
        } else {
            map.put("flag", false);
        }
        return map;
    }

    @PostMapping("/upload")
    public String upload(@RequestParam String name, MultipartFile pkg) throws IOException {
        var file = new File("pkg/" + name);
        var parent = file.getParentFile();
        if (!parent.exists()) {
            parent.mkdirs();
        }
        if (file.exists()) {
            Files.delete(file.toPath());
        }
        try {
            pkg.transferTo(file.getCanonicalFile());
            return "success";
        } catch (IllegalStateException | IOException e) {
            throw new CustomException("upload fail", e);
        }
    }

    @PostMapping("/merge")
    public void mergeFiles(@RequestParam String name, Integer total) throws IOException {
        var env = new NctigbaEnvDO();
        env.setPath("localhost");
        var file = new File("pkg/" + name);
        var parent = file.getParentFile();
        if (!parent.exists()) {
            parent.mkdirs();
        }
        if (file.exists()) {
            Files.delete(file.toPath());
        }
        try {
            byte[] byt = new byte[10 * 1024 * 1024];
            int len;
            for (int i = 0; i < total; i++) {
                try (FileInputStream temp = new FileInputStream("pkg/" + name + "-" + i)) {
                    while ((len = temp.read(byt)) != -1) {
                        try (FileOutputStream outputStream = new FileOutputStream(file, true)) {
                            outputStream.write(byt, 0, len);
                        }
                    }
                }
            }
            for (int i = 0; i < total; i++) {
                File clearFile = new File("pkg/" + name + "-" + i);
                clearFile.delete();
            }
            env.setPath(file.getCanonicalPath());
            if (name.startsWith("elasticsearch")) {
                env.setType(type.ELASTICSEARCH_PKG);
            } else if (name.startsWith("filebeat")) {
                env.setType(type.FILEBEAT_PKG);
            }
            elasticsearchService.save(env);
        } catch (IllegalStateException | IOException e) {
            throw new CustomException("merge fail", e);
        }
    }
}