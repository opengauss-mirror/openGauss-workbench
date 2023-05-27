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
 * OpsHostTagRelService.java
 *
 * IDENTIFICATION
 * openGauss-visualtool/visualtool-service/src/main/java/org/opengauss/admin/system/service/ops/impl/OpsHostTagRelService.java
 *
 * -------------------------------------------------------------------------
 */


package org.opengauss.admin.system.service.ops.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.opengauss.admin.common.core.domain.entity.ops.OpsHostTagEntity;
import org.opengauss.admin.common.core.domain.entity.ops.OpsHostTagRel;
import org.opengauss.admin.common.core.domain.model.ops.host.tag.HostTagInputDto;
import org.opengauss.admin.system.mapper.ops.OpsHostTagRelMapper;
import org.opengauss.admin.system.service.ops.IOpsHostTagRelService;
import org.opengauss.admin.system.service.ops.IOpsHostTagService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author lhf
 * @date 2023/3/14 23:40
 **/
@Service
public class OpsHostTagRelService extends ServiceImpl<OpsHostTagRelMapper, OpsHostTagRel> implements IOpsHostTagRelService {

    @Autowired
    private IOpsHostTagService opsHostTagService;

    @Override
    public void addHostTagRel(List<String> hostIds, List<OpsHostTagEntity> tags) {
        List<OpsHostTagRel> hostTagRelList = new ArrayList<>();
        for (String hostId : hostIds) {
            for (OpsHostTagEntity tag : tags) {
                OpsHostTagRel opsHostTagRel = new OpsHostTagRel();
                opsHostTagRel.setHostId(hostId);
                opsHostTagRel.setTagId(tag.getHostTagId());
                opsHostTagRel.setCreateTime(new Date());
                hostTagRelList.add(opsHostTagRel);
            }
        }

        saveBatch(hostTagRelList);
    }

    @Override
    public Map<String, Set<String>> mapByHostIds(List<String> hostIds) {
        if (CollUtil.isEmpty(hostIds)) {
            return Collections.emptyMap();
        }

        Map<String, Set<String>> tags = new HashMap<>();

        LambdaQueryWrapper<OpsHostTagRel> queryWrapper = Wrappers.lambdaQuery(OpsHostTagRel.class)
                .in(OpsHostTagRel::getHostId, hostIds);

        List<OpsHostTagRel> list = list(queryWrapper);
        if (CollUtil.isNotEmpty(list)) {
            List<String> tagIds = list.stream().map(OpsHostTagRel::getTagId).collect(Collectors.toList());
            List<OpsHostTagEntity> tagEntityList = opsHostTagService.listByIds(tagIds);
            Map<String, String> tagMap = tagEntityList.stream().collect(Collectors.toMap(OpsHostTagEntity::getHostTagId, OpsHostTagEntity::getName));

            for (OpsHostTagRel opsHostTagRel : list) {
                String hostId = opsHostTagRel.getHostId();
                String tagId = opsHostTagRel.getTagId();

                Set<String> tagNames = tags.get(hostId);
                if (Objects.isNull(tagNames)) {
                    tagNames = new HashSet<>();
                    tags.put(hostId, tagNames);
                }
                tagNames.add(tagMap.get(tagId));
            }
        }

        return tags;
    }

    @Override
    public void cleanHostTag(String hostId) {
        if (StrUtil.isEmpty(hostId)) {
            return;
        }
        LambdaQueryWrapper<OpsHostTagRel> queryWrapper = Wrappers.lambdaQuery(OpsHostTagRel.class)
                .eq(OpsHostTagRel::getHostId, hostId);

        remove(queryWrapper);
    }

    @Override
    public void delByTagId(String tagId) {
        if (StrUtil.isEmpty(tagId)) {
            return;
        }

        LambdaQueryWrapper<OpsHostTagRel> queryWrapper = Wrappers.lambdaQuery(OpsHostTagRel.class)
                .eq(OpsHostTagRel::getTagId, tagId);
        remove(queryWrapper);
    }

    @Override
    public void delTagRelation(HostTagInputDto hostTagInputDto) {
        if (CollUtil.isNotEmpty(hostTagInputDto.getNames())
                && CollUtil.isNotEmpty(hostTagInputDto.getHostIds())) {
            LambdaQueryWrapper<OpsHostTagRel> queryWrapper = Wrappers.lambdaQuery(OpsHostTagRel.class)
                    .in(OpsHostTagRel::getHostId, hostTagInputDto.getHostIds())
                    .in(OpsHostTagRel::getTagId, hostTagInputDto.getNames());

            remove(queryWrapper);
        }
    }
}
