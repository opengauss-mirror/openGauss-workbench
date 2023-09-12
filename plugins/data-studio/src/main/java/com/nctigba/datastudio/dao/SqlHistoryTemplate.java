/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package com.nctigba.datastudio.dao;

import com.nctigba.datastudio.model.entity.SqlHistoryDO;
import com.nctigba.datastudio.util.LocaleString;
import lombok.extern.slf4j.Slf4j;
import org.opengauss.admin.common.exception.CustomException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
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
public class SqlHistoryTemplate implements ApplicationRunner {
    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Override
    public void run(ApplicationArguments args) {
        jdbcTemplate.execute(
                "create table if not exists sqlHistory ("
                        + " id integer primary key,"
                        + " startTime varchar(20),"
                        + " success boolean,"
                        + " sql text,"
                        + " executeTime varchar(10),"
                        + " lock boolean,"
                        + " webUser varchar(20)"
                        + ");");
    }

    /**
     * insert sql execute history
     *
     * @param sqlHistoryDOList sqlHistoryDOList
     */
    public void insertTable(List<SqlHistoryDO> sqlHistoryDOList) {
        log.info("SqlHistoryTemplate insertTable sqlHistoryDOList: " + sqlHistoryDOList);
        String sql = "insert into sqlHistory(startTime, success, sql, executeTime, lock, webUser) values(?,?,?,?,?,?)";
        int[] counts = jdbcTemplate.batchUpdate(sql, new BatchPreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                ps.setString(1, sqlHistoryDOList.get(i).getStartTime());
                ps.setBoolean(2, sqlHistoryDOList.get(i).isSuccess());
                ps.setString(3, sqlHistoryDOList.get(i).getSql());
                ps.setString(4, sqlHistoryDOList.get(i).getExecuteTime());
                ps.setBoolean(5, sqlHistoryDOList.get(i).isLock());
                ps.setString(6, sqlHistoryDOList.get(i).getWebUser());
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
        log.info("AsyncHelper insertSqlHistory list: " + list);
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
        List<SqlHistoryDO> lockList = new ArrayList<>();
        List<SqlHistoryDO> unLockList = new ArrayList<>();
        List<SqlHistoryDO> sqlHistoryDOList = jdbcTemplate.query(
                "select id, startTime, success, sql, executeTime, lock, webUser from sqlHistory where webUser = '"
                        + sqlHistoryDO.getWebUser() + "' order by startTime desc;",
                new BeanPropertyRowMapper<>(SqlHistoryDO.class));
        sqlHistoryDOList.forEach(item -> {
            if (item.isLock()) {
                lockList.add(item);
            } else {
                unLockList.add(item);
            }
        });

        List<SqlHistoryDO> list = new ArrayList<>();
        list.addAll(lockList);
        list.addAll(unLockList);
        log.info("SqlHistoryTemplate queryTable list: " + list);
        return list;
    }

    /**
     * query count
     *
     * @param webUser webUser
     * @param isLock isLock
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
        log.info("SqlHistoryTemplate updateTable sqlHistoryDO: " + sqlHistoryDO);
        boolean isLock = sqlHistoryDO.isLock();
        if (isLock) {
            Integer count = queryCount(sqlHistoryDO.getWebUser(), true);
            if (count == 50) {
                throw new CustomException(LocaleString.transLanguage("2019"));
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
        log.info("SqlHistoryTemplate deleteTable sqlHistoryDO: " + sqlHistoryDO);
        Integer id = sqlHistoryDO.getId();
        Boolean isLock = jdbcTemplate.queryForObject(
                "select lock from sqlHistory where id = " + id + ";", Boolean.class);
        log.info("SqlHistoryTemplate deleteTable isLock: " + isLock);
        if (Boolean.TRUE.equals(isLock)) {
            throw new CustomException(LocaleString.transLanguage("2021"));
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
