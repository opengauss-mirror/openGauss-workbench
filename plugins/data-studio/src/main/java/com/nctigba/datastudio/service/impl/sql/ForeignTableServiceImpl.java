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
 *  ForeignTableServiceImpl.java
 *
 *  IDENTIFICATION
 *  plugins/data-studio/src/main/java/com/nctigba/datastudio/service/impl/sql/ForeignTableServiceImpl.java
 *
 *  -------------------------------------------------------------------------
 */

package com.nctigba.datastudio.service.impl.sql;

import com.nctigba.datastudio.compatible.ForeignTableSqlService;
import com.nctigba.datastudio.model.query.ForeignTableQuery;
import com.nctigba.datastudio.model.query.TableDataEditQuery;
import com.nctigba.datastudio.service.ForeignTableService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.nctigba.datastudio.utils.DebugUtils.comGetUuidType;

/**
 * ForeignTableServiceImpl
 *
 * @since 2023-10-10
 */
@Slf4j
@Service
public class ForeignTableServiceImpl implements ForeignTableService {
    private Map<String, ForeignTableSqlService> foreignTableSqlService;

    /**
     * set foreign table sql service
     *
     * @param sqlServicesList sqlServicesList
     */
    @Resource
    public void setGainObjectSQLService(List<ForeignTableSqlService> sqlServicesList) {
        foreignTableSqlService = new HashMap<>();
        for (ForeignTableSqlService service : sqlServicesList) {
            foreignTableSqlService.put(service.type(), service);
        }
    }

    @Override
    public List<Map<String, String>> queryServer(ForeignTableQuery request) throws SQLException {
        return foreignTableSqlService.get(comGetUuidType(request.getUuid())).queryServer(request);
    }

    @Override
    public void create(ForeignTableQuery request) throws SQLException {
        foreignTableSqlService.get(comGetUuidType(request.getUuid())).create(request);
    }

    @Override
    public void deleteForeignTable(ForeignTableQuery request) throws SQLException {
        foreignTableSqlService.get(comGetUuidType(request.getUuid())).deleteForeignTable(request);
    }

    @Override
    public void deleteForeignServer(ForeignTableQuery request) throws SQLException {
        foreignTableSqlService.get(comGetUuidType(request.getUuid())).deleteForeignServer(request);
    }

    @Override
    public Map<String, String> ddl(ForeignTableQuery request) throws SQLException {
        return foreignTableSqlService.get(comGetUuidType(request.getUuid())).ddl(request);
    }

    @Override
    public List<Map<String, String>> attribute(ForeignTableQuery request) throws SQLException {
        return foreignTableSqlService.get(comGetUuidType(request.getUuid())).attribute(request);
    }

    @Override
    public String test(ForeignTableQuery request) {
        return foreignTableSqlService.get(comGetUuidType(request.getUuid())).test(request);
    }

    @Override
    public void createServer(ForeignTableQuery request) throws SQLException {
        foreignTableSqlService.get(comGetUuidType(request.getUuid())).createServer(request);
    }

    @Override
    public void edit(TableDataEditQuery request) throws SQLException {
        foreignTableSqlService.get(comGetUuidType(request.getUuid())).edit(request);
    }
}
