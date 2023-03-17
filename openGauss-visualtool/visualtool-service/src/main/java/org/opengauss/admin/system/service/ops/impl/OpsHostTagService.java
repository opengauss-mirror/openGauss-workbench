package org.opengauss.admin.system.service.ops.impl;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.opengauss.admin.common.core.domain.entity.ops.OpsHostTagEntity;
import org.opengauss.admin.common.core.domain.model.ops.host.tag.HostTagInputDto;
import org.opengauss.admin.system.mapper.ops.OpsHostTagMapper;
import org.opengauss.admin.system.service.ops.IOpsHostTagRelService;
import org.opengauss.admin.system.service.ops.IOpsHostTagService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author lhf
 * @date 2023/3/14 23:39
 **/
@Service
public class OpsHostTagService extends ServiceImpl<OpsHostTagMapper, OpsHostTagEntity> implements IOpsHostTagService {
    @Autowired
    private IOpsHostTagRelService opsHostTagRelService;
    @Override
    @Transactional(rollbackFor = Exception.class)
    public synchronized void addTag(HostTagInputDto hostTagInputDto) {
        if (CollUtil.isEmpty(hostTagInputDto.getHostIds()) || CollUtil.isEmpty(hostTagInputDto.getNames())){
            return;
        }
        List<OpsHostTagEntity> tags = buildTags(hostTagInputDto.getNames());
        opsHostTagRelService.addHostTagRel(hostTagInputDto.getHostIds(),tags);
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
