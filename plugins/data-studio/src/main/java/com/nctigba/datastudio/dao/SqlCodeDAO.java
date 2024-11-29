/*
 *  Copyright (c) GBA-NCTI-ISDC. 2024-2024.
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
 *  SqlCodeDAO.java
 *
 *  IDENTIFICATION
 *  plugins/data-studio/src/main/java/com/nctigba/datastudio/dao/SqlCodeDAO.java
 *
 *  -------------------------------------------------------------------------
 */

package com.nctigba.datastudio.dao;

import com.nctigba.datastudio.model.dto.SqlCodeDTO;
import com.nctigba.datastudio.model.entity.SqlCodeDo;
import com.nctigba.datastudio.model.query.SqlCodeQuery;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;

import static java.lang.Math.ceil;

/**
 * SqlCodeDAO
 *
 * @author liupengfei
 * @since 2024/11/25
 */
@Slf4j
@Repository
public class SqlCodeDAO implements ApplicationRunner {
    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        jdbcTemplate.execute(
                "create table if not exists sqlCode ("
                        + " id integer primary key AUTOINCREMENT,"
                        + " name varchar(128) not null,"
                        + " code clob not null,"
                        + " description varchar(512)"
                        + ");");
    }

    /**
     * insert
     *
     * @param sqlCodeDo SqlCodeDo
     */
    public void insert(SqlCodeDo sqlCodeDo) {
        String sql = "insert into sqlCode(name,code,description) values('" + sqlCodeDo.getName()
                + "','" + sqlCodeDo.getCode() + "','" + sqlCodeDo.getDescription() + "');";
        jdbcTemplate.execute(sql);
    }

    /**
     * list
     *
     * @param query SqlCodeQuery
     * @return SqlCodeDTO
     */
    public SqlCodeDTO list(SqlCodeQuery query) {
        String sql = "select * from sqlCode";
        sql = sql + where(query) + " limit " + query.getPageSize() + " offset "
                + ((query.getPageNum() - 1) * query.getPageSize()) + ";";
        List<SqlCodeDo> sqlCodeDoList = jdbcTemplate.query(sql, new BeanPropertyRowMapper<>(SqlCodeDo.class));
        String countSql = "select count(*) from sqlCode";
        Long total = jdbcTemplate.queryForObject(countSql + where(query), Long.class);
        SqlCodeDTO dto = new SqlCodeDTO();
        dto.setData(sqlCodeDoList);
        dto.setPageSize(query.getPageSize());
        dto.setPageNum(query.getPageNum());
        dto.setPageTotal((int) ceil((double) total / query.getPageSize()));
        dto.setDataSize(total);
        return dto;
    }

    /**
     * listById
     *
     * @param id Integer
     * @return SqlCodeDo
     */
    public SqlCodeDo listById(Integer id) {
        String sql = "select * from sqlCode where id = " + id;
        return jdbcTemplate.queryForObject(sql, new BeanPropertyRowMapper<>(SqlCodeDo.class));
    }

    private static String where(SqlCodeQuery query) {
        String where;
        if (StringUtils.isNotEmpty(query.getName()) && StringUtils.isEmpty(query.getCode())) {
            where = " where name like '%" + query.getName() + "%'";
        } else if (StringUtils.isEmpty(query.getName()) && StringUtils.isNotEmpty(query.getCode())) {
            where = " where code like '%" + query.getCode() + "%'";
        } else if (StringUtils.isNotEmpty(query.getName()) && StringUtils.isNotEmpty(query.getCode())) {
            where = " where name like '%" + query.getName() + "%' and code like '%" + query.getCode() + "%'";
        } else if (query.getId() != null) {
            where = " where id = " + query.getId();
        } else {
            where = "";
        }
        return where;
    }

    /**
     * update
     *
     * @param sqlCodeDo SqlCodeDo
     */
    public void update(SqlCodeDo sqlCodeDo) {
        String sql = "update sqlCode set name = ?, code = ?, description = ?  where id = ?";
        ArrayList<Object> objects = new ArrayList<>();
        objects.add(sqlCodeDo.getName());
        objects.add(sqlCodeDo.getCode());
        objects.add(sqlCodeDo.getDescription());
        objects.add(sqlCodeDo.getId());
        jdbcTemplate.update(sql, objects.toArray());
    }

    /**
     * delete
     *
     * @param id Integer
     */
    public void delete(Integer id) {
        String sql = "delete from sqlCode where id = ?";
        jdbcTemplate.update(sql, id);
    }
}
