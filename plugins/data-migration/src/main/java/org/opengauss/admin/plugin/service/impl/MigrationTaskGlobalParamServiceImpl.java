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
 * MigrationTaskGlobalParamServiceImpl.java
 *
 * IDENTIFICATION
 * data-migration/src/main/java/org/opengauss/admin/plugin/service/impl/MigrationTaskGlobalParamServiceImpl.java
 *
 * -------------------------------------------------------------------------
 */


package org.opengauss.admin.plugin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.opengauss.admin.plugin.domain.MigrationTaskGlobalParam;
import org.opengauss.admin.plugin.mapper.MigrationTaskGlobalParamMapper;
import org.opengauss.admin.plugin.service.MigrationTaskGlobalParamService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author xielibo
 * @date 2023/01/14 09:01
 **/
@Service
@Slf4j
public class MigrationTaskGlobalParamServiceImpl extends ServiceImpl<MigrationTaskGlobalParamMapper, MigrationTaskGlobalParam> implements MigrationTaskGlobalParamService {

    @Override
    public List<MigrationTaskGlobalParam> selectByMainTaskId(Integer mainTaskId) {
        LambdaQueryWrapper<MigrationTaskGlobalParam> query = new LambdaQueryWrapper<>();
        query.eq(MigrationTaskGlobalParam::getMainTaskId, mainTaskId);
        return this.list(query);
    }

    @Override
    public void deleteByMainTaskId(Integer mainTaskId) {
        LambdaQueryWrapper<MigrationTaskGlobalParam> query = new LambdaQueryWrapper<>();
        query.eq(MigrationTaskGlobalParam::getMainTaskId, mainTaskId);
        this.remove(query);
    }
}
