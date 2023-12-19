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
 *  ExportServiceImpl.java
 *
 *  IDENTIFICATION
 *  plugins/data-studio/src/main/java/com/nctigba/datastudio/service/impl/sql/ExportServiceImpl.java
 *
 *  -------------------------------------------------------------------------
 */

package com.nctigba.datastudio.service.impl.sql;

import com.nctigba.datastudio.compatible.ExportServiceSqlService;
import com.nctigba.datastudio.config.ConnectionConfig;
import com.nctigba.datastudio.model.query.ExportQuery;
import com.nctigba.datastudio.service.DatabaseFunctionSPService;
import com.nctigba.datastudio.service.DatabaseSequenceService;
import com.nctigba.datastudio.service.DatabaseViewService;
import com.nctigba.datastudio.service.DbConnectionService;
import com.nctigba.datastudio.service.ExportService;
import com.nctigba.datastudio.service.TableDataService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import static com.nctigba.datastudio.utils.DebugUtils.comGetUuidType;

/**
 * ExportServiceImpl
 *
 * @since 2023-6-26
 */
@Slf4j
@Service
public class ExportServiceImpl implements ExportService {
    @Autowired
    private ConnectionConfig connectionConfig;

    @Autowired
    private TableDataService tableDataService;

    @Autowired
    private DatabaseFunctionSPService functionSPService;

    @Autowired
    private DatabaseViewService viewService;

    @Autowired
    private DatabaseSequenceService sequenceService;

    @Autowired
    private DbConnectionService dbConnectionService;

    private Map<String, ExportServiceSqlService> exportServiceSqlService;

    /**
     * set foreign table sql service
     *
     * @param sqlServicesList sqlServicesList
     */
    @Resource
    public void setGainObjectSQLService(List<ExportServiceSqlService> sqlServicesList) {
        exportServiceSqlService = new HashMap<>();
        for (ExportServiceSqlService service : sqlServicesList) {
            exportServiceSqlService.put(service.type(), service);
        }
    }

    @Override
    public void exportTableDdl(ExportQuery request, HttpServletResponse response) throws SQLException, IOException {
        exportServiceSqlService.get(comGetUuidType(request.getUuid())).exportTableDdl(request, response);
    }

    @Override
    public void exportTableData(ExportQuery request, HttpServletResponse response) throws IOException, SQLException {
        exportServiceSqlService.get(comGetUuidType(request.getUuid())).exportTableData(request, response);
    }

    @Override
    public void importTableData(ExportQuery request) throws IOException, SQLException {
        exportServiceSqlService.get(comGetUuidType(request.getUuid())).importTableData(request);
    }

    @Override
    public void exportFunctionDdl(ExportQuery request, HttpServletResponse response) throws SQLException, IOException {
        exportServiceSqlService.get(comGetUuidType(request.getUuid())).exportFunctionDdl(request, response);
    }

    @Override
    public void exportViewDdl(ExportQuery request, HttpServletResponse response) throws SQLException, IOException {
        exportServiceSqlService.get(comGetUuidType(request.getUuid())).exportViewDdl(request, response);
    }

    @Override
    public void exportSequenceDdl(ExportQuery request, HttpServletResponse response) throws SQLException, IOException {
        exportServiceSqlService.get(comGetUuidType(request.getUuid())).exportSequenceDdl(request, response);
    }

    @Override
    public void exportSchemaDdl(ExportQuery request, HttpServletResponse response)
            throws IOException, ExecutionException, InterruptedException {
        exportServiceSqlService.get(comGetUuidType(request.getUuid())).exportSchemaDdl(request, response);
    }
}
