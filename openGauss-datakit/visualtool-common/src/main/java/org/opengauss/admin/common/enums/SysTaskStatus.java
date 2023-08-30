/*
 * Copyright (c) 2022 Huawei Technologies Co.,Ltd.
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
 * SysTaskStatus.java
 *
 * IDENTIFICATION
 * openGauss-visualtool/visualtool-common/src/main/java/org/opengauss/admin/common/enums/SysTaskStatus.java
 *
 * -------------------------------------------------------------------------
 */


package org.opengauss.admin.common.enums;

/**
 * task status
 *
 * @author xielibo
 */
public enum SysTaskStatus {

    CREATED(0, "created"),
    PROCESSING(1, "processing"),
    FINISH(2, "finish"),
    EXEC_FAIL(3, "execution failed"),
    STOP(4, "stop")
    ;

    private final Integer code;
    private final String info;

    SysTaskStatus(Integer code, String info) {
        this.code = code;
        this.info = info;
    }

    public Integer getCode() {
        return code;
    }

    public String getInfo() {
        return info;
    }
}
