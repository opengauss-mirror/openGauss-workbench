/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2024-2024. All rights reserved.
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
 * ProcessStatus.java
 *
 * IDENTIFICATION
 * plugins/data-migration/src/main/java/org/opengauss/admin/plugin/vo/ProcessStatus.java
 *
 * -------------------------------------------------------------------------
 */

package org.opengauss.admin.plugin.vo;

import lombok.Data;

/**
 * The ps command in Linux is used to provide information about the currently running processes,
 * including their process identification numbers (PIDs), the terminal associated with them,
 * and the current state. Here are some commonly used options with the ps command:
 *
 * ps: This command alone lists the processes running for the current shell.
 *
 * @author: wangchao
 * @Date: 2024/12/30 11:55
 * @since 7.0.0
 **/
@Data
public class ProcessStatus {
    private String name;
    private String uid;
    private String pid;
    private String cmd;

    public ProcessStatus(String uid, String pid, String cmd) {
        this.uid = uid;
        this.pid = pid;
        this.cmd = cmd;
    }
}