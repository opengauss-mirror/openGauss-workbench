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

package org.opengauss.jsch.pool;

/**
 * progress callback
 *
 * @author: wangchao
 * @Date: 2025/8/4 14:05
 * @Description: ProgressCallback
 * @since 7.0.0
 **/
public interface ProgressCallback {
    /**
     * progress
     *
     * @param current current
     * @param total total
     * @return percent
     */
    String progress(long current, long total);

    /**
     * completed
     */
    void completed();

    /**
     * failed
     *
     * @param t throwable
     */
    void failed(Throwable t);
}
