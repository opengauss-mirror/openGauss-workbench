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
 * CheckDbVO.java
 *
 * IDENTIFICATION
 * base-ops/src/main/java/org/opengauss/admin/plugin/vo/ops/CheckDbVO.java
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
public class CheckDbVO {
    private CheckItemVO checkCurConnCount;
    private CheckItemVO checkCursorNum;
    private CheckItemVO checkPgxcgroup;
    private CheckItemVO checkTableSpace;
    private CheckItemVO checkSysadminUser;
    private CheckItemVO checkHashIndex;
    private CheckItemVO checkPgxcRedistb;
    private CheckItemVO checkNodeGroupName;
    private CheckItemVO checkTDDate;
}
