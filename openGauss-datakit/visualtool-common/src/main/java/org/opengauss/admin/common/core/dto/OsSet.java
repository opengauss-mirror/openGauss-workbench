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

package org.opengauss.admin.common.core.dto;

import lombok.Data;
import lombok.AllArgsConstructor;

import java.util.HashSet;

/**
 * used only for Json to Frontend
 *
 * @author chenyuchen2024
 * @date 24/02/22 21:12
 **/
@Data
@AllArgsConstructor
public class OsSet {

    /**
     * os set based on centos
     **/
    private HashSet<String> centosBasedSet;

    /**
     * os set based on openEuler
     **/
    private HashSet<String> openEulerBasedSet;

    /**
     * check whether os is based on centos
     */
    public boolean isCentos(String os) {
        return centosBasedSet.contains(os);
    }

    /**
     * check whether os is based on openEuler
     */
    public boolean isOpenEuler(String os) {
        return openEulerBasedSet.contains(os);
    }
}
