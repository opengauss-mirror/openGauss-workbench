/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
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
 * MigrationTaskCheckProgressDetailServiceImpl.java
 *
 * IDENTIFICATION
 * plugins/data-migration/src/main/java/org/opengauss/admin/plugin/service/impl
 * /MigrationTaskCheckProgressDetailServiceImpl.java
 *
 * -------------------------------------------------------------------------
 */

package org.opengauss.admin.plugin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import org.opengauss.admin.plugin.domain.MigrationTaskCheckProgressDetail;
import org.opengauss.admin.plugin.mapper.MigrationTaskCheckProgressDetailMapper;
import org.opengauss.admin.plugin.service.MigrationTaskCheckProgressDetailService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * MigrationTaskCheckProgressDetailService
 *
 * @author: wangchao
 * @Date: 2024/12/30 09:53
 * @Description: MigrationTaskCheckProgressService
 * @since 7.0.0
 **/
@Service
public class MigrationTaskCheckProgressDetailServiceImpl
    extends ServiceImpl<MigrationTaskCheckProgressDetailMapper, MigrationTaskCheckProgressDetail>
    implements MigrationTaskCheckProgressDetailService {
    @Override
    public void removeByTaskIds(List<Integer> ids) {
        LambdaQueryWrapper<MigrationTaskCheckProgressDetail> query = Wrappers.lambdaQuery(
            MigrationTaskCheckProgressDetail.class);
        query.in(MigrationTaskCheckProgressDetail::getTaskId, ids);
        remove(query);
    }
}
