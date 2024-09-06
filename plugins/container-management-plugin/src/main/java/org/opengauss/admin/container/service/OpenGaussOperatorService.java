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
 * OpenGaussOperatorService.java
 *
 * IDENTIFICATION
 * plugins/container-management-plugin/src/main/java/org/opengauss/admin/container/service/OpenGaussOperatorService.java
 *
 * -------------------------------------------------------------------------
 */

package org.opengauss.admin.container.service;

import org.opengauss.admin.container.beans.OpenGaussOperator;

import java.util.List;
import java.util.Map;

/**
 * OpenGaussOperatorService
 *
 * @since 2024-08-29
 */
public interface OpenGaussOperatorService {
    /**
     * 从k8s获取openGauss-operator信息
     */
    void syncOperatorFromK8s();

    /**
     * 查询operator信息
     *
     * @param k8sId    id
     * @param type     type
     * @param pageNum  pageNum
     * @param pageSize pageSize
     * @return map
     */
    Map<String, Object> findOperator(String k8sId, String type, int pageNum, int pageSize);

    /**
     * 查询单个operator信息
     *
     * @param operator operator
     * @return OpenGaussOperator
     */
    OpenGaussOperator selectOne(OpenGaussOperator operator);

    /**
     * 推荐operator
     *
     * @param k8sId id
     * @param type  type
     * @return list
     */
    List<OpenGaussOperator> recommendedOperator(String k8sId, String type);

    /**
     * 改变Operator管理数量
     *
     * @param operator OpenGaussOperator
     * @param opt      opt
     * @return 更新结果
     */
    Integer quantityChange(OpenGaussOperator operator, String opt);
}
