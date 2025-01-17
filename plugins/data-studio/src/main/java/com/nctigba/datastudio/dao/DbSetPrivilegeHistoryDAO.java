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
 *  DbSetPrivilegeHistoryDAO.java
 *
 *  IDENTIFICATION
 *  plugins/data-studio/src/main/java/com/nctigba/datastudio/dao/DbSetPrivilegeHistoryDAO.java
 *
 *  -------------------------------------------------------------------------
 */

package com.nctigba.datastudio.dao;

import com.nctigba.datastudio.model.dto.PrivilegeHistoryDTO;
import com.nctigba.datastudio.model.entity.PrivilegeHistoryDO;
import com.nctigba.datastudio.model.query.PrivilegeHistoryQuery;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCallback;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.stereotype.Component;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

import static java.lang.Math.ceil;

/**
 * DbPrivilegeDAO
 *
 * @author liupengfei
 * @since 2024/10/30
 */
@Component
@Slf4j
public class DbSetPrivilegeHistoryDAO implements ApplicationRunner {
    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        jdbcTemplate.execute(
                "create table if not exists privilegeHistory ("
                        + " id integer primary key AUTOINCREMENT,"
                        + " startTime varchar(20),"
                        + " success boolean,"
                        + " errMes varchar(512),"
                        + " sql clob,"
                        + " privilegeSetQuery clob"
                        + ");");
    }

    /**
     * insert privilege execute history
     *
     * @param privilegeHistoryDO privilegeHistoryDO
     */
    public void insertTable(PrivilegeHistoryDO privilegeHistoryDO) {
        String sql = "insert into privilegeHistory(startTime, success, errMes, sql, privilegeSetQuery) "
                + "values(?,?,?,?,?)";
        jdbcTemplate.execute(new PreparedStatementCreator() {
            @Override
            public PreparedStatement createPreparedStatement(Connection con) throws SQLException {
                return con.prepareStatement(sql);
            }
        }, new PreparedStatementCallback<Object>() {
            @Override
            public Object doInPreparedStatement(PreparedStatement ps) throws SQLException, DataAccessException {
                ps.setString(1, privilegeHistoryDO.getStartTime());
                ps.setBoolean(2, privilegeHistoryDO.isSuccess());
                ps.setString(3, privilegeHistoryDO.getErrMes());
                ps.setString(4, privilegeHistoryDO.getSql());
                ps.setString(5, privilegeHistoryDO.getPrivilegeSetQuery());
                return ps.execute();
            }
        });
        log.info("privilegeHistory insertTable: " + privilegeHistoryDO);
    }


    /**
     * query privilege history
     *
     * @param privilegeHistoryQuery privilegeHistoryQuery
     * @return PrivilegeHistoryDTO
     */
    public PrivilegeHistoryDTO queryTable(PrivilegeHistoryQuery privilegeHistoryQuery) {
        log.info("DbSetPrivilegeHistory queryTable privilegeHistory: " + privilegeHistoryQuery);
        String sqlSelect = "select id, startTime, success, errMes, sql, privilegeSetQuery "
                + "from privilegeHistory ";
        String sqlCount = "select count(*) from privilegeHistory ";
        if (StringUtils.isNotEmpty(privilegeHistoryQuery.getLike())) {
            String where = "where sql like '%" + privilegeHistoryQuery.getLike() + "%'";
            sqlSelect = sqlSelect + where;
            sqlCount = sqlCount + where;
        }
        sqlSelect = sqlSelect + "order by startTime desc "
                + "limit " + privilegeHistoryQuery.getPageSize()
                + " offset " + ((privilegeHistoryQuery.getPageNum() - 1) * privilegeHistoryQuery.getPageSize()) + ";";
        List<PrivilegeHistoryDO> list = jdbcTemplate.query(sqlSelect,
                new BeanPropertyRowMapper<>(PrivilegeHistoryDO.class));
        Long total = jdbcTemplate.queryForObject(sqlCount, Long.class);
        PrivilegeHistoryDTO dto = new PrivilegeHistoryDTO();
        dto.setData(list);
        dto.setPageSize(privilegeHistoryQuery.getPageSize());
        dto.setPageNum(privilegeHistoryQuery.getPageNum());
        dto.setPageTotal((int) ceil((double) total / privilegeHistoryQuery.getPageSize()));
        dto.setDataSize(total);
        return dto;
    }
}
