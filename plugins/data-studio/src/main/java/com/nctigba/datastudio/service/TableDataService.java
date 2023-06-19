/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package com.nctigba.datastudio.service;


import com.nctigba.datastudio.model.dto.DatabaseConstraintDTO;
import com.nctigba.datastudio.model.dto.DatabaseConstraintPkDTO;
import com.nctigba.datastudio.model.dto.DatabaseCreUpdColumnDTO;
import com.nctigba.datastudio.model.dto.DatabaseCreateTableDTO;
import com.nctigba.datastudio.model.dto.DatabaseIndexDTO;
import com.nctigba.datastudio.model.dto.TableAlterDTO;
import com.nctigba.datastudio.model.dto.TableAttributeDTO;
import com.nctigba.datastudio.model.dto.TableDataDTO;
import com.nctigba.datastudio.model.dto.TableNameDTO;
import com.nctigba.datastudio.model.query.SelectDataQuery;
import com.nctigba.datastudio.model.query.TableDataEditQuery;
import com.nctigba.datastudio.model.query.TableDataQuery;
import com.nctigba.datastudio.model.query.TablePKDataQuery;

import java.util.List;
import java.util.Map;

public interface TableDataService {
    TableDataDTO tableData(TableDataQuery request) throws Exception;

    Map<String, Object> tableColumn(SelectDataQuery request) throws Exception;

    Map<String, Object> tableIndex(SelectDataQuery request) throws Exception;

    Map<String, Object> tableConstraint(SelectDataQuery request) throws Exception;

    Map<String, String> tableDdl(SelectDataQuery request) throws Exception;


    Map<String, Object> tableColumnEdit(DatabaseCreUpdColumnDTO request) throws Exception;

    void tableDataClose(String winId) throws Exception;

    TablePKDataQuery tableDataEdit(TableDataEditQuery request) throws Exception;

    void tableEditConstraint(DatabaseConstraintDTO request) throws Exception;

    void tableEditPkConstraint(DatabaseConstraintPkDTO request) throws Exception;

    void tableEditIndex(DatabaseIndexDTO request) throws Exception;

    void tableCreate(DatabaseCreateTableDTO request) throws Exception;

    String returnTableCreate(DatabaseCreateTableDTO request) throws Exception;

    void tableTruncate(TableNameDTO request) throws Exception;

    void tableVacuum(TableNameDTO request) throws Exception;

    void tableReindex(TableNameDTO request) throws Exception;

    void tableRename(TableAlterDTO request) throws Exception;

    void tableComment(TableAlterDTO request) throws Exception;

    void tableAlterSchema(TableAlterDTO request) throws Exception;

    void tableDrop(TableNameDTO request) throws Exception;

    void tableAlterTablespace(TableAlterDTO request) throws Exception;

    Map<String, Object> tableSequence(TableNameDTO request) throws Exception;

    List<Map<String, Object>> tableAttribute(TableAttributeDTO request) throws Exception;
}
