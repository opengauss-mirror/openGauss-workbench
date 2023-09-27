/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package com.nctigba.observability.sql.model.param;

import org.springframework.stereotype.Service;

/**
 * ParamInfoTable
 *
 * @author luomeng
 * @since 2023/9/26
 */
@Service
public class ParamInfoTable implements TableDefine {
    /**
     * Get table name
     *
     * @return list
     */
    @Override
    public String getTableName() {
        return "param_info";
    }

    /**
     * Get table column
     *
     * @return list
     */
    @Override
    public String getColumnDef() {
        return "id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, paramType TEXT,"
                + " paramName TEXT, paramDetail TEXT, suggestValue TEXT, defaultValue TEXT,"
                + " unit TEXT, suggestExplain TEXT, diagnosisRule TEXT";
    }
}
