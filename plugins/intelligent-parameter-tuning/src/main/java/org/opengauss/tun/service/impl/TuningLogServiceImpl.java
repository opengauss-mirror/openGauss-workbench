/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2012-2022. All rights reserved.
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
 *
 * -------------------------------------------------------------------------
 */

package org.opengauss.tun.service.impl;

import java.util.List;
import org.opengauss.tun.domain.TuningLog;
import org.opengauss.tun.mapper.TuningLogMapper;
import org.opengauss.tun.service.TuningLogService;
import org.opengauss.tun.utils.response.RespBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * TuningLogServiceImpl
 *
 * @author liu
 * @since 2023-12-20
 */
@Service
public class TuningLogServiceImpl implements TuningLogService {
    @Autowired
    private TuningLogMapper logMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int save(TuningLog log) {
        return logMapper.insert(log);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public RespBean deleteLogs(List<String> ids) {
        return RespBean.success("success", logMapper.deleteBatchIds(ids));
    }
}
