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

package com.nctigba.observability.sql.service.impl.collection.ebpf;

import com.nctigba.observability.sql.constant.AgentParamConstants;
import org.springframework.stereotype.Service;

/**
 * FileTopItem
 *
 * @author luomeng
 * @since 2023/7/28
 */
@Service
public class FileTopItem extends EbpfCollectionItem {
    @Override
    public String getHttpParam() {
        return AgentParamConstants.FILE_TOP;
    }
}