/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package com.nctigba.observability.sql.model.history.point;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * IndexAdvisorDTO
 *
 * @author luomeng
 * @since 2023/8/17
 */
@Data
@Accessors(chain = true)
public class TuningAdvisorDTO {
    ExecPlanDetailDTO firstExplain;
    Object advisor;
    ExecPlanDetailDTO afterExplain;
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
