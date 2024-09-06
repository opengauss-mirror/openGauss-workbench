/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2024-2024. All rights reserved.
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
 *  OpenGaussImageController.java
 *
 *  IDENTIFICATION
 *  plugins/container-management-plugin/src/main/java/org/opengauss/admin/container/controller
 * /OpenGaussImageController.java
 *
 *  -------------------------------------------------------------------------
 */

package org.opengauss.admin.container.controller;

import org.opengauss.admin.common.core.domain.AjaxResult;
import org.opengauss.admin.container.beans.OpenGaussImage;
import org.opengauss.admin.container.constant.CommonConstant;
import org.opengauss.admin.container.service.OpenGaussImageService;
import org.opengauss.admin.container.service.dto.OpenGaussImageQuery;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * OpenGaussImageController
 *
 * @since 2024-6-26 16:39
 **/
@RestController
@RequestMapping("/image")
public class OpenGaussImageController {
    @Autowired
    private OpenGaussImageService openGaussImageService;

    /**
     * 新增ogc镜像
     *
     * @param openGaussImage ogc镜像模型
     * @return 创建结果
     */
    @PostMapping("/add")
    public AjaxResult add(@RequestBody OpenGaussImage openGaussImage) {
        String res = openGaussImageService.add(openGaussImage);
        if (CommonConstant.RETURN_CODE_SUCCESS.equals(res)) {
            return AjaxResult.success();
        } else {
            return AjaxResult.error(res);
        }
    }

    /**
     * 更新ogc镜像
     *
     * @param openGaussImage ogc镜像模型
     * @return 更新结果
     */
    @PostMapping("/update")
    public AjaxResult update(@RequestBody OpenGaussImage openGaussImage) {
        String res = openGaussImageService.update(openGaussImage);
        if (CommonConstant.RETURN_CODE_SUCCESS.equals(res)) {
            return AjaxResult.success();
        } else {
            return AjaxResult.error(res);
        }
    }

    /**
     * 删除ogc镜像
     *
     * @param id ogc镜像id
     * @return 删除结果
     */
    @DeleteMapping("/delete/{id}")
    public AjaxResult delete(@PathVariable("id") Integer id) {
        String res = openGaussImageService.deleteById(id);
        if (CommonConstant.RETURN_CODE_SUCCESS.equals(res)) {
            return AjaxResult.success();
        } else {
            return AjaxResult.error(res);
        }
    }

    /**
     * 分页查询ogc镜像
     *
     * @param type         镜像类型
     * @param architecture 集群架构
     * @param os           系统类型
     * @param version      版本
     * @param enable       是否可用
     * @param pageNum      页码
     * @param pageSize     页大小
     * @return ogc镜像列表分页数据
     */
    @GetMapping("/list")
    public AjaxResult list(@RequestParam(value = "type", required = false) String type,
                           @RequestParam(value = "architecture", required = false) String architecture,
                           @RequestParam(value = "os", required = false) String os,
                           @RequestParam(value = "version", required = false) String version,
                           @RequestParam(value = "enable", required = false) Boolean enable,
                           @RequestParam(value = "pageNum", required = true) Integer pageNum,
                           @RequestParam(value = "pageSize", required = true) Integer pageSize) {
        OpenGaussImageQuery query = new OpenGaussImageQuery();
        query.setType(type);
        query.setArchitecture(architecture);
        query.setOs(os);
        query.setVersion(version);
        query.setIsEnabled(enable);
        Map<String, Object> pageMap = openGaussImageService.select(query, pageNum, pageSize);
        return AjaxResult.success(pageMap);
    }
}
