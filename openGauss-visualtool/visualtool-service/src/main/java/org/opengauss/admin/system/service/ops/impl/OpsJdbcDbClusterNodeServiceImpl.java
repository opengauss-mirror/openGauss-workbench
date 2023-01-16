package org.opengauss.admin.system.service.ops.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.opengauss.admin.common.core.domain.entity.ops.OpsJdbcDbClusterEntity;
import org.opengauss.admin.common.core.domain.entity.ops.OpsJdbcDbClusterNodeEntity;
import org.opengauss.admin.common.core.domain.model.ops.jdbc.JdbcDbClusterNodeInputDto;
import org.opengauss.admin.common.core.domain.model.ops.jdbc.JdbcInfo;
import org.opengauss.admin.common.exception.ops.OpsException;
import org.opengauss.admin.common.utils.ops.JdbcUtil;
import org.opengauss.admin.system.mapper.ops.OpsJdbcDbClusterNodeMapper;
import org.opengauss.admin.system.service.ops.IOpsJdbcDbClusterNodeService;
import org.opengauss.admin.system.service.ops.IOpsJdbcDbClusterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Connection;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author lhf
 * @date 2023/1/13 11:09
 **/
@Service
public class OpsJdbcDbClusterNodeServiceImpl extends ServiceImpl<OpsJdbcDbClusterNodeMapper, OpsJdbcDbClusterNodeEntity> implements IOpsJdbcDbClusterNodeService {

    @Autowired
    private IOpsJdbcDbClusterService opsJdbcDbClusterService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void del(String clusterNodeId) {
        OpsJdbcDbClusterNodeEntity clusterNodeEntity = getById(clusterNodeId);
        if (Objects.isNull(clusterNodeEntity)) {
            throw new OpsException("Node information does not exist");
        }

        String clusterId = clusterNodeEntity.getClusterId();
        List<OpsJdbcDbClusterNodeEntity> nodes = listNodeByClusterId(clusterId);
        if (CollUtil.isNotEmpty(nodes) && nodes.size() == 1) {
            opsJdbcDbClusterService.del(clusterNodeEntity.getClusterId());
        } else {
            removeById(clusterNodeId);
        }
    }

    private List<OpsJdbcDbClusterNodeEntity> listNodeByClusterId(String clusterId) {
        if (StrUtil.isEmpty(clusterId)) {
            return Collections.emptyList();
        }

        LambdaQueryWrapper<OpsJdbcDbClusterNodeEntity> queryWrapper = Wrappers.lambdaQuery(OpsJdbcDbClusterNodeEntity.class)
                .eq(OpsJdbcDbClusterNodeEntity::getClusterId, clusterId);
        return list(queryWrapper);
    }

    @Override
    public OpsJdbcDbClusterNodeEntity getClusterNodeByIpAndPort(String ip, String port) {
        LambdaQueryWrapper<OpsJdbcDbClusterNodeEntity> queryWrapper = Wrappers.lambdaQuery(OpsJdbcDbClusterNodeEntity.class)
                .eq(OpsJdbcDbClusterNodeEntity::getIp, ip)
                .eq(OpsJdbcDbClusterNodeEntity::getPort, port);
        return getOne(queryWrapper, false);
    }

    @Override
    public Set<String> fuzzyQueryClusterIds(String name) {
        Set<String> res = new HashSet<>();
        LambdaQueryWrapper<OpsJdbcDbClusterNodeEntity> queryWrapper = Wrappers.lambdaQuery(OpsJdbcDbClusterNodeEntity.class)
                .like(OpsJdbcDbClusterNodeEntity::getName, name)
                .or()
                .like(OpsJdbcDbClusterNodeEntity::getIp, name)
                .or()
                .like(OpsJdbcDbClusterNodeEntity::getPort, name)
                .or()
                .like(OpsJdbcDbClusterNodeEntity::getUrl, name)
                .select(OpsJdbcDbClusterNodeEntity::getClusterId);

        List<OpsJdbcDbClusterNodeEntity> list = list(queryWrapper);
        if (CollUtil.isNotEmpty(list)) {
            res.addAll(list.stream().map(OpsJdbcDbClusterNodeEntity::getClusterId).collect(Collectors.toSet()));
        }
        return res;
    }

    @Override
    public Map<String, List<OpsJdbcDbClusterNodeEntity>> mapClusterNodesByClusterId(Set<String> clusterIds) {
        if (CollUtil.isEmpty(clusterIds)) {
            return Collections.emptyMap();
        }

        Map<String, List<OpsJdbcDbClusterNodeEntity>> res = new HashMap<>();

        LambdaQueryWrapper<OpsJdbcDbClusterNodeEntity> queryWrapper = Wrappers.lambdaQuery(OpsJdbcDbClusterNodeEntity.class)
                .in(OpsJdbcDbClusterNodeEntity::getClusterId, clusterIds)
                .orderByDesc(OpsJdbcDbClusterNodeEntity::getCreateTime);

        List<OpsJdbcDbClusterNodeEntity> list = list(queryWrapper);
        if (CollUtil.isNotEmpty(list)) {
            res = list.stream().collect(Collectors.groupingBy(OpsJdbcDbClusterNodeEntity::getClusterId));
        }

        return res;
    }

    @Override
    public void delByClusterId(String clusterId) {
        if (StrUtil.isEmpty(clusterId)) {
            return;
        }

        LambdaQueryWrapper<OpsJdbcDbClusterNodeEntity> queryWrapper = Wrappers.lambdaQuery(OpsJdbcDbClusterNodeEntity.class)
                .eq(OpsJdbcDbClusterNodeEntity::getClusterId, clusterId);

        remove(queryWrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(String clusterNodeId, JdbcDbClusterNodeInputDto clusterNodeInput) {
        OpsJdbcDbClusterNodeEntity clusterNodeEntity = getById(clusterNodeId);
        if (Objects.isNull(clusterNodeEntity)) {
            throw new OpsException("Cluster node information does not exist");
        }

        String url = clusterNodeInput.getUrl();
        JdbcInfo jdbcInfo = JdbcUtil.parseUrl(url);

        OpsJdbcDbClusterNodeEntity opsJdbcDbClusterNodeEntity = new OpsJdbcDbClusterNodeEntity();
        opsJdbcDbClusterNodeEntity.setClusterNodeId(clusterNodeEntity.getClusterNodeId());
        opsJdbcDbClusterNodeEntity.setClusterId(clusterNodeEntity.getClusterId());
        opsJdbcDbClusterNodeEntity.setName(clusterNodeInput.getName());
        opsJdbcDbClusterNodeEntity.setIp(jdbcInfo.getIp());
        opsJdbcDbClusterNodeEntity.setPort(jdbcInfo.getPort());
        opsJdbcDbClusterNodeEntity.setUsername(clusterNodeInput.getUsername());
        opsJdbcDbClusterNodeEntity.setPassword(clusterNodeInput.getPassword());
        opsJdbcDbClusterNodeEntity.setUrl(url);
        opsJdbcDbClusterNodeEntity.setUpdateTime(new Date());

        updateById(opsJdbcDbClusterNodeEntity);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void add(String clusterId, JdbcDbClusterNodeInputDto clusterNodeInput) {
        OpsJdbcDbClusterEntity clusterEntity = opsJdbcDbClusterService.getById(clusterId);
        if (Objects.isNull(clusterEntity)) {
            throw new OpsException("Cluster information does not exist");
        }

        if (StrUtil.isEmpty(clusterNodeInput.getUsername())) {
            throw new OpsException("Username can not be empty");
        }

        if (StrUtil.isEmpty(clusterNodeInput.getPassword())) {
            throw new OpsException("password can not be empty");
        }

        String url = clusterNodeInput.getUrl();
        JdbcInfo jdbcInfo = JdbcUtil.parseUrl(url);

        OpsJdbcDbClusterNodeEntity clusterNodeEntity = getClusterNodeByIpAndPort(jdbcInfo.getIp(), jdbcInfo.getPort());
        if (Objects.nonNull(clusterNodeEntity)) {
            throw new OpsException("Cluster node information already exists");
        }

        OpsJdbcDbClusterNodeEntity opsJdbcDbClusterNodeEntity = new OpsJdbcDbClusterNodeEntity();

        opsJdbcDbClusterNodeEntity.setClusterId(clusterEntity.getClusterId());
        opsJdbcDbClusterNodeEntity.setName(clusterNodeInput.getName());
        opsJdbcDbClusterNodeEntity.setIp(jdbcInfo.getIp());
        opsJdbcDbClusterNodeEntity.setPort(jdbcInfo.getPort());
        opsJdbcDbClusterNodeEntity.setUsername(clusterNodeInput.getUsername());
        opsJdbcDbClusterNodeEntity.setPassword(clusterNodeInput.getPassword());
        opsJdbcDbClusterNodeEntity.setUrl(url);
        opsJdbcDbClusterNodeEntity.setCreateTime(new Date());

        save(opsJdbcDbClusterNodeEntity);

        if (Objects.nonNull(clusterNodeInput.getDeployType())) {
            clusterEntity.setDeployType(clusterNodeInput.getDeployType());
            opsJdbcDbClusterService.updateById(clusterEntity);
        } else {
            throw new OpsException("deployment type is empty");
        }
    }

    @Override
    public boolean ping(JdbcDbClusterNodeInputDto clusterNodeInput) {
        boolean res = false;
        try (Connection connection = JdbcUtil.getConnection(clusterNodeInput.getUrl(), clusterNodeInput.getUsername(), clusterNodeInput.getPassword())) {
            if (Objects.nonNull(connection)) {
                res = true;
            }
        } catch (Exception e) {
            log.error("jdbc ping get link exception", e);
            res = false;
        }
        return res;
    }
}
