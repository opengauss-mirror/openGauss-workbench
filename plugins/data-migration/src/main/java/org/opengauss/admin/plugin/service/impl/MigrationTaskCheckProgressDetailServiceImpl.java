/*
 * Copyright (c) 2026 Huawei Technologies Co., Ltd.
 * openGauss-workbench is licensed under Mulan PSL v2.
 * You can use this software according to the terms and conditions of the Mulan PSL v2.
 * You may obtain a copy of Mulan PSL v2 at:
 *          http://license.coscl.org.cn/MulanPSL2
 * THIS SOFTWARE IS PROVIDED ON AN "AS IS" BASIS, WITHOUT WARRANTIES OF ANY KIND,
 * EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO NON-INFRINGEMENT,
 * MERCHANTABILITY OR FIT FOR A PARTICULAR PURPOSE.
 * See the Mulan PSL v2 for more details.
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
