/*
 *  Copyright (c) GBA-NCTI-ISDC. 2022-2024.
 *
 *  openGauss DataKit is licensed under Mulan PSL v2.
 *  You can use this software according to the terms and conditions of the Mulan PSL v2.
 *  You may obtain a copy of Mulan PSL v2 at:
 *
 *  http://license.coscl.org.cn/MulanPSL2
 *
 *  THIS SOFTWARE IS PROVIDED ON AN "AS IS" BASIS, WITHOUT WARRANTIES OF ANY KIND,
 *  EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO NON-INFRINGEMENT,
 *  MERCHANTABILITY OR FITFOR A PARTICULAR PURPOSE.
 *  See the Mulan PSL v2 for more details.
 *  -------------------------------------------------------------------------
 *
 *  BlockSessionDTO.java
 *
 *  IDENTIFICATION
 *  plugins/observability-sql-diagnosis/src/main/java/com/nctigba/observability/sql/model/dto/point/BlockSessionDTO.java
 *
 *  -------------------------------------------------------------------------
 */

package com.nctigba.observability.sql.model.dto.point;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * BlockSessionDTO
 *
 * @author luomeng
 * @since 2023/8/22
 */
@Data
@Accessors(chain = true)
public class BlockSessionDTO {
    private String startTime;
    private String endTime;
    private String wSessionId;
    private String wPid;
    private String lSessionId;
    private String lPid;
    private String lUser;
    private String lockingQuery;
    private String tableName;
    private String applicationName;
    private String clientAddress;
    private String state;
    private String mode;
}
