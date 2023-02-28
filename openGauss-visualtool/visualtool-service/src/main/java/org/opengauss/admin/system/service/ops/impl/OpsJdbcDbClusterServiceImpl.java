package org.opengauss.admin.system.service.ops.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.opengauss.admin.common.core.domain.entity.ops.OpsJdbcDbClusterEntity;
import org.opengauss.admin.common.core.domain.entity.ops.OpsJdbcDbClusterNodeEntity;
import org.opengauss.admin.common.core.domain.model.ops.jdbc.*;
import org.opengauss.admin.common.enums.ops.DbTypeEnum;
import org.opengauss.admin.common.enums.ops.DeployTypeEnum;
import org.opengauss.admin.common.exception.ops.OpsException;
import org.opengauss.admin.common.utils.ops.JdbcUtil;
import org.opengauss.admin.system.mapper.ops.OpsJdbcDbClusterMapper;
import org.opengauss.admin.system.service.ops.IOpsJdbcDbClusterNodeService;
import org.opengauss.admin.system.service.ops.IOpsJdbcDbClusterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author lhf
 * @date 2023/1/13 11:08
 **/
@Service
public class OpsJdbcDbClusterServiceImpl extends ServiceImpl<OpsJdbcDbClusterMapper, OpsJdbcDbClusterEntity> implements IOpsJdbcDbClusterService {

    @Autowired
    private IOpsJdbcDbClusterNodeService opsJdbcDbClusterNodeService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void add(JdbcDbClusterInputDto clusterInput) {
        OpsJdbcDbClusterEntity clusterEntity = saveCluster(clusterInput);
        saveClusterNode(clusterEntity, clusterInput);
    }

    @Override
    public Page<JdbcDbClusterVO> page(String name, Page page) {
        Set<String> clusterIdConditions = new HashSet<>();
        if (StrUtil.isNotEmpty(name)) {
            LambdaQueryWrapper<OpsJdbcDbClusterEntity> queryWrapper = Wrappers.lambdaQuery(OpsJdbcDbClusterEntity.class)
                    .like(OpsJdbcDbClusterEntity::getName, name)
                    .or()
                    .like(OpsJdbcDbClusterEntity::getRemark, name)
                    .select(OpsJdbcDbClusterEntity::getClusterId);

            List<OpsJdbcDbClusterEntity> clusterEntityList = list(queryWrapper);
            if (CollUtil.isNotEmpty(clusterEntityList)) {
                clusterIdConditions.addAll(clusterEntityList.stream().map(OpsJdbcDbClusterEntity::getClusterId).collect(Collectors.toSet()));
            }

            Set<String> fuzzyQueryClusterIds = opsJdbcDbClusterNodeService.fuzzyQueryClusterIds(name);
            clusterIdConditions.addAll(fuzzyQueryClusterIds);

            if (CollUtil.isEmpty(clusterIdConditions)) {
                return Page.of(0, 0);
            }
        }

        LambdaQueryWrapper<OpsJdbcDbClusterEntity> queryWrapper = Wrappers.lambdaQuery(OpsJdbcDbClusterEntity.class)
                .in(CollUtil.isNotEmpty(clusterIdConditions), OpsJdbcDbClusterEntity::getClusterId, clusterIdConditions)
                .orderByDesc(OpsJdbcDbClusterEntity::getCreateTime);

        Page<OpsJdbcDbClusterEntity> searchPage = page(page, queryWrapper);

        Page<JdbcDbClusterVO> resPage = new Page<>();
        resPage.setTotal(searchPage.getTotal());
        resPage.setRecords(buildPageRecords(searchPage.getRecords()));
        return resPage;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void del(String clusterId) {
        OpsJdbcDbClusterEntity clusterEntity = getById(clusterId);
        if (Objects.isNull(clusterEntity)) {
            throw new OpsException("Cluster information does not exist");
        }
        opsJdbcDbClusterNodeService.delByClusterId(clusterId);
        removeById(clusterId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(String clusterId, JdbcDbClusterInputDto clusterInput) {
        OpsJdbcDbClusterEntity clusterEntity = getById(clusterId);
        if (Objects.isNull(clusterEntity)) {
            throw new OpsException("Cluster information does not exist");
        }

        del(clusterId);
        add(clusterInput);
    }

    @Override
    public List<JdbcDbClusterInputDto> importAnalysis(MultipartFile file) {
        List<JdbcDbClusterInputDto> jdbcDbClusterInputDto = null;
        try {
            jdbcDbClusterInputDto = parseCSV(file);
        } catch (Exception e) {
            String msg = "The content of the file is malformed";

            if (e instanceof OpsException) {
                msg = msg + " : " + e.getMessage();
            }

            log.error(msg, e);
            throw new OpsException(msg);
        }

        return analysisWrongNodes(jdbcDbClusterInputDto);
    }

    private List<JdbcDbClusterInputDto> analysisWrongNodes(List<JdbcDbClusterInputDto> jdbcDbClusterInputDto) {
        List<JdbcDbClusterInputDto> wrongClusters = new ArrayList<>();

        for (JdbcDbClusterInputDto dbClusterInputDto : jdbcDbClusterInputDto) {
            boolean clusterWrong = false;
            DeployTypeEnum deployType = dbClusterInputDto.getDeployType();
            if (Objects.isNull(deployType)) {
                if (clusterWrong) {
                    dbClusterInputDto.setRemark(dbClusterInputDto.getRemark() + "，deployType not null");
                } else {
                    dbClusterInputDto.setRemark("deployType not null");
                }
                clusterWrong = true;
            }

            List<JdbcDbClusterNodeInputDto> nodes = dbClusterInputDto.getNodes();
            if (CollUtil.isEmpty(nodes)) {
                if (clusterWrong) {
                    dbClusterInputDto.setRemark(dbClusterInputDto.getRemark() + "，Cluster node cannot be empty");
                } else {
                    dbClusterInputDto.setRemark("Cluster node cannot be empty");
                }
                clusterWrong = true;
            }

            for (JdbcDbClusterNodeInputDto node : nodes) {
                boolean nodeWrong = false;
                String name = node.getName();
                if (StrUtil.isEmpty(name)) {
                    if (nodeWrong) {
                        node.setRemark(node.getRemark() + "，Node name cannot be empty");
                    } else {
                        node.setRemark("Node name cannot be empty");
                    }

                    nodeWrong = true;
                }

                String url = node.getUrl();
                try {
                    JdbcInfo jdbcInfo = JdbcUtil.parseUrl(url);
                    if (Objects.isNull(jdbcInfo)) {
                        throw new OpsException("parsing url failed");
                    }

                    if (Objects.isNull(jdbcInfo.getDbType())) {
                        throw new OpsException("wrong database type");
                    }

                    if (StrUtil.isEmpty(jdbcInfo.getIp())) {
                        throw new OpsException("wrong ip");
                    }

                    if (StrUtil.isEmpty(jdbcInfo.getPort())) {
                        throw new OpsException("wrong port");
                    }

                    OpsJdbcDbClusterNodeEntity clusterNodeByIpAndPort = opsJdbcDbClusterNodeService.getClusterNodeByIpAndPort(jdbcInfo.getIp(), jdbcInfo.getPort());
                    if (Objects.nonNull(clusterNodeByIpAndPort)) {
                        throw new OpsException("The current instance node already exists in other clusters");
                    }
                } catch (Exception e) {
                    String msg = "";
                    if (e instanceof OpsException) {
                        msg = e.getMessage();
                    } else {
                        msg = "parsing url error";
                    }
                    if (nodeWrong) {
                        node.setRemark(node.getRemark() + "，" + msg);
                    } else {
                        node.setRemark(msg);
                    }
                    nodeWrong = true;
                }

                String username = node.getUsername();
                if (StrUtil.isEmpty(username)) {
                    if (nodeWrong) {
                        node.setRemark(node.getRemark() + "，username is empty");
                    } else {
                        node.setRemark("username is empty");
                    }
                    nodeWrong = true;
                }

                String password = node.getPassword();
                if (StrUtil.isEmpty(password)) {
                    if (nodeWrong) {
                        node.setRemark(node.getRemark() + "，password is empty");
                    } else {
                        node.setRemark("password is empty");
                    }
                    nodeWrong = true;
                }

                clusterWrong |= nodeWrong;
            }

            if (clusterWrong) {
                wrongClusters.add(dbClusterInputDto);
            }
        }

        return wrongClusters;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void importCluster(MultipartFile file) {
        List<JdbcDbClusterInputDto> jdbcDbClusterInputDto = null;
        try {
            jdbcDbClusterInputDto = parseCSV(file);
        } catch (Exception e) {
            log.error("The content of the file is malformed", e);
            throw new OpsException("The content of the file is malformed");
        }

        List<JdbcDbClusterInputDto> wrongClusters = analysisWrongNodes(jdbcDbClusterInputDto);
        if (CollUtil.isNotEmpty(wrongClusters)) {
            throw new OpsException("There are " + wrongClusters.size() + " errors in the file content, please re-import after processing");
        }

        for (JdbcDbClusterInputDto dbClusterInputDto : jdbcDbClusterInputDto) {
            add(dbClusterInputDto);
        }
    }

    @Override
    public List<JdbcDbClusterVO> listAll() {
        List<OpsJdbcDbClusterEntity> dbClusterList = list();
        return buildPageRecords(dbClusterList);
    }

    @Data
    @AllArgsConstructor
    class Cluster {
        private String name;
        private String deployType;

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            Cluster cluster = (Cluster) o;

            if (name != null ? !name.equals(cluster.name) : cluster.name != null) return false;
            return deployType != null ? deployType.equals(cluster.deployType) : cluster.deployType == null;
        }

        @Override
        public int hashCode() {
            int result = name != null ? name.hashCode() : 0;
            result = 31 * result + (deployType != null ? deployType.hashCode() : 0);
            return result;
        }
    }

    @Data
    @AllArgsConstructor
    class ClusterNode {
        private String name;
        private String url;
        private String username;
        private String password;

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            ClusterNode that = (ClusterNode) o;

            return url != null ? url.equals(that.url) : that.url == null;
        }

        @Override
        public int hashCode() {
            return url != null ? url.hashCode() : 0;
        }
    }

    private List<JdbcDbClusterInputDto> parseCSV(MultipartFile file) {
        List<JdbcDbClusterInputDto> clusterList = new ArrayList<>();
        Map<Cluster, Set<ClusterNode>> clusterListMap = new HashMap<>();
        Integer lineNum = 0;
        try (InputStream inputStream = file.getInputStream(); final InputStreamReader inputStreamReader = new InputStreamReader(inputStream); BufferedReader bufferedReader = new BufferedReader(inputStreamReader)) {
            String line = null;
            while ((line = bufferedReader.readLine()) != null) {
                if (0 == lineNum++) {
                    continue;
                }

                String[] col = line.split(",");
                if (col.length != 6) {
                    throw new OpsException("The data in row " + lineNum + " is wrong");
                } else {
                    Cluster thatCluster = new Cluster(col[0], col[1]);
                    ClusterNode thatClusterNode = new ClusterNode(col[2], col[3], col[3], col[4]);

                    Set<ClusterNode> clusterNodes = clusterListMap.get(thatCluster);
                    if (Objects.isNull(clusterNodes)) {
                        clusterNodes = new HashSet<>();
                        clusterListMap.put(thatCluster, clusterNodes);
                    }

                    clusterNodes.add(thatClusterNode);
                }
            }
        } catch (IOException e) {
            throw new OpsException("Failed to read zone file contents");
        }

        if (CollUtil.isNotEmpty(clusterListMap)) {
            clusterListMap.forEach((cluster, nodes) -> {
                List<JdbcDbClusterNodeInputDto> clusterNodes = new ArrayList<>();
                for (ClusterNode node : nodes) {
                    clusterNodes.add(JdbcDbClusterNodeInputDto.of(node.getName(), node.getUrl(), node.getUsername(), node.getPassword()));
                }

                JdbcDbClusterInputDto inputDto = JdbcDbClusterInputDto.of(cluster.getName(), DeployTypeEnum.nameOf(cluster.getDeployType()));
                inputDto.setNodes(clusterNodes);
                clusterList.add(inputDto);
            });
        }

        if (CollUtil.isEmpty(clusterList)) {
            throw new OpsException("The cluster content read from the file is empty");
        }
        return clusterList;
    }

    private List<JdbcDbClusterVO> buildPageRecords(List<OpsJdbcDbClusterEntity> records) {
        if (CollUtil.isEmpty(records)) {
            return Collections.emptyList();
        }

        List<JdbcDbClusterVO> res = new ArrayList<>();

        Set<String> clusterIds = records.stream().map(OpsJdbcDbClusterEntity::getClusterId).collect(Collectors.toSet());
        Map<String, List<OpsJdbcDbClusterNodeEntity>> clusterNodeMap = opsJdbcDbClusterNodeService.mapClusterNodesByClusterId(clusterIds);

        for (OpsJdbcDbClusterEntity record : records) {
            List<JdbcDbClusterNodeVO> nodes = new ArrayList<>();
            JdbcDbClusterVO jdbcDbClusterVO = JdbcDbClusterVO.of(record, nodes);

            String clusterId = record.getClusterId();
            List<OpsJdbcDbClusterNodeEntity> clusterNodeEntityList = clusterNodeMap.get(clusterId);
            if (CollUtil.isNotEmpty(clusterNodeEntityList)) {
                for (OpsJdbcDbClusterNodeEntity clusterNodeEntity : clusterNodeEntityList) {
                    nodes.add(JdbcDbClusterNodeVO.of(clusterNodeEntity));
                }
            }

            res.add(jdbcDbClusterVO);
        }

        return res;
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

            OpsJdbcDbClusterNodeEntity clusterNodeEntity = opsJdbcDbClusterNodeService.getClusterNodeByIpAndPort(jdbcInfo.getIp(), jdbcInfo.getPort());
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

        JdbcDbClusterNodeInputDto firstNode = nodes.get(0);
        String url = firstNode.getUrl();
        JdbcInfo jdbcInfo = JdbcUtil.parseUrl(url);
        DbTypeEnum dbType = jdbcInfo.getDbType();


        OpsJdbcDbClusterEntity clusterEntity = new OpsJdbcDbClusterEntity();
        clusterEntity.setName(clusterInput.getClusterName());
        clusterEntity.setDeployType(clusterInput.getDeployType());
        clusterEntity.setDbType(dbType);

        Date now = new Date();
        clusterEntity.setCreateTime(now);
        clusterEntity.setUpdateTime(now);

        save(clusterEntity);
        return clusterEntity;
    }
}
