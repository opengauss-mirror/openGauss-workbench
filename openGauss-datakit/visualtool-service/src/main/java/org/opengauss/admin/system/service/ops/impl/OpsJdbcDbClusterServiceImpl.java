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
 * OpsJdbcDbClusterServiceImpl.java
 *
 * IDENTIFICATION
 * openGauss-visualtool/visualtool-service/src/main/java/org/opengauss/admin/system/service/ops/impl/OpsJdbcDbClusterServiceImpl.java
 *
 * -------------------------------------------------------------------------
 */


package org.opengauss.admin.system.service.ops.impl;

import com.alibaba.excel.EasyExcel;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import lombok.extern.slf4j.Slf4j;

import org.opengauss.admin.common.core.domain.entity.ops.OpsJdbcDbClusterEntity;
import org.opengauss.admin.common.core.domain.entity.ops.OpsJdbcDbClusterNodeEntity;
import org.opengauss.admin.common.core.domain.model.ops.jdbc.JdbcDbClusterImportAnalysisVO;
import org.opengauss.admin.common.core.domain.model.ops.jdbc.JdbcDbClusterInputDto;
import org.opengauss.admin.common.core.domain.model.ops.jdbc.JdbcDbClusterNodeInputDto;
import org.opengauss.admin.common.core.domain.model.ops.jdbc.JdbcDbClusterNodeVO;
import org.opengauss.admin.common.core.domain.model.ops.jdbc.JdbcDbClusterVO;
import org.opengauss.admin.common.core.domain.model.ops.jdbc.JdbcInfo;
import org.opengauss.admin.common.core.dto.ops.OpsJdbcClusterDto;
import org.opengauss.admin.common.core.dto.ops.OpsJdbcClusterImportDto;
import org.opengauss.admin.common.enums.ops.DbTypeEnum;
import org.opengauss.admin.common.enums.ops.DeployTypeEnum;
import org.opengauss.admin.common.exception.ops.OpsException;
import org.opengauss.admin.common.utils.StringUtils;
import org.opengauss.admin.common.utils.excel.OpsJdbcClusterImportListener;
import org.opengauss.admin.common.utils.ops.JdbcUtil;
import org.opengauss.admin.system.mapper.ops.OpsJdbcDbClusterMapper;
import org.opengauss.admin.system.service.ops.IHostService;
import org.opengauss.admin.system.service.ops.IOpsJdbcDbClusterNodeService;
import org.opengauss.admin.system.service.ops.IOpsJdbcDbClusterService;
import org.opengauss.admin.system.utils.MysqlUtils;
import org.opengauss.admin.system.utils.OpengaussUtils;
import org.opengauss.admin.system.utils.PostgresqlUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author lhf
 * @date 2023/1/13 11:08
 **/
@Slf4j
@Service
public class OpsJdbcDbClusterServiceImpl extends ServiceImpl<OpsJdbcDbClusterMapper, OpsJdbcDbClusterEntity> implements IOpsJdbcDbClusterService {
    @Autowired
    @Lazy
    private IOpsJdbcDbClusterNodeService opsJdbcDbClusterNodeService;
    @Autowired
    private IHostService hostService;
    @Autowired
    private EncryptionUtils encryptionUtils;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void add(JdbcDbClusterInputDto clusterInput) {
        OpsJdbcDbClusterEntity clusterEntity = saveCluster(clusterInput);
        saveClusterNode(clusterEntity, clusterInput);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(String clusterId) {
        if (StrUtil.isEmpty(clusterId)) {
            return;
        }
        opsJdbcDbClusterNodeService.deleteByClusterId(clusterId);
        removeById(clusterId);
    }

    /**
     * Batch delete jdbc cluster
     *
     * @param ids cluster ids
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchDelete(List<Long> ids) {
        List<String> clusterIds = ids.stream().map(String::valueOf).collect(Collectors.toList());
        opsJdbcDbClusterNodeService.deleteByClusterIds(clusterIds);
        removeByIds(clusterIds);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(String clusterId, JdbcDbClusterInputDto clusterInput) {
        OpsJdbcDbClusterEntity clusterEntity = getById(clusterId);
        if (Objects.isNull(clusterEntity)) {
            throw new OpsException("Cluster information does not exist");
        }

        delete(clusterId);
        add(clusterInput);
    }

    @Override
    public JdbcDbClusterImportAnalysisVO importAnalysis(MultipartFile file) {
        try {
            List<OpsJdbcClusterImportDto> importDtoList = EasyExcel
                    .read(file.getInputStream(), OpsJdbcClusterImportDto.class, new OpsJdbcClusterImportListener())
                    .sheet()
                    .doReadSync();
            if (CollUtil.isEmpty(importDtoList)) {
                throw new OpsException("Import jdbc cluster read from the file are empty");
            }

            parseImportData(importDtoList);
            return JdbcDbClusterImportAnalysisVO.of(importDtoList);
        } catch (IOException e) {
            log.error("Failed to parse jdbc cluster import file", e);
            throw new OpsException("Failed to parse jdbc cluster import file, exception: " + e.getClass().getName()
                    + ", message: " + e.getMessage());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void importCluster(MultipartFile file) {
        try {
            List<OpsJdbcClusterImportDto> importDtoList = EasyExcel
                    .read(file.getInputStream(), OpsJdbcClusterImportDto.class, new OpsJdbcClusterImportListener())
                    .sheet()
                    .doReadSync();
            if (CollUtil.isEmpty(importDtoList)) {
                throw new OpsException("Import jdbc cluster read from the file are empty");
            }

            List<OpsJdbcClusterDto> opsJdbcClusterDtoList = parseImportData(importDtoList);
            if (importDtoList.stream().anyMatch(OpsJdbcClusterImportDto::hasError)) {
                throw new OpsException("Failed to import jdbc cluster, import records have errors. "
                        + "Please call importAnalysis first.");
            }

            for (OpsJdbcClusterDto opsJdbcClusterDto : opsJdbcClusterDtoList) {
                OpsJdbcDbClusterEntity clusterEntity = opsJdbcClusterDto.getClusterEntity();
                Date now = new Date();
                clusterEntity.setCreateTime(now);
                clusterEntity.setUpdateTime(now);
                save(clusterEntity);

                List<OpsJdbcDbClusterNodeEntity> clusterNodeEntityList = opsJdbcClusterDto.getClusterNodeEntityList();
                for (OpsJdbcDbClusterNodeEntity clusterNodeEntity : clusterNodeEntityList) {
                    clusterNodeEntity.setClusterId(clusterEntity.getClusterId());
                    clusterNodeEntity.setCreateTime(now);
                    clusterNodeEntity.setUpdateTime(now);
                }
                opsJdbcDbClusterNodeService.saveBatch(clusterNodeEntityList);
            }
        } catch (IOException e) {
            log.error("Failed to parse jdbc cluster import file", e);
            throw new OpsException("Failed to parse jdbc cluster import file, exception: " + e.getClass().getName()
                    + ", message: " + e.getMessage());
        }
    }

    private List<OpsJdbcClusterDto> parseImportData(List<OpsJdbcClusterImportDto> importDtoList) {
        Map<String, OpsJdbcClusterImportDto> ipPortUsernameMap = new HashMap<>();
        Map<String, List<OpsJdbcClusterImportDto>> groupByClusterName = new HashMap<>();
        for (OpsJdbcClusterImportDto dto : importDtoList) {
            if (dto.hasError()) {
                continue;
            } else {
                dto.setPassword(encryptionUtils.encrypt(dto.getPassword()));
            }

            String key = generateKey(dto.getIp(), dto.getPort(), dto.getUsername());
            if (ipPortUsernameMap.containsKey(key)) {
                dto.setErrorMsg("Multiple records with the same 'Node IP, Port, Username' cannot be imported");
                continue;
            } else {
                ipPortUsernameMap.put(key, dto);
            }

            if (groupByClusterName.containsKey(dto.getClusterName())) {
                if (dto.getDbType().equals(groupByClusterName.get(dto.getClusterName()).get(0).getDbType())) {
                    groupByClusterName.get(dto.getClusterName()).add(dto);
                } else {
                    dto.setErrorMsg("Database type must be the same for all records with the same cluster name");
                }
            } else {
                List<OpsJdbcClusterImportDto> sameClusterNameImportDtoList = new ArrayList<>();
                sameClusterNameImportDtoList.add(dto);
                groupByClusterName.put(dto.getClusterName(), sameClusterNameImportDtoList);
            }
        }

        checkClusterNameDuplicateInDb(groupByClusterName);
        checkIpPortUsernameDuplicateInDb(ipPortUsernameMap);

        List<OpsJdbcClusterDto> opsJdbcClusterDtoList = new ArrayList<>();
        if (importDtoList.stream().noneMatch(OpsJdbcClusterImportDto::hasError)) {
            groupByClusterName.forEach((key, value) -> {
                opsJdbcClusterDtoList.add(generateOpsJdbcClusterDto(value));
            });
        }
        return opsJdbcClusterDtoList;
    }

    private void checkIpPortUsernameDuplicateInDb(Map<String, OpsJdbcClusterImportDto> ipPortUsernameMap) {
        if (ipPortUsernameMap.isEmpty()) {
            return;
        }
        LambdaQueryWrapper<OpsJdbcDbClusterNodeEntity> nodeQueryWrapper = new LambdaQueryWrapper<>();
        nodeQueryWrapper.select(OpsJdbcDbClusterNodeEntity::getIp, OpsJdbcDbClusterNodeEntity::getPort,
                OpsJdbcDbClusterNodeEntity::getUsername);
        List<OpsJdbcDbClusterNodeEntity> clusterNodeEntityList = opsJdbcDbClusterNodeService.list(nodeQueryWrapper);
        if (CollUtil.isNotEmpty(clusterNodeEntityList)) {
            for (OpsJdbcDbClusterNodeEntity nodeEntity : clusterNodeEntityList) {
                String key = generateKey(nodeEntity.getIp(), nodeEntity.getPort(), nodeEntity.getUsername());
                if (ipPortUsernameMap.containsKey(key)) {
                    ipPortUsernameMap.get(key).setErrorMsg("The cluster node with the same 'Node IP, Port, Username' "
                            + "already exists in other clusters.");
                }
            }
        }
    }

    private void checkClusterNameDuplicateInDb(Map<String, List<OpsJdbcClusterImportDto>> groupByClusterName) {
        if (groupByClusterName.isEmpty()) {
            return;
        }
        List<String> clusterNameList = groupByClusterName.keySet().stream().toList();
        LambdaQueryWrapper<OpsJdbcDbClusterEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(OpsJdbcDbClusterEntity::getName, clusterNameList);
        List<OpsJdbcDbClusterEntity> clusterEntityList = list(queryWrapper);
        if (CollUtil.isNotEmpty(clusterEntityList)) {
            clusterEntityList.forEach((clusterEntity) -> {
                groupByClusterName.get(clusterEntity.getName()).forEach((dto) -> {
                    dto.setErrorMsg("The cluster with the same name '" + clusterEntity.getName()
                            + "' has already been managed.");
                });
            });
        }
    }

    private String generateKey(String ip, String port, String username) {
        return ip + ":" + port + ":" + username;
    }

    private OpsJdbcClusterDto generateOpsJdbcClusterDto(List<OpsJdbcClusterImportDto> importDtoList) {
        OpsJdbcDbClusterEntity clusterEntity = new OpsJdbcDbClusterEntity();
        clusterEntity.setName(importDtoList.get(0).getClusterName());
        clusterEntity.setDbType(importDtoList.get(0).getDbType());

        String version;
        OpsJdbcDbClusterNodeEntity nodeEntity;
        boolean isVersionDiff = false;
        List<OpsJdbcDbClusterNodeEntity> nodeEntityList = new ArrayList<>();
        for (OpsJdbcClusterImportDto importDto : importDtoList) {
            nodeEntity = importDto.convertToOpsJdbcClusterNodeEntity();
            nodeEntityList.add(nodeEntity);

            try {
                version = getClusterVersionNum(importDto.getDbType(), nodeEntity.getUrl(), nodeEntity.getUsername(),
                        nodeEntity.getPassword());
                if (isVersionDiff) {
                    importDto.setErrorMsg("Version number must be the same for all records with the same cluster name");
                    continue;
                }

                if (clusterEntity.getVersionNum() == null) {
                    clusterEntity.setVersionNum(version);
                    continue;
                }

                if (!clusterEntity.getVersionNum().equals(version)) {
                    isVersionDiff = true;
                    importDto.setErrorMsg("Version number must be the same for all records with the same cluster name");
                }
            } catch (OpsException e) {
                importDto.setErrorMsg(e.getMessage());
            }
        }

        if (importDtoList.size() == 1) {
            clusterEntity.setDeployType(DeployTypeEnum.SINGLE_NODE);
        } else {
            clusterEntity.setDeployType(DeployTypeEnum.CLUSTER);
        }
        return new OpsJdbcClusterDto(clusterEntity, nodeEntityList);
    }

    @Override
    public List<JdbcDbClusterVO> listByType(String type) {
        if (StrUtil.isEmpty(type)) {
            throw new OpsException("type parameter does not exist");
        }

        LambdaQueryWrapper<OpsJdbcDbClusterEntity> queryWrapper = Wrappers.lambdaQuery(OpsJdbcDbClusterEntity.class)
                .eq(OpsJdbcDbClusterEntity::getDbType, type.toUpperCase());

        List<OpsJdbcDbClusterEntity> dbClusterList = list(queryWrapper);
        return buildPageRecords(dbClusterList);
    }

    @Override
    public List<JdbcDbClusterVO> listByType(DbTypeEnum dbType) {
        List<OpsJdbcDbClusterEntity> jdbcClusters;
        if (dbType == null) {
            jdbcClusters = list();
        } else {
            LambdaQueryWrapper<OpsJdbcDbClusterEntity> queryWrapper = Wrappers.lambdaQuery(OpsJdbcDbClusterEntity.class)
                    .eq(OpsJdbcDbClusterEntity::getDbType, dbType);
            jdbcClusters = list(queryWrapper);
        }

        List<OpsJdbcDbClusterNodeEntity> jdbcNodes = opsJdbcDbClusterNodeService.listByClusterIds(
                jdbcClusters.stream().map(OpsJdbcDbClusterEntity::getClusterId).toList());
        return buildJdbcClusterVos(jdbcClusters, jdbcNodes);
    }

    @Override
    public List<OpsJdbcDbClusterEntity> listByName(String name) {
        if (StrUtil.isEmpty(name)) {
            return list();
        }

        LambdaQueryWrapper<OpsJdbcDbClusterEntity> queryWrapper = Wrappers.lambdaQuery(OpsJdbcDbClusterEntity.class)
                .like(OpsJdbcDbClusterEntity::getName, name);

        return list(queryWrapper);
    }

    @Override
    public JdbcDbClusterVO getJdbcClusterVoByClusterId(String clusterId) {
        if (StrUtil.isEmpty(clusterId)) {
            throw new IllegalArgumentException("Parameter clusterId cannot be empty");
        }

        OpsJdbcDbClusterEntity jdbcCluster = getById(clusterId);
        if (jdbcCluster == null) {
            return null;
        }

        List<OpsJdbcDbClusterNodeEntity> jdbcNodes = opsJdbcDbClusterNodeService.listByClusterIds(List.of(clusterId));
        return buildJdbcClusterVos(List.of(jdbcCluster), jdbcNodes).get(0);
    }

    @Override
    public List<OpsJdbcDbClusterEntity> listByNameAndType(String name, String type) {
        LambdaQueryWrapper<OpsJdbcDbClusterEntity> queryWrapper = new LambdaQueryWrapper<>();
        if (!StrUtil.isEmpty(name)) {
            queryWrapper.like(OpsJdbcDbClusterEntity::getName, name);
        }
        if (!StrUtil.isEmpty(type)) {
            queryWrapper.eq(OpsJdbcDbClusterEntity::getDbType, type.toUpperCase(Locale.ROOT));
        }

        return list(queryWrapper);
    }

    @Override
    public List<JdbcDbClusterVO> getJdbcClusterVosByNameAndIp(String name, String ip) {
        List<OpsJdbcDbClusterEntity> jdbcClusters = listByName(name);
        List<OpsJdbcDbClusterNodeEntity> jdbcNodes = opsJdbcDbClusterNodeService.listByIpAndClusterIds(
                ip, jdbcClusters.stream().map(OpsJdbcDbClusterEntity::getClusterId).toList());
        List<String> jdbcIds = jdbcNodes.stream().map(OpsJdbcDbClusterNodeEntity::getClusterId).toList();
        List<OpsJdbcDbClusterEntity> jdbcClustersFiltered = jdbcClusters.stream()
                .filter(cluster -> jdbcIds.contains(cluster.getClusterId()))
                .toList();

        return buildJdbcClusterVos(jdbcClustersFiltered, jdbcNodes);
    }

    @Override
    public List<JdbcDbClusterVO> getJdbcClusterVosByNameIpAndType(String name, String ip, String type) {
        List<OpsJdbcDbClusterEntity> jdbcClusters = listByNameAndType(name, type);
        List<OpsJdbcDbClusterNodeEntity> jdbcNodes = opsJdbcDbClusterNodeService.listByIpAndClusterIds(
                ip, jdbcClusters.stream().map(OpsJdbcDbClusterEntity::getClusterId).toList());
        List<String> jdbcIds = jdbcNodes.stream().map(OpsJdbcDbClusterNodeEntity::getClusterId).toList();
        List<OpsJdbcDbClusterEntity> jdbcClustersFiltered = jdbcClusters.stream()
                .filter(cluster -> jdbcIds.contains(cluster.getClusterId()))
                .toList();

        return buildJdbcClusterVos(jdbcClustersFiltered, jdbcNodes);
    }

    @Override
    public String version(String clusterId, DbTypeEnum dbType) {
        if (StrUtil.isEmpty(clusterId)) {
            throw new IllegalArgumentException("Cluster id cannot be empty");
        }

        LambdaQueryWrapper<OpsJdbcDbClusterEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(OpsJdbcDbClusterEntity::getClusterId, clusterId);
        if (dbType != null) {
            queryWrapper.eq(OpsJdbcDbClusterEntity::getDbType, dbType);
        }

        OpsJdbcDbClusterEntity cluster = getOne(queryWrapper);
        if (cluster == null) {
            log.error("Jdbc cluster does not exist, clusterId: {}", clusterId);
            throw new OpsException("Jdbc cluster does not exist, clusterId: " + clusterId);
        }

        String versionNum = cluster.getVersionNum();
        if (!StrUtil.isEmpty(versionNum)) {
            return versionNum;
        }

        OpsJdbcDbClusterNodeEntity node = opsJdbcDbClusterNodeService.getOneByClusterId(clusterId);
        if (node == null) {
            log.error("Jdbc cluster node does not exist, clusterId: {}", clusterId);
            throw new OpsException("Jdbc cluster node does not exist, clusterId: " + clusterId);
        }

        versionNum = getClusterVersionNum(cluster.getDbType(), node.getUrl(), node.getUsername(), node.getPassword());
        cluster.setVersionNum(versionNum);
        updateById(cluster);
        return versionNum;
    }

    private List<JdbcDbClusterVO> buildJdbcClusterVos(
            List<OpsJdbcDbClusterEntity> jdbcClusters, List<OpsJdbcDbClusterNodeEntity> jdbcNodes
    ) {
        List<JdbcDbClusterVO> jdbcDbClusterVos = new ArrayList<>();
        for (OpsJdbcDbClusterEntity cluster : jdbcClusters) {
            String clusterId = cluster.getClusterId();
            List<JdbcDbClusterNodeVO> nodes = jdbcNodes.stream()
                    .filter(node -> node.getClusterId().equals(clusterId))
                    .map(JdbcDbClusterNodeVO::of)
                    .toList();
            jdbcDbClusterVos.add(JdbcDbClusterVO.of(cluster, nodes));
        }
        return jdbcDbClusterVos;
    }

    private List<JdbcDbClusterVO> buildPageRecords(List<OpsJdbcDbClusterEntity> records) {
        if (CollUtil.isEmpty(records)) {
            return Collections.emptyList();
        }
        List<JdbcDbClusterVO> res = new ArrayList<>();
        Set<String> clusterIds = records.stream().map(OpsJdbcDbClusterEntity::getClusterId).collect(Collectors.toSet());
        Map<String, List<OpsJdbcDbClusterNodeEntity>> clusterNodeMap = opsJdbcDbClusterNodeService.mapClusterNodesByClusterId(clusterIds);
        final Set<String> ipSet = clusterNodeMap.values().stream().flatMap(val -> val.stream()).map(OpsJdbcDbClusterNodeEntity::getIp).collect(Collectors.toSet());
        Map<String, String> ipOsMap = hostService.mapOsByIps(ipSet);
        for (OpsJdbcDbClusterEntity record : records) {
            List<JdbcDbClusterNodeVO> nodes = new ArrayList<>();
            JdbcDbClusterVO jdbcDbClusterVO = JdbcDbClusterVO.of(record, nodes);
            String clusterId = record.getClusterId();
            List<OpsJdbcDbClusterNodeEntity> clusterNodeEntityList = clusterNodeMap.get(clusterId);
            if (CollUtil.isNotEmpty(clusterNodeEntityList)) {
                Connection conn = null;
                for (OpsJdbcDbClusterNodeEntity clusterNodeEntity : clusterNodeEntityList) {
                    nodes.add(JdbcDbClusterNodeVO.of(clusterNodeEntity, ipOsMap.get(clusterNodeEntity.getIp())));
                    try {
                        conn = JdbcUtil.getConnection(clusterNodeEntity.getUrl(), clusterNodeEntity.getUsername(),
                            encryptionUtils.decrypt(clusterNodeEntity.getPassword()));
                    } catch (OpsException e) {
                        log.error("opsException: ", e);
                    }
                    jdbcDbClusterVO.setVersionNum(getDbVersion(conn));
                }
            }
            res.add(jdbcDbClusterVO);
        }
        return res;
    }

    private String getDbVersion(Connection connection) {
        if (connection == null) {
            return "";
        }
        String sql = "select version()";
        String version = "";
        try (PreparedStatement preparedStatement = connection.prepareStatement(sql);
             ResultSet resultSet = preparedStatement.executeQuery()) {
            if (resultSet.next()) {
                version = resultSet.getString(1);
                return version;
            }
        } catch (SQLException e) {
            log.error("query version fail", e);
            throw new OpsException("query version fail");
        }
        return version;
    }

    private void saveClusterNode(OpsJdbcDbClusterEntity clusterEntity, JdbcDbClusterInputDto clusterInput) {
        List<JdbcDbClusterNodeInputDto> nodes = clusterInput.getNodes();
        if (CollUtil.isEmpty(nodes)) {
            throw new OpsException("Cluster node information does not exist");
        }

        Date now = new Date();
        List<OpsJdbcDbClusterNodeEntity> clusterNodeEntityList = new ArrayList<>();
        for (JdbcDbClusterNodeInputDto node : nodes) {
            String url = node.getUrl();
            JdbcInfo jdbcInfo = JdbcUtil.parseUrl(url);

            OpsJdbcDbClusterNodeEntity clusterNodeEntity = opsJdbcDbClusterNodeService
                    .getClusterNodeByIpAndPort(jdbcInfo.getIp(), jdbcInfo.getPort(), node.getUsername());
            if (Objects.nonNull(clusterNodeEntity)) {
                throw new OpsException("Cluster node information already exists");
            }

            OpsJdbcDbClusterNodeEntity opsJdbcDbClusterNodeEntity = new OpsJdbcDbClusterNodeEntity();

            opsJdbcDbClusterNodeEntity.setClusterId(clusterEntity.getClusterId());
            opsJdbcDbClusterNodeEntity.setName(node.getName());
            opsJdbcDbClusterNodeEntity.setIp(jdbcInfo.getIp());
            opsJdbcDbClusterNodeEntity.setPort(jdbcInfo.getPort());
            opsJdbcDbClusterNodeEntity.setUsername(node.getUsername());
            opsJdbcDbClusterNodeEntity.setPassword(node.getPassword());
            opsJdbcDbClusterNodeEntity.setUrl(url);
            opsJdbcDbClusterNodeEntity.setCreateTime(now);
            opsJdbcDbClusterNodeEntity.setUpdateTime(now);
            opsJdbcDbClusterNodeEntity.setRemark(node.getRemark());

            clusterNodeEntityList.add(opsJdbcDbClusterNodeEntity);
        }

        if (CollUtil.isNotEmpty(clusterNodeEntityList)) {
            opsJdbcDbClusterNodeService.saveBatch(clusterNodeEntityList);
        } else {
            throw new OpsException("Cluster node information does not exist");
        }
    }

    private OpsJdbcDbClusterEntity saveCluster(JdbcDbClusterInputDto clusterInput) {
        List<JdbcDbClusterNodeInputDto> nodes = clusterInput.getNodes();
        if (CollUtil.isEmpty(nodes)) {
            throw new OpsException("Cluster node information does not exist");
        }
        if (StringUtils.isEmpty(clusterInput.getClusterName())) {
            throw new OpsException("Cluster name cannot be empty");
        }

        LambdaQueryWrapper<OpsJdbcDbClusterEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(OpsJdbcDbClusterEntity::getName, clusterInput.getClusterName());
        List<OpsJdbcDbClusterEntity> clusterEntityList = list(queryWrapper);
        if (CollUtil.isNotEmpty(clusterEntityList)) {
            throw new OpsException("Cluster name already exists");
        }

        JdbcDbClusterNodeInputDto firstNode = nodes.get(0);
        String url = firstNode.getUrl();
        JdbcInfo jdbcInfo = JdbcUtil.parseUrl(url);
        DbTypeEnum dbType = jdbcInfo.getDbType();

        OpsJdbcDbClusterEntity clusterEntity = new OpsJdbcDbClusterEntity();
        clusterEntity.setName(clusterInput.getClusterName());
        clusterEntity.setDeployType(clusterInput.getDeployType());
        clusterEntity.setDbType(dbType);
        clusterEntity.setVersionNum(getClusterVersionNum(dbType, url, firstNode.getUsername(),
                firstNode.getPassword()));

        Date now = new Date();
        clusterEntity.setCreateTime(now);
        clusterEntity.setUpdateTime(now);

        save(clusterEntity);
        return clusterEntity;
    }

    private String getClusterVersionNum(DbTypeEnum dbType, String url, String username, String password) {
        try (Connection connection = JdbcUtil.getConnection(url, username, encryptionUtils.decrypt(password))) {
            if (DbTypeEnum.MYSQL.equals(dbType)) {
                return MysqlUtils.getVersion(connection);
            } else if (DbTypeEnum.OPENGAUSS.equals(dbType)) {
                return OpengaussUtils.getVersion(connection);
            } else if (DbTypeEnum.POSTGRESQL.equals(dbType)) {
                return PostgresqlUtils.getVersion(connection);
            } else {
                throw new OpsException("DbTypeEnum " + dbType + " is not supported");
            }
        } catch (SQLException e) {
            log.error("Jdbc db cluster get version exception", e);
            throw new OpsException(e.getClass().getName() + e.getMessage());
        }
    }
}
