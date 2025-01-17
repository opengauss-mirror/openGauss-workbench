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
 *  PrivilegeServiceImpl.java
 *
 *  IDENTIFICATION
 *  plugins/data-studio/src/main/java/com/nctigba/datastudio/service/impl/sql/PrivilegeServiceImpl.java
 *
 *  -------------------------------------------------------------------------
 */

package com.nctigba.datastudio.service.impl.sql;

import com.alibaba.fastjson.JSON;
import com.nctigba.datastudio.compatible.PrivilegeSQLService;
import com.nctigba.datastudio.config.ConnectionConfig;
import com.nctigba.datastudio.dao.DbSetPrivilegeHistoryDAO;
import com.nctigba.datastudio.model.dto.PrivilegeHistoryDTO;
import com.nctigba.datastudio.model.entity.PrivilegeHistoryDO;
import com.nctigba.datastudio.model.query.PrivilegeHistoryQuery;
import com.nctigba.datastudio.model.query.PrivilegeSetQuery;
import com.nctigba.datastudio.service.PrivilegeService;
import lombok.extern.slf4j.Slf4j;
import org.opengauss.admin.common.exception.CustomException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.nctigba.datastudio.utils.DebugUtils.comGetUuidType;

/**
 * PrivilegeServiceImpl
 *
 * @author liupengfei
 * @since 2024/10/28
 */
@Service
@Slf4j
public class PrivilegeServiceImpl implements PrivilegeService {
    private Map<String, PrivilegeSQLService> privilegeSQLService;
    @Autowired
    private ConnectionConfig connectionConfig;
    @Autowired
    private DbSetPrivilegeHistoryDAO dbSetPrivilegeHistoryDAO;

    /**
     * setPrivilegeService
     *
     * @param sqlServicesList implements PrivilegeSQLService
     */
    @Resource
    public void setPrivilegeService(List<PrivilegeSQLService> sqlServicesList) {
        privilegeSQLService = new HashMap<>();
        for (PrivilegeSQLService service : sqlServicesList) {
            privilegeSQLService.put(service.type(), service);
        }
    }

    @Override
    public String getPrivilegeSql(PrivilegeSetQuery request) {
        return privilegeSQLService.get(comGetUuidType(request.getUuid())).getPrivilegeSql(request);
    }

    @Override
    public void setPrivilege(PrivilegeSetQuery request) {
        String privilegeSql = getPrivilegeSql(request);
        PrivilegeHistoryDO history = new PrivilegeHistoryDO();
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        history.setStartTime(df.format(new Date()));
        history.setSql(privilegeSql);
        history.setPrivilegeSetQuery(JSON.toJSONString(request));
        try (
                Connection connection = connectionConfig.connectDatabase(request.getUuid());
                Statement statement = connection.createStatement();
        ) {
            log.info("PrivilegeServiceImpl setPrivilege SQL: {}", privilegeSql);
            statement.execute(privilegeSql);
            history.setSuccess(true);
        } catch (SQLException e) {
            log.error(e.getMessage());
            history.setErrMes(e.getMessage());
            history.setSuccess(false);
            throw new CustomException(e.getMessage(), e);
        } finally {
            dbSetPrivilegeHistoryDAO.insertTable(history);
        }
    }

    @Override
    public PrivilegeHistoryDTO getPrivilegeHistory(PrivilegeHistoryQuery request) {
        return dbSetPrivilegeHistoryDAO.queryTable(request);
    }
}
