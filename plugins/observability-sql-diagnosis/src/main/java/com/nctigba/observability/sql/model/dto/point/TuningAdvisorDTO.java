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
 *  TuningAdvisorDTO.java
 *
 *  IDENTIFICATION
 *  plugins/observability-sql-diagnosis/src/main/java/com/nctigba/observability/sql/model/dto/point/TuningAdvisorDTO.java
 *
 *  -------------------------------------------------------------------------
 */

package com.nctigba.observability.sql.model.dto.point;

import com.nctigba.observability.sql.model.vo.point.ExecPlanDetailVO;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * IndexAdvisorVO
 *
 * @author luomeng
 * @since 2023/8/17
 */
@Data
@Accessors(chain = true)
public class TuningAdvisorDTO {
    ExecPlanDetailVO firstExplain;
    Object advisor;
    ExecPlanDetailVO afterExplain;
    String improvement;

    /**
     * IndexTable
     *
     * @author luomeng
     * @since 2023/8/17
     */
    @Data
    @Accessors(chain = true)
    public static class IndexTable {
        private String schema;
        private String tableName;
        private String column;
        private String indexType;
        private String indexSql;
    }
}
