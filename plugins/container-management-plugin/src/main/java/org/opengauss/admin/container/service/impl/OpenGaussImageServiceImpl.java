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
 * OpenGaussImageServiceImpl.java
 *
 * IDENTIFICATION
 * plugins/container-management-plugin/src/main/java/org/opengauss/admin/container/service/impl
 * /OpenGaussImageServiceImpl.java
 *
 * -------------------------------------------------------------------------
 */

package org.opengauss.admin.container.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.opengauss.admin.container.beans.OpenGaussImage;
import org.opengauss.admin.container.constant.CommonConstant;
import org.opengauss.admin.container.mapper.OpenGaussImageMapper;
import org.opengauss.admin.container.service.OpenGaussImageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.opengauss.admin.container.service.dto.OpenGaussImageQuery;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * OpenGaussImageServiceImpl
 *
 * @since 2024-08-29
 */
@Service
public class OpenGaussImageServiceImpl implements OpenGaussImageService {
    @Autowired
    OpenGaussImageMapper openGaussImageMapper;

    @Override
    public String add(OpenGaussImage openGaussImage) {
        if (isOpenGaussImageAddParNull(openGaussImage)) {
            return "参数不能为空";
        }
        // 镜像不能重复（type、architecture、os、version、image）
        LambdaQueryWrapper<OpenGaussImage> queryWrapper = Wrappers.lambdaQuery();
        queryWrapper.eq(OpenGaussImage::getType, openGaussImage.getType())
                .eq(OpenGaussImage::getArchitecture, openGaussImage.getArchitecture())
                .eq(OpenGaussImage::getOs, openGaussImage.getOs())
                .eq(OpenGaussImage::getVersion, openGaussImage.getVersion())
                .eq(OpenGaussImage::getImage, openGaussImage.getImage());
        if (openGaussImageMapper.selectOne(queryWrapper) != null) {
            return "添加失败，镜像重复";
        }
        // 参数设置
        openGaussImage.setEnable(true);
        Date now = new Date();
        openGaussImage.setCreateTime(now);
        openGaussImage.setUpdateTime(now);
        if (openGaussImageMapper.insert(openGaussImage) != 1) {
            return "镜像添加失败，入库异常";
        }
        return CommonConstant.RETURN_CODE_SUCCESS;
    }

    private boolean isOpenGaussImageAddParNull(OpenGaussImage openGaussImage) {
        return org.apache.commons.lang3.StringUtils.isBlank(openGaussImage.getType())
                || org.apache.commons.lang3.StringUtils.isBlank(openGaussImage.getArchitecture())
                || org.apache.commons.lang3.StringUtils.isBlank(openGaussImage.getOs())
                || org.apache.commons.lang3.StringUtils.isBlank(openGaussImage.getVersion())
                || org.apache.commons.lang3.StringUtils.isBlank(openGaussImage.getImage())
                || openGaussImage.getPriority() == null;
    }

    @Override
    public String update(OpenGaussImage openGaussImage) {
        if (openGaussImage.getId() == null) {
            return "ID为空，修改失败";
        }
        OpenGaussImage openGaussImageByDB = openGaussImageMapper.selectById(openGaussImage.getId());
        if (openGaussImageByDB == null) {
            return "修改失败，查询镜像信息异常";
        }
        Boolean isEnable = openGaussImage.getEnable();
        Integer priority = openGaussImage.getPriority();
        String describe = openGaussImage.getDescribe();

        if (isEnable != null) {
            openGaussImageByDB.setEnable(isEnable);
        }
        if (priority != null) {
            openGaussImageByDB.setPriority(priority);
        }
        if (describe != null) {
            openGaussImageByDB.setDescribe(describe);
        }
        openGaussImageByDB.setUpdateTime(new Date());
        if (openGaussImageMapper.updateById(openGaussImageByDB) != 1) {
            return "数据修改失败，入库异常";
        }
        return CommonConstant.RETURN_CODE_SUCCESS;
    }

    @Override
    public String deleteById(Integer id) {
        if (openGaussImageMapper.selectById(id) == null) {
            return "删除镜像失败，该镜像不存在";
        }
        if (openGaussImageMapper.deleteById(id) != 1) {
            return "删除镜像失败，数据库操作异常";
        }
        return CommonConstant.RETURN_CODE_SUCCESS;
    }

    @SuppressWarnings("all")
    @Override
    public Map<String, Object> select(OpenGaussImageQuery query, int pageNum, int pageSize) {
        Map<String, Object> result = new HashMap<>();
        // 分页
        Page<OpenGaussImage> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<OpenGaussImage> queryWrapper = Wrappers.lambdaQuery();
        queryWrapper.eq(StringUtils.hasText(query.getType()), OpenGaussImage::getType, query.getType())
                .eq(StringUtils.hasText(query.getArchitecture()),
                        OpenGaussImage::getArchitecture, query.getArchitecture())
                .eq(StringUtils.hasText(query.getOs()), OpenGaussImage::getOs, query.getOs())
                .eq(StringUtils.hasText(query.getVersion()), OpenGaussImage::getVersion, query.getVersion())
                .eq(query.getIsEnabled() != null, OpenGaussImage::getEnable, query.getIsEnabled())
                .orderByDesc(OpenGaussImage::getPriority);

        openGaussImageMapper.selectPage(page, queryWrapper);
        result.put("data", page.getRecords());
        result.put("total", page.getTotal());
        return result;
    }

    @Override
    public List<OpenGaussImage> selectImageList(String type, String architecture, String os, String version,
                                                Boolean isEnabled) {
        LambdaQueryWrapper<OpenGaussImage> queryWrapper = Wrappers.lambdaQuery();
        queryWrapper.eq(StringUtils.hasText(type), OpenGaussImage::getType, type)
                .eq(OpenGaussImage::getArchitecture, architecture)
                .eq(OpenGaussImage::getOs, os)
                .eq(OpenGaussImage::getVersion, version)
                .eq(OpenGaussImage::getEnable, isEnabled)
                .orderByDesc(OpenGaussImage::getPriority);
        return openGaussImageMapper.selectList(queryWrapper);
    }
}
