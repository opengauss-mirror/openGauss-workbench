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
 * SSHBody.java
 *
 * IDENTIFICATION
 * openGauss-visualtool/visualtool-common/src/main/java/org/opengauss/admin/common/core/domain/model/ops/host/SSHBody.java
 *
 * -------------------------------------------------------------------------
 */


package org.opengauss.admin.common.core.domain.model.ops.host;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * @author lhf
 * @date 2022/8/8 13:57
 **/
@Data
@AllArgsConstructor
public class SSHBody {
    private String ip;

    private Integer sshPort;

    private String sshUsername;

    private String sshPassword;

    private String businessId;
}
