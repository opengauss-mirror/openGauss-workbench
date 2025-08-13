/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
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
