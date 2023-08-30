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
 * CheckDeviceVO.java
 *
 * IDENTIFICATION
 * base-ops/src/main/java/org/opengauss/admin/plugin/vo/ops/CheckDeviceVO.java
 *
 * -------------------------------------------------------------------------
 */

package org.opengauss.admin.plugin.vo.ops;

import lombok.Data;

/**
 * @author lhf
 * @date 2022/10/21 18:36
 **/
@Data
public class CheckDeviceVO {
    private CheckItemVO checkBlockdev;
    private CheckItemVO checkDiskFormat;
    private CheckItemVO checkSpaceUsage;
    private CheckItemVO checkInodeUsage;
    private CheckItemVO checkSwapMemory;
    private CheckItemVO checkLogicalBlock;
    private CheckItemVO checkIOrequestqueue;
    private CheckItemVO checkMaxAsyIOrequests;
    private CheckItemVO checkIOConfigure;
}
