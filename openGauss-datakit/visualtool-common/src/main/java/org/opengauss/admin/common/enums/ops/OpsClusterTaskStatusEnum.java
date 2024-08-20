/*
 * Copyright (c) 2024 Huawei Technologies Co.,Ltd.
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
 * OpsClusterTaskStatusEnum.java
 *
 * IDENTIFICATION
 *  openGauss-datakit/visualtool-common/src/main/java/org/opengauss/admin/common/enums/ops/OpsClusterTaskStatusEnum.java
 *
 * -------------------------------------------------------------------------
 */

package org.opengauss.admin.common.enums.ops;

import java.util.Objects;

/**
 * OpsClusterTaskStatusEnum
 *
 * @author wangchao
 * @date 2024/06/15 09:26
 */
public enum OpsClusterTaskStatusEnum {

    // 任务状态
    /**
     * 草稿
     */
    DRAFT("DRAFT"),
    /**
     * 待执行
     */
    PENDING("PENDING"),
    /**
     * 执行等待中
     */
    WAITING("WAITING"),
    /**
     * 执行中
     */
    RUNNING("RUNNING"),
    /**
     * 执行成功
     */
    SUCCESS("SUCCESS"),
    /**
     * 执行失败
     */
    FAILED("FAILED"),
    /**
     * 执行取消
     */
    CANCELED("CANCELED"),
    /**
     * 未知 任务不存在
     */
    UNKNOWN("UNKNOWN");

    private String status;

    OpsClusterTaskStatusEnum(String status) {
        this.status = status;
    }

    public String getStatus() {
        return status;
    }


    public static boolean isPending(OpsClusterTaskStatusEnum taskStatusEnum) {
        return Objects.equals(taskStatusEnum, PENDING);
    }

    public static boolean isFailed(OpsClusterTaskStatusEnum taskStatusEnum) {
        return Objects.equals(taskStatusEnum, FAILED) || Objects.equals(taskStatusEnum, CANCELED);
    }
}
