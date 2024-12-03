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
 * OpenGaussImageService.java
 *
 * IDENTIFICATION
 * plugins/container-management-plugin/src/main/java/org/opengauss/admin/container/service/OpenGaussImageService.java
 *
 * -------------------------------------------------------------------------
 */

package org.opengauss.admin.container.service;

import org.opengauss.admin.container.beans.OpenGaussImage;
import org.opengauss.admin.container.service.dto.OpenGaussImageQuery;

import java.util.List;
import java.util.Map;

/**
 * OpenGaussImageService
 *
 * @since 2024-08-29
 */
public interface OpenGaussImageService {
    /**
     * 添加镜像
     *
     * @param openGaussImage 镜像
     * @return 异常信息
     */
    String add(OpenGaussImage openGaussImage);

    /**
     * 修改镜像
     *
     * @param openGaussImage 镜像
     * @return 异常信息
     */
    String update(OpenGaussImage openGaussImage);

    /**
     * 删除镜像
     *
     * @param id id
     * @return 异常信息
     */
    String deleteById(Integer id);

    /**
     * 查询镜像
     *
     * @param query OpenGaussImageQuery
     * @param pageNum pageNum
     * @param pageSize pageSize
     * @return Map
     */
    @SuppressWarnings("all")
    Map<String, Object> select(OpenGaussImageQuery query, int pageNum, int pageSize);

    /**
     * 查询
     *
     * @param type         type
     * @param architecture OpenGaussImageService
     * @param os           os
     * @param version      version
     * @param isEnabled    isEnable
     * @return list
     */
    List<OpenGaussImage> selectImageList(String type, String architecture, String os, String version,
                                         Boolean isEnabled);
}
