/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2024-2024. All rights reserved.
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
 * K8sClusterServiceImpl.java
 *
 * IDENTIFICATION
 * plugins/container-management-plugin/src/main/java/org/opengauss/admin/container/service/impl/K8sClusterServiceImpl
 * .java
 *
 * -------------------------------------------------------------------------
 */

package org.opengauss.admin.container.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.opengauss.admin.container.beans.K8sCluster;
import org.opengauss.admin.container.beans.OpenGaussCluster;
import org.opengauss.admin.container.constant.CommonConstant;
import org.opengauss.admin.container.mapper.K8sClusterMapper;
import org.opengauss.admin.container.mapper.OpenGaussClusterMapper;
import org.opengauss.admin.container.service.K8sClusterService;
import org.opengauss.admin.container.service.cache.K8sClusterCacheManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * K8sClusterServiceImpl
 *
 * @since 2024-08-29
 */
@Slf4j
@Service
public class K8sClusterServiceImpl implements K8sClusterService {
    @Autowired
    private K8sClusterCacheManager k8sClusterCacheManager;
    @Autowired
    private K8sClusterMapper k8sClusterMapper;
    @Autowired
    private OpenGaussClusterMapper openGaussClusterMapper;

    /**
     * convertListToPage
     *
     * @param coll     list
     * @param startInt pageNum
     * @param pageSize pageSize
     * @return map
     */
    public static Map<String, Object> convertListToPage(List<?> coll, int startInt, int pageSize) {
        Map<String, Object> pageResult = new HashMap<>();
        if (CollectionUtil.isEmpty(coll)) {
            pageResult.put("data", Collections.emptyList());
            pageResult.put("total", 0);
            return pageResult;
        }
        int size = coll.size();
        int startIndex = startInt == 0 ? startInt : startInt - 1;
        if (startIndex >= size) {
            pageResult.put("data", Collections.emptyList());
        } else {
            int endInt = startIndex + pageSize;
            endInt = endInt > size ? size : endInt;
            List<?> pageData = coll.subList(startIndex, endInt);
            pageResult.put("data", pageData);
        }
        pageResult.put("total", size);
        return pageResult;
    }

    @Override
    public String addK8sClusterInfo(K8sCluster k8sCluster) {
        // 参数判空
        if (isK8sClusterAddParNull(k8sCluster)) {
            return "参数不能为空";
        }

        // 集群ID唯一
        if (k8sClusterMapper.selectById(k8sCluster.getId()) != null) {
            return "添加失败，k8s集群ID重复";
        }

        // 集群name唯一
        LambdaQueryWrapper<K8sCluster> queryWrapper = Wrappers.lambdaQuery();
        queryWrapper.eq(K8sCluster::getName, k8sCluster.getName());
        if (k8sClusterMapper.selectCount(queryWrapper) > 0) {
            return "添加失败，k8s集群名重复";
        }

        // 默认启用k8s集群
        k8sCluster.setIsEnable(true);
        Date now = new Date();
        k8sCluster.setCreateTime(now);
        k8sCluster.setUpdateTime(now);

        // 入库
        if (k8sClusterMapper.insert(k8sCluster) != 1) {
            return "添加失败，入库异常";
        }

        // 重置缓存
        k8sClusterCacheManager.resetCluster();
        return CommonConstant.RETURN_CODE_SUCCESS;
    }

    private boolean isK8sClusterAddParNull(K8sCluster k8sCluster) {
        return StringUtils.isBlank(k8sCluster.getId())
                || StringUtils.isBlank(k8sCluster.getName())
                || StringUtils.isBlank(k8sCluster.getApiServer())
                || k8sCluster.getPort() == null
                || StringUtils.isBlank(k8sCluster.getDomain())
                || StringUtils.isBlank(k8sCluster.getPrometheusUrl())
                || StringUtils.isBlank(k8sCluster.getHarborAddress())
                || StringUtils.isBlank(k8sCluster.getResourcePool());
    }

    @Override
    public String deleteK8sClusterInfo(String k8sId) {
        if (k8sId == null) {
            return "参数为空";
        }
        if (k8sClusterMapper.selectById(k8sId) == null) {
            return "删除失败，不存在该集群信息";
        }

        // 检查集群是否在使用中
        LambdaQueryWrapper<OpenGaussCluster> queryWrapper = Wrappers.lambdaQuery();
        queryWrapper.eq(OpenGaussCluster::getK8sId, k8sId);
        if (openGaussClusterMapper.selectCount(queryWrapper) > 0) {
            return "删除失败，k8s集群使用中!";
        }
        if (k8sClusterMapper.deleteById(k8sId) != 1) {
            return "删除失败，数据库操作异常";
        }

        // 重置缓存
        k8sClusterCacheManager.resetCluster();
        return CommonConstant.RETURN_CODE_SUCCESS;
    }

    @Override
    public String updateK8sClusterInfo(K8sCluster k8sCluster) {
        if (k8sCluster.getId() == null) {
            return "参数为空";
        }
        boolean isUpdateRecordFlg = false;
        // 根据id查询
        K8sCluster k8sClusterDb = k8sClusterMapper.selectById(k8sCluster.getId());
        if (k8sClusterDb == null) {
            return "更新失败，不存在该k8s集群";
        }

        // 更新数据(目前只允许修改enable、资源池两个字段)
        if (k8sCluster.getIsEnable() != null) {
            k8sClusterDb.setIsEnable(k8sCluster.getIsEnable());
            isUpdateRecordFlg = true;
        }
        if (k8sCluster.getResourcePool() != null) {
            k8sClusterDb.setResourcePool(k8sCluster.getResourcePool());
            isUpdateRecordFlg = true;
        }
        if (!isUpdateRecordFlg) {
            return CommonConstant.RETURN_CODE_SUCCESS;
        }
        k8sClusterDb.setUpdateTime(new Date());
        // 更新数据库
        if (k8sClusterMapper.updateById(k8sClusterDb) != 1) {
            return "更新失败，数据库操作异常";
        }
        // 重置缓存
        k8sClusterCacheManager.resetCluster();
        return CommonConstant.RETURN_CODE_SUCCESS;
    }

    @Override
    public List<K8sCluster> getK8sClusterInfo(String k8sId) {
        List<K8sCluster> results;
        if (StringUtils.isNotBlank(k8sId)) {
            results = Collections.emptyList();
            K8sCluster cluster = k8sClusterCacheManager.getCluster(k8sId);
            if (cluster != null) {
                results = new ArrayList<>(1);
                results.add(cluster);
            }
        } else {
            results = k8sClusterCacheManager.listCluster();
            if (results.isEmpty()) {
                results = k8sClusterCacheManager.initK8sClusterCache();
            }
        }
        return results;
    }

    @Override
    public Map<String, Object> getK8sClusterPageInfo(String k8sId, Integer pageNum, Integer pageSize) {
        List<K8sCluster> results;
        if (StringUtils.isNotBlank(k8sId)) {
            results = Collections.emptyList();
            K8sCluster cluster = k8sClusterCacheManager.getCluster(k8sId);
            if (cluster != null) {
                results = new ArrayList<>(1);
                results.add(cluster);
            }
        } else {
            results = k8sClusterCacheManager.listCluster();
            if (results.isEmpty()) {
                results = k8sClusterCacheManager.initK8sClusterCache();
            }
        }
        return convertListToPage(results, pageNum, pageSize);
    }
}
