/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package com.nctigba.datastudio.compatible;

import com.nctigba.datastudio.model.dto.CreateDatabaseDTO;
import com.nctigba.datastudio.model.dto.DatabaseNameDTO;
import com.nctigba.datastudio.model.dto.RenameDatabaseDTO;
import com.nctigba.datastudio.util.DebugUtils;
import org.opengauss.admin.common.exception.CustomException;

public interface DatabaseObjectSQLService {
    String type();

    default String createDatabase(CreateDatabaseDTO database) {
        throw new CustomException(DebugUtils.getMessage());
    }

    default String connectionDatabaseTest() {
        throw new CustomException(DebugUtils.getMessage());
    }

    default String deleteDatabaseSQL(DatabaseNameDTO database) {
        throw new CustomException(DebugUtils.getMessage());
    }

    default String renameDatabaseSQL(RenameDatabaseDTO database) {
        throw new CustomException(DebugUtils.getMessage());
    }

    default String conRestrictionsSQL(RenameDatabaseDTO database) {
        throw new CustomException(DebugUtils.getMessage());
    }

    default String databaseAttributeSQL(DatabaseNameDTO database) {
        throw new CustomException(DebugUtils.getMessage());
    }

    default String databaseAttributeUpdateSQL(DatabaseNameDTO database) {
        throw new CustomException(DebugUtils.getMessage());
    }
}
