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
import com.nctigba.observability.log.mapper.NctigbaEnvMapper;
import com.nctigba.observability.log.model.entity.NctigbaEnvDO;
import com.nctigba.observability.log.model.entity.NctigbaEnvDO.InstallType;
import com.nctigba.observability.log.model.vo.LogClusterNodeVO;
import com.nctigba.observability.log.model.vo.LogClusterVO;
import com.nctigba.observability.log.model.vo.LogPathVO;
import com.nctigba.observability.log.service.ClusterManager;
import com.nctigba.observability.log.service.impl.ElasticsearchService;
import com.nctigba.observability.log.service.impl.FilebeatService;
import com.nctigba.observability.log.util.SshSession;
import org.opengauss.admin.common.core.domain.entity.ops.OpsClusterNodeEntity;
import org.opengauss.admin.common.core.domain.entity.ops.OpsHostEntity;
import org.opengauss.admin.common.core.domain.entity.ops.OpsHostUserEntity;
import org.opengauss.admin.common.core.domain.model.ops.OpsClusterNodeVO;
import org.opengauss.admin.common.core.domain.model.ops.OpsClusterVO;
import org.opengauss.admin.common.enums.ops.DeployTypeEnum;
import org.opengauss.admin.common.exception.CustomException;
import org.opengauss.admin.system.plugin.facade.HostFacade;
import org.opengauss.admin.system.plugin.facade.HostUserFacade;
import org.opengauss.admin.system.service.ops.IOpsClusterNodeService;
import org.opengauss.admin.system.service.ops.impl.EncryptionUtils;
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
import java.util.ArrayList;
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
    @AutowiredType(Type.PLUGIN_MAIN)
    private IOpsClusterNodeService opsClusterNodeService;
    @Autowired
    @AutowiredType(Type.PLUGIN_MAIN)
    private EncryptionUtils encryptionUtils;
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
    @GetMapping("/logPath")
    public LogPathVO logPath(@RequestParam String nodeId) {
        LogPathVO logPathVO = new LogPathVO();
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
            String ogRunLogPath;
            if (suffixPath != null && !suffixPath.startsWith("/")) {
                ogRunLogPath = prefixPath + "/" + suffixPath;
            } else {
                ogRunLogPath = suffixPath;
            }
            logPathVO.setOgRunLogPath(ogRunLogPath);
            logPathVO.setCmLogPath(getCmLogPath(nodeId));
            return logPathVO;
        } catch (SQLException e) {
            throw new CustomException("connect database failed.");
        }
    }

    private String getCmLogPath(String nodeId) {
        OpsClusterVO opsClusterVO = clusterManager.getOpsClusterByNodeId(nodeId);
        if (DeployTypeEnum.SINGLE_NODE.equals(opsClusterVO.getDeployType())) {
            return "";
        }
        OpsClusterNodeEntity opsClusterNodeEntity = opsClusterNodeService.getById(nodeId);
        if (opsClusterNodeEntity == null) {
            throw new CustomException("No cluster node information found");
        }
        String installUserId = opsClusterNodeEntity.getInstallUserId();
        OpsHostUserEntity user = hostUserFacade.getById(installUserId);
        ClusterManager.OpsClusterNodeVOSub node = clusterManager.getOpsNodeById(nodeId);
        OpsHostEntity hostEntity = hostFacade.getById(node.getHostId());
        if (hostEntity == null) {
            throw new CustomException("host not found");
        }
        String cmLogPath;
        try (var session = SshSession.connect(hostEntity.getPublicIp(), hostEntity.getPort(),
                user.getUsername(), encryptionUtils.decrypt(user.getPassword()))) {
            String envPath = opsClusterVO.getEnvPath();
            boolean isEnvApart = envPath != null && !"".equals(envPath);
            String cmLogPathCommand = isEnvApart ? "source " + envPath + " && echo $GAUSSLOG/cm" : "echo $GAUSSLOG/cm";
            cmLogPath = session.execute(cmLogPathCommand);
        } catch (IOException e) {
            throw new CustomException("connect failed:" + e.getMessage());
        }
        return cmLogPath;
    }

    @GetMapping("/hostUser/{hostId}")
    public List<OpsHostUserEntity> hostUser(@PathVariable String hostId) {
        return hostUserFacade.listHostUserByHostId(hostId);
    }

    /**
     * Get installed elasticsearch
     *
     * @return list
     */
    @GetMapping("/elasticsearch")
    public List<NctigbaEnvDO> listElasticsearch() {
        List<NctigbaEnvDO> env = envMapper
                .selectList(Wrappers.<NctigbaEnvDO>lambdaQuery().eq(NctigbaEnvDO::getType, InstallType.ELASTICSEARCH));
        env.forEach(e -> e.setHost(hostFacade.getById(e.getHostid())));
        return env;
    }

    /**
     * Get installed filebeat
     *
     * @return list
     */
    @GetMapping("/filebeat")
    public List<LogClusterVO> listFilebeat() {
        List<NctigbaEnvDO> env = envMapper
                .selectList(
                        Wrappers.<NctigbaEnvDO>lambdaQuery().in(NctigbaEnvDO::getType, List.of(InstallType.FILEBEAT)));
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
        var hosts = env.stream().map(NctigbaEnvDO::getNodeid).collect(Collectors.toSet());
        List<OpsClusterVO> clusterVOList = clusters.stream().filter(c -> {
            var nodes = c.getClusterNodes().stream().filter(n -> hosts.contains(n.getNodeId())).collect(
                    Collectors.toList());
            c.setClusterNodes(nodes);
            return nodes.size() > 0;
        }).collect(Collectors.toList());
        return reBuildTree(clusterVOList, env);
    }

    private List<LogClusterVO> reBuildTree(List<OpsClusterVO> clusterVOList, List<NctigbaEnvDO> env) {
        List<LogClusterVO> logClusterVOList = new ArrayList<>();
        for (OpsClusterVO opsClusterVO : clusterVOList) {
            LogClusterVO logClusterVO = new LogClusterVO();
            logClusterVO.setClusterId(opsClusterVO.getClusterId());
            logClusterVO.setClusterName(opsClusterVO.getClusterName());
            logClusterVO.setDbType(opsClusterVO.getDeployType());
            List<LogClusterNodeVO> logClusterNodeVOList = new ArrayList<>();
            for (OpsClusterNodeVO opsClusterNodeVO : opsClusterVO.getClusterNodes()) {
                LogClusterNodeVO logClusterNodeVO = new LogClusterNodeVO();
                logClusterNodeVO.setNodeId(opsClusterNodeVO.getNodeId());
                logClusterNodeVO.setHostId(opsClusterNodeVO.getHostId());
                logClusterNodeVO.setClusterRole(opsClusterNodeVO.getClusterRole());
                logClusterNodeVO.setDbPort(opsClusterNodeVO.getDbPort());
                logClusterNodeVO.setPublicIp(opsClusterNodeVO.getPublicIp());
                env.forEach(f -> {
                    if (f.getNodeid().equals(opsClusterNodeVO.getNodeId()) && f.getHostid().equals(
                            opsClusterNodeVO.getHostId())) {
                        logClusterNodeVO.setId(f.getId());
                    }
                });
                logClusterNodeVOList.add(logClusterNodeVO);
            }
            logClusterVO.setClusterNodes(logClusterNodeVOList);
            logClusterVOList.add(logClusterVO);
        }
        return logClusterVOList;
    }

    @GetMapping("/hosts")
    public List<OpsHostEntity> hosts() {
        return hostFacade.listAll();
    }

    @GetMapping("/pkg")
    public Map<String, Object> listPkg(String key, String hostId) {
        Map<String, Object> map = new HashMap<>();
        var host = hostFacade.getById(hostId);
        LambdaQueryWrapper<NctigbaEnvDO> wrapper = Wrappers.lambdaQuery();
        boolean isElasticsearch = "elasticsearch".equals(key);
        wrapper.eq(NctigbaEnvDO::getType, isElasticsearch ? InstallType.ELASTICSEARCH_PKG : InstallType.FILEBEAT_PKG);
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
                env.setType(InstallType.ELASTICSEARCH_PKG);
            } else if (name.startsWith("filebeat")) {
                env.setType(InstallType.FILEBEAT_PKG);
            }
            elasticsearchService.save(env);
        } catch (IllegalStateException | IOException e) {
            throw new CustomException("merge fail", e);
        }
    }
}