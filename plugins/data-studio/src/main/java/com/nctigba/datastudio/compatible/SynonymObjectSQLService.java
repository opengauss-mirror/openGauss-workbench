/*
 * Copyright (c) 2026 Huawei Technologies Co., Ltd.
 * openGauss-workbench is licensed under Mulan PSL v2.
 * You can use this software according to the terms and conditions of the Mulan PSL v2.
 * You may obtain a copy of Mulan PSL v2 at:
 *          http://license.coscl.org.cn/MulanPSL2
 * THIS SOFTWARE IS PROVIDED ON AN "AS IS" BASIS, WITHOUT WARRANTIES OF ANY KIND,
 * EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO NON-INFRINGEMENT,
 * MERCHANTABILITY OR FIT FOR A PARTICULAR PURPOSE.
 * See the Mulan PSL v2 for more details.
 */

package com.nctigba.datastudio.compatible;

import com.nctigba.datastudio.model.dto.DatabaseCreateSynonymDTO;
import com.nctigba.datastudio.model.dto.DatabaseDropSynonymDTO;
import com.nctigba.datastudio.model.dto.DatabaseSynonymAttributeDTO;
import com.nctigba.datastudio.utils.DebugUtils;
import org.opengauss.admin.common.exception.CustomException;

/**
 *  SynonymObjectSQLService
 *
 * @since 2023-06-25
 */
public interface SynonymObjectSQLService {
    /**
     * type
     *
     * @return String
     */
    String type();

    /**
     * splicing synonym ddl
     *
     * @param request request
     * @return String
     */
    default String splicingSynonymDDL(DatabaseCreateSynonymDTO request) {
        throw new CustomException(DebugUtils.getMessage());
    }


    /**
     * synonym attribute sql
     *
     * @param request request
     * @return String
     */
    default String synonymAttributeSQL(DatabaseSynonymAttributeDTO request) {
        throw new CustomException(DebugUtils.getMessage());
    }

    /**
     * drop synonym sql
     *
     * @param request request
     * @return String
     */
    default String dropSynonymSQL(DatabaseDropSynonymDTO request) {
        throw new CustomException(DebugUtils.getMessage());
    }
}
