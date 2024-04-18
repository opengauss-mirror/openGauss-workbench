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
 *  SqlHistoryTemplateDAO.java
 *
 *  IDENTIFICATION
 *  plugins/data-studio/src/main/java/com/nctigba/datastudio/dao/SqlHistoryTemplateDAO.java
 *
 *  -------------------------------------------------------------------------
 */

package com.nctigba.datastudio.dao;

import com.nctigba.datastudio.model.entity.SqlHistoryDO;
import com.nctigba.datastudio.utils.LocaleStringUtils;
import lombok.extern.slf4j.Slf4j;
import org.opengauss.admin.common.exception.CustomException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.jdbc.UncategorizedSQLException;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * SqlHistoryTemplate
 *
 * @since 2023-6-26
 */
@Slf4j
@Repository
public class SqlHistoryTemplateDAO implements ApplicationRunner {
    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Override
    public void run(ApplicationArguments args) {
        jdbcTemplate.execute(
                "create table if not exists sqlHistory ("
                        + " id integer primary key AUTOINCREMENT,"
                        + " startTime varchar(20),"
                        + " success boolean,"
                        + " sql text,"
                        + " executeTime varchar(10),"
                        + " lock boolean,"
                        + " webUser varchar(20)"
                        + ");");
        String[] sqlList = {
                "alter table sqlHistory add column errMes varchar(512);",
                "alter table sqlHistory add column updateCount integer;"
        };
        for (String s : sqlList) {
            try {
                jdbcTemplate.execute(s);
            } catch (UncategorizedSQLException e) {
                continue;
            }
        }
    }

    /**
     * insert sql execute history
     *
     * @param sqlHistoryDOList sqlHistoryDOList
     */
    public void insertTable(List<SqlHistoryDO> sqlHistoryDOList) {
        String sql = "insert into sqlHistory(startTime, success, sql, executeTime, lock, webUser, errMes, updateCount) "
                + "values(?,?,?,?,?,?,?,?)";
        int[] counts = jdbcTemplate.batchUpdate(sql, new BatchPreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                ps.setString(1, sqlHistoryDOList.get(i).getStartTime());
                ps.setBoolean(2, sqlHistoryDOList.get(i).isSuccess());
                ps.setString(3, sqlHistoryDOList.get(i).getSql());
                ps.setString(4, sqlHistoryDOList.get(i).getExecuteTime());
                ps.setBoolean(5, sqlHistoryDOList.get(i).isLock());
                ps.setString(6, sqlHistoryDOList.get(i).getWebUser());
                ps.setString(7, sqlHistoryDOList.get(i).getErrMes());
                ps.setInt(8, sqlHistoryDOList.get(i).getUpdateCount());
            }

            @Override
            public int getBatchSize() {
                return sqlHistoryDOList.size();
            }
        });
        log.info("SqlHistoryTemplate insertTable counts: " + Arrays.toString(counts));
    }

    /**
     * insert sql history
     *
     * @param list list
     */
    public void insertSqlHistory(List<SqlHistoryDO> list) {
        String webUser = list.get(0).getWebUser();
        Integer lockCount = queryCount(webUser, true);
        Integer unlockCount = queryCount(webUser, false);
        log.info("AsyncHelper insertSqlHistory lockCount--unlockCount: " + lockCount + "--" + unlockCount);

        if (lockCount + list.size() > 200) {
            deleteUnlockList(webUser);
            Collections.reverse(list);
            List<SqlHistoryDO> limitList = list.stream().limit(200 - lockCount).collect(Collectors.toList());
            insertTable(limitList);
        } else if (lockCount + unlockCount + list.size() > 200) {
            List<Integer> idList = queryIdList(list.size() + lockCount + unlockCount - 200);
            deleteByIdList(idList);
            insertTable(list);
        } else {
            insertTable(list);
        }
        log.info("AsyncHelper insertSqlHistory end: ");
    }

    /**
     * query sql history
     *
     * @param sqlHistoryDO sqlHistoryDO
     * @return List
     */
    public List<SqlHistoryDO> queryTable(SqlHistoryDO sqlHistoryDO) {
        log.info("SqlHistoryTemplate queryTable sqlHistoryDO: " + sqlHistoryDO);
        List<SqlHistoryDO> list = jdbcTemplate.query(
                "select id, startTime, success, sql, executeTime, lock, webUser, errMes, updateCount "
                        + "from sqlHistory where webUser = '"
                        + sqlHistoryDO.getWebUser() + "' order by lock desc, startTime desc;",
                new BeanPropertyRowMapper<>(SqlHistoryDO.class));
        return list;
    }

    /**
     * query count
     *
     * @param webUser webUser
     * @param isLock  isLock
     * @return Integer
     */
    public Integer queryCount(String webUser, boolean isLock) {
        log.info("SqlHistoryTemplate queryCount webUser: " + webUser);
        Integer count = jdbcTemplate.queryForObject(
                "select count(lock) from sqlHistory where webUser = '" + webUser + "' and lock = " + isLock + ";",
                Integer.class);
        log.info("SqlHistoryTemplate queryCount count: " + count);
        return Objects.requireNonNullElse(count, 0);
    }

    /**
     * query id list
     *
     * @param count count
     * @return List
     */
    public List<Integer> queryIdList(int count) {
        log.info("SqlHistoryTemplate queryIdList count: " + count);
        List<Integer> idList = jdbcTemplate.queryForList(
                "select id from sqlHistory where lock = false order by startTime limit " + count + ";",
                Integer.class);
        log.info("SqlHistoryTemplate queryIdList idList: " + idList);
        return idList;
    }

    /**
     * update status by id
     *
     * @param sqlHistoryDO sqlHistoryDO
     */
    public void updateStatusById(SqlHistoryDO sqlHistoryDO) {
        boolean isLock = sqlHistoryDO.isLock();
        if (isLock) {
            Integer count = queryCount(sqlHistoryDO.getWebUser(), true);
            if (count == 50) {
                throw new CustomException(LocaleStringUtils.transLanguage("2019"));
            }
        }

        jdbcTemplate.execute(
                "update sqlHistory set lock = " + sqlHistoryDO.isLock()
                        + " where id = " + sqlHistoryDO.getId() + ";");
    }

    /**
     * delete sql history by id
     *
     * @param sqlHistoryDO sqlHistoryDO
     */
    public void deleteById(SqlHistoryDO sqlHistoryDO) {
        Integer id = sqlHistoryDO.getId();
        Boolean isLock = jdbcTemplate.queryForObject(
                "select lock from sqlHistory where id = " + id + ";", Boolean.class);
        log.info("SqlHistoryTemplate deleteTable isLock: " + isLock);
        if (Boolean.TRUE.equals(isLock)) {
            throw new CustomException(LocaleStringUtils.transLanguage("2021"));
        }
        jdbcTemplate.execute("delete from sqlHistory where id = " + sqlHistoryDO.getId() + ";");
    }

    /**
     * delete unlock sql history
     *
     * @param webUser webUser
     */
    public void deleteUnlockList(String webUser) {
        jdbcTemplate.execute("delete from sqlHistory where webUser = '" + webUser + "' and lock = false;");
    }

    /**
     * delete sql history by id list
     *
     * @param idList idList
     */
    public void deleteByIdList(List<Integer> idList) {
        log.info("SqlHistoryTemplate deleteByIdList idList: " + idList);
        String sql = "delete from sqlHistory where id = ?";
        int[] counts = jdbcTemplate.batchUpdate(sql, new BatchPreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                ps.setInt(1, idList.get(i));
            }

            @Override
            public int getBatchSize() {
                return idList.size();
            }
        });
        log.info("SqlHistoryTemplate insertTable counts: " + Arrays.toString(counts));
    }
}
