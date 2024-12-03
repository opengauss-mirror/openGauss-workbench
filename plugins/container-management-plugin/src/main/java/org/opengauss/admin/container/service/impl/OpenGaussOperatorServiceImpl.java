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
 * OpenGaussOperatorServiceImpl.java
 *
 * IDENTIFICATION
 * plugins/container-management-plugin/src/main/java/org/opengauss/admin/container/service/impl
 * /OpenGaussOperatorServiceImpl.java
 *
 * -------------------------------------------------------------------------
 */

package org.opengauss.admin.container.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.kubernetes.client.openapi.models.V1Deployment;
import org.opengauss.admin.container.beans.K8sCluster;
import org.opengauss.admin.container.beans.OpenGaussOperator;
import org.opengauss.admin.container.config.ConstantEnum;
import org.opengauss.admin.container.kubernetes.api.DeploymentApi;
import org.opengauss.admin.container.mapper.OpenGaussOperatorMapper;
import org.opengauss.admin.container.service.OpenGaussOperatorService;
import org.opengauss.admin.container.service.cache.K8sClusterCacheManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;

/**
 * OpenGaussOperatorServiceImpl
 *
 * @since 2024-08-29
 */
@Service
public class OpenGaussOperatorServiceImpl implements OpenGaussOperatorService {
    private static final Logger LOGGER = LoggerFactory.getLogger(OpenGaussOperatorServiceImpl.class);

    @Autowired
    private K8sClusterCacheManager k8sClusterCacheManager;
    @Autowired
    private DeploymentApi deploymentApi;
    @Autowired
    private OpenGaussOperatorMapper openGaussOperatorMapper;
    @Value("${myProps.openGaussOperator.quantity.max:100}")
    private Integer opengaussOperatorDefaultMaxQuantity;

    @Override
    public void syncOperatorFromK8s() {
        // 从缓存查询k8s_cluster信息
        List<K8sCluster> k8sClusters = k8sClusterCacheManager.listCluster();
        for (K8sCluster k8sCluster : k8sClusters) {
            // 从API Server获取Operator Deployment信息
            Map<String, List<V1Deployment>> deploymentMap = getOperatorDeploymentMap(k8sCluster);
            // 从数据库获取所有operator信息
            List<OpenGaussOperator> operatorList = getOpenGaussOperatorByK8sId(k8sCluster.getId());
            // 封装operator map
            Map<String, OpenGaussOperator> operatorMap = buildOperatorMap(k8sCluster, operatorList);
            // 删除k8s不存在的operator数据
            cleanUpInvalidData(k8sCluster, operatorList, deploymentMap);
            // 对新部署的operator入库
            updateOperatorData(deploymentMap, operatorMap);
        }
    }

    private Map<String, OpenGaussOperator> buildOperatorMap(K8sCluster k8sCluster,
                                                            List<OpenGaussOperator> operatorList) {
        Map<String, OpenGaussOperator> operatorMap = new HashMap<>();
        for (OpenGaussOperator openGaussOperator : operatorList) {
            String name = openGaussOperator.getName();
            String type = openGaussOperator.getType();
            String key = operatorMapKey(k8sCluster.getId(), type, name);
            operatorMap.put(key, openGaussOperator);
        }
        return operatorMap;
    }

    private List<OpenGaussOperator> getOpenGaussOperatorByK8sId(String k8sId) {
        LambdaQueryWrapper<OpenGaussOperator> queryWrapper = Wrappers.lambdaQuery();
        queryWrapper.eq(OpenGaussOperator::getK8sClusterId, k8sId);
        return openGaussOperatorMapper.selectList(queryWrapper);
    }

    private void updateOperatorData(Map<String, List<V1Deployment>> deploymentMap,
                                    Map<String, OpenGaussOperator> operatorMap) {
        Date now = new Date();
        for (String key : deploymentMap.keySet()) {
            if (!operatorMap.containsKey(key)) {
                OpenGaussOperator openGaussOperator = new OpenGaussOperator();
                String[] split = key.split(":");
                openGaussOperator.setK8sClusterId(split[0]);
                openGaussOperator.setType(split[1]);
                openGaussOperator.setName(split[2]);
                openGaussOperator.setCurrentManageQuantity(0);
                openGaussOperator.setMaxManageQuantity(opengaussOperatorDefaultMaxQuantity);
                openGaussOperator.setCreateTime(now);
                openGaussOperator.setUpdateTime(now);
                if (openGaussOperatorMapper.insert(openGaussOperator) != 1) {
                    LOGGER.error("Inserting operator data exception");
                }
            }
        }
    }

    private void cleanUpInvalidData(K8sCluster k8sCluster, List<OpenGaussOperator> operatorList,
                                    Map<String, List<V1Deployment>> deploymentMap) {
        for (OpenGaussOperator openGaussOperator : operatorList) {
            String name = openGaussOperator.getName();
            String type = openGaussOperator.getType();
            String key = operatorMapKey(k8sCluster.getId(), type, name);
            if (!deploymentMap.containsKey(key)) {
                if (openGaussOperatorMapper.deleteById(openGaussOperator.getId()) != 1) {
                    LOGGER.error("Abnormal deletion of operator data");
                }
            }
        }
    }

    private Map<String, List<V1Deployment>> getOperatorDeploymentMap(K8sCluster k8sCluster) {
        // 访问API Server获取deploymentList
        String labelSelectorPrd =
                ConstantEnum.OPENGAUSS_OPERATOR_TYPE_KEY.getValue() + "="
                        + ConstantEnum.OPENGAUSS_OPERATOR_TYPE_PRD.getValue();
        String labelSelectorTest =
                ConstantEnum.OPENGAUSS_OPERATOR_TYPE_KEY.getValue() + "="
                        + ConstantEnum.OPENGAUSS_OPERATOR_TYPE_TEST.getValue();

        // 正式使用
        List<V1Deployment> deploymentPrdList = deploymentApi
                .getDeploymentsByNamespacesAndSelectorHttpInfo(k8sCluster,
                        ConstantEnum.OPENGAUSS_OPERATOR_NAMESPACE.getValue(), null, labelSelectorPrd);

        // 测试验证
        List<V1Deployment> deploymentTestList = deploymentApi
                .getDeploymentsByNamespacesAndSelectorHttpInfo(k8sCluster,
                        ConstantEnum.OPENGAUSS_OPERATOR_NAMESPACE.getValue(), null, labelSelectorTest);

        // 封装为Map
        Map<String, List<V1Deployment>> deploymentMap = new HashMap<>();
        for (V1Deployment deployment : deploymentPrdList) {
            String name = deployment.getMetadata().getName();
            String key = operatorMapKey(k8sCluster.getId(), ConstantEnum.OPENGAUSS_OPERATOR_TYPE_PRD.getValue(), name);
            deploymentMap.computeIfAbsent(key, e -> new ArrayList<V1Deployment>()).add(deployment);
        }
        for (V1Deployment deployment : deploymentTestList) {
            String name = deployment.getMetadata().getName();
            String key = operatorMapKey(k8sCluster.getId(), ConstantEnum.OPENGAUSS_OPERATOR_TYPE_TEST.getValue(), name);
            deploymentMap.computeIfAbsent(key, e -> new ArrayList<V1Deployment>()).add(deployment);
        }
        return deploymentMap;
    }

    private String operatorMapKey(String k8sId, String type, String name) {
        return String.format("%s:%s:%s", k8sId, type, name);
    }

    @Override
    public Map<String, Object> findOperator(String k8sId, String type, int pageNum, int pageSize) {
        Map<String, Object> result = new HashMap<>();
        Page<OpenGaussOperator> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<OpenGaussOperator> queryWrapper = Wrappers.lambdaQuery();
        queryWrapper.eq(StringUtils.hasText(k8sId), OpenGaussOperator::getK8sClusterId, k8sId)
                .eq(StringUtils.hasText(type), OpenGaussOperator::getType, type)
                .orderByAsc(OpenGaussOperator::getCurrentManageQuantity);
        openGaussOperatorMapper.selectPage(page, queryWrapper);
        result.put("data", page.getRecords());
        result.put("total", page.getTotal());
        return result;
    }

    @Override
    public OpenGaussOperator selectOne(OpenGaussOperator operator) {
        LambdaQueryWrapper<OpenGaussOperator> queryWrapper = Wrappers.lambdaQuery();
        queryWrapper.eq(StringUtils.hasText(operator.getK8sClusterId()), OpenGaussOperator::getK8sClusterId,
                        operator.getK8sClusterId())
                .eq(StringUtils.hasText(operator.getType()), OpenGaussOperator::getType, operator.getType());
        return openGaussOperatorMapper.selectOne(queryWrapper);
    }

    @Override
    public List<OpenGaussOperator> recommendedOperator(String k8sId, String type) {
        LambdaQueryWrapper<OpenGaussOperator> queryWrapper = Wrappers.lambdaQuery();
        queryWrapper.eq(StringUtils.hasText(k8sId), OpenGaussOperator::getK8sClusterId, k8sId)
                .eq(StringUtils.hasText(type), OpenGaussOperator::getType, type)
                .orderByAsc(OpenGaussOperator::getCurrentManageQuantity);
        return openGaussOperatorMapper.selectList(queryWrapper);
    }

    @Override
    public Integer quantityChange(OpenGaussOperator operator, String opt) {
        if (ConstantEnum.OPERATOR_NUM_ADD.getValue().equals(opt)) {
            operator.setCurrentManageQuantity(operator.getCurrentManageQuantity() + 1);
        } else if (ConstantEnum.OPERATOR_NUM_REDUCE.getValue().equals(opt)) {
            operator.setCurrentManageQuantity(operator.getCurrentManageQuantity() - 1);
        } else {
            return 0;
        }
        operator.setUpdateTime(new Date());
        return openGaussOperatorMapper.updateById(operator);
    }
}
