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

package com.nctigba.observability.sql.model.vo.point;

import com.nctigba.observability.sql.enums.ShowDataEnum;

/**
 * ShowData
 *
 * @author luomeng
 * @since 2023/11/22
 */
public interface ShowData<T> {
    /**
     * Get data type
     *
     * @return ShowDataEnum
     */
    ShowDataEnum getType();

    /**
     * Get data
     *
     * @return Object
     */
    T getData();
}
