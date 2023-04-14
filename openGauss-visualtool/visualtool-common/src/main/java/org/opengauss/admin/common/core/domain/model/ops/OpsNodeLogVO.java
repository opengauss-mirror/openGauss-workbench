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
 * OpsNodeLogVO.java
 *
 * IDENTIFICATION
 * openGauss-visualtool/visualtool-common/src/main/java/org/opengauss/admin/common/core/domain/model/ops/OpsNodeLogVO.java
 *
 * -------------------------------------------------------------------------
 */


package org.opengauss.admin.common.core.domain.model.ops;

import lombok.Data;

/**
 * https://opengauss.org/zh/docs/3.0.0/docs/Administratorguide/%E6%97%A5%E5%BF%97%E5%8F%82%E8%80%83.html
 *
 * @author lhf
 * @date 2022/10/12 17:08
 **/
@Data
public class OpsNodeLogVO {
    /**
     * System Log path
     */
    private String systemLogPath;
    /**
     * Path of operation logs. If the environment variable $GAUSSLOG does not exist or is empty, tool logs are not recorded in corresponding tool log files and are only printed on the screen
     */
    private String opLogPath;
    /**
     * Black Box Log Path
     */
    private String dumpLogPath;
    /**
     * Performance log path
     */
    private String performanceLogPath;
}
