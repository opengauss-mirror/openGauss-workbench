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
 * OpsHostTagService.java
 *
 * IDENTIFICATION
 * openGauss-visualtool/visualtool-service/src/main/java/org/opengauss/admin/system/service/ops/impl/OpsHostTagService.java
 *
 * -------------------------------------------------------------------------
 */


package org.opengauss.admin.system.service.ops.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.opengauss.admin.common.core.domain.entity.ops.OpsHostTagEntity;
import org.opengauss.admin.common.core.domain.model.ops.host.tag.HostTagInputDto;
import org.opengauss.admin.common.core.domain.model.ops.host.tag.HostTagPageVO;
import org.opengauss.admin.common.exception.ops.OpsException;
import org.opengauss.admin.system.mapper.ops.OpsHostTagMapper;
import org.opengauss.admin.system.service.ops.IOpsHostTagRelService;
import org.opengauss.admin.system.service.ops.IOpsHostTagService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author lhf
 * @date 2023/3/14 23:39
 **/
@Service
public class OpsHostTagService extends ServiceImpl<OpsHostTagMapper, OpsHostTagEntity> implements IOpsHostTagService {
    @Autowired
    @Lazy
    private IOpsHostTagRelService opsHostTagRelService;
    @Autowired
    private OpsHostTagMapper opsHostTagMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public synchronized void addTag(HostTagInputDto hostTagInputDto) {
        if (CollUtil.isEmpty(hostTagInputDto.getHostIds()) || CollUtil.isEmpty(hostTagInputDto.getNames())){
            return;
        }
        List<OpsHostTagEntity> tags = buildTags(hostTagInputDto.getNames());
        opsHostTagRelService.addHostTagRel(hostTagInputDto.getHostIds(),tags);
    }

    @Override
    public IPage<HostTagPageVO> page(Page page, String name) {
        IPage<HostTagPageVO> tagPageVOIPage = opsHostTagMapper.page(page, name);
        if (tagPageVOIPage.getRecords().size() != 0) {
            List<HostTagPageVO> records = tagPageVOIPage.getRecords();
            for (HostTagPageVO record : records) {
                if (record.getRelNum() == null) {
                    record.setRelNum(0);
                }
            }
        }
        return tagPageVOIPage;
    }

    @Override
    public synchronized void add(String name) {
        if (exist(name)){
            throw new OpsException("tag already exists");
        }
        buildTags(Arrays.asList(name));
    }

    @Override
    public void update(String tagId, String name) {
        OpsHostTagEntity tagEntity = getById(tagId);
        if (Objects.isNull(tagEntity)){
            throw new OpsException("tag does not exist");
        }

        if (StrUtil.isEmpty(name)){
            throw new OpsException("Tag name cannot be empty");
        }

        tagEntity.setName(name);
        tagEntity.setUpdateTime(new Date());
        updateById(tagEntity);
    }

    private boolean exist(String name) {
        if (StrUtil.isEmpty(name)){
            throw new OpsException("Tag name cannot be empty");
        }
        LambdaQueryWrapper<OpsHostTagEntity> queryWrapper = Wrappers.lambdaQuery(OpsHostTagEntity.class)
                .eq(OpsHostTagEntity::getName, name);

        return CollUtil.isNotEmpty(list(queryWrapper));
    }

    private List<OpsHostTagEntity> buildTags(List<String> names) {
        List<OpsHostTagEntity> res = new ArrayList<>();
        LambdaQueryWrapper<OpsHostTagEntity> queryWrapper = Wrappers.lambdaQuery(OpsHostTagEntity.class)
                .in(OpsHostTagEntity::getName, names);

        List<OpsHostTagEntity> existTags = list(queryWrapper);

        if (CollUtil.isNotEmpty(existTags)){
            res.addAll(existTags);
            names.removeAll(existTags.stream().map(OpsHostTagEntity::getName).collect(Collectors.toList()));
        }

        if (CollUtil.isNotEmpty(names)){
            List<OpsHostTagEntity> newTags = new ArrayList<>();
            for (String name : names) {
                OpsHostTagEntity opsHostTagEntity = new OpsHostTagEntity();
                opsHostTagEntity.setName(name);
                opsHostTagEntity.setCreateTime(new Date());
                newTags.add(opsHostTagEntity);
            }

            saveBatch(newTags);
            res.addAll(newTags);
        }

        return res;
    }
}
