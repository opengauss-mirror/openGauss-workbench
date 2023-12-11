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

package org.opengauss.collect.utils.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * RespMybatis
 *
 * @author liu
 * @since 2022-10-01
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RespMybatis {
    private int targetNum;
    private int actuallyNum;

    /**
     * save
     *
     * @param targetNum   targetNum
     * @param actuallyNum actuallyNum
     * @return RespBean RespBean
     */
    public static RespBean save(int targetNum, int actuallyNum) {
        if (targetNum == actuallyNum) {
            return RespBean.success("save success");
        }
        return RespBean.error("save fail");
    }

    /**
     * update
     *
     * @param targetNum   targetNum
     * @param actuallyNum actuallyNum
     * @return RespBean RespBean
     */
    public static RespBean update(int targetNum, int actuallyNum) {
        if (targetNum == actuallyNum) {
            return RespBean.success("update success");
        }
        return RespBean.error("update fail");
    }

    /**
     * update
     *
     * @param targetNum   targetNum
     * @return RespBean RespBean
     */
    public static RespBean delete(int targetNum) {
        if (targetNum > 0) {
            return RespBean.success("delete success");
        }
        return RespBean.error("delete fail");
    }
}
