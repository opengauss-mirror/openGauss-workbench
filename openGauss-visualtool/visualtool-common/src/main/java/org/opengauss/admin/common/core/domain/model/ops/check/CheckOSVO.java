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
 * CheckOSVO.java
 *
 * IDENTIFICATION
 * openGauss-visualtool/visualtool-common/src/main/java/org/opengauss/admin/common/core/domain/model/ops/check/CheckOSVO.java
 *
 * -------------------------------------------------------------------------
 */


package org.opengauss.admin.common.core.domain.model.ops.check;

import lombok.Data;

/**
 * @author lhf
 * @date 2022/10/21 18:35
 **/
@Data
public class CheckOSVO {
    private CheckItemVO checkEncoding;
    private CheckItemVO checkFirewall;
    private CheckItemVO checkKernelVer;
    private CheckItemVO checkMaxHandle;
    private CheckItemVO checkNTPD;
    private CheckItemVO checkOSVer;
    private CheckItemVO checkSysParams;
    private CheckItemVO checkTHP;
    private CheckItemVO checkTimeZone;
    private CheckItemVO checkCPU;
    private CheckItemVO checkSshdService;
    private CheckItemVO checkSshdConfig;
    private CheckItemVO checkCrondService;
    private CheckItemVO checkStack;
    private CheckItemVO checkSysPortRange;
    private CheckItemVO checkMemInfo;
    private CheckItemVO checkHyperThread;
    private CheckItemVO checkMaxProcMemory;
    private CheckItemVO checkBootItems;
    private CheckItemVO checkKeyProAdj;
    private CheckItemVO checkFilehandle;
    private CheckItemVO checkDropCache;
}
