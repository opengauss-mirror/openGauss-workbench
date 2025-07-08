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
 */

package org.opengauss.admin.common.core.domain.entity.agent;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;

/**
 * GenericCompositeKey
 *
 * @author: wangchao
 * @Date: 2025/4/17 16:01
 * @Description: GenericCompositeKey
 * @since 7.0.0-RC2
 **/
@EqualsAndHashCode
@AllArgsConstructor
@NoArgsConstructor
public class GenericCompositeKey<T1, T2, T3, T4> implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private T1 t1;
    private T2 t2;
    private T3 t3;
    private T4 t4;

    @Override
    public String toString() {
        return t1 + "," + t2 + "," + t3 + "," + t4;
    }
}
