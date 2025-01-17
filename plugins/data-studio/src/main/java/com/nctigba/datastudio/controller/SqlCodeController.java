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
 *  SqlCodeController.java
 *
 *  IDENTIFICATION
 *  plugins/data-studio/src/main/java/com/nctigba/datastudio/controller/SqlCodeController.java
 *
 *  -------------------------------------------------------------------------
 */

package com.nctigba.datastudio.controller;

import com.nctigba.datastudio.dao.SqlCodeDAO;
import com.nctigba.datastudio.model.dto.SqlCodeDTO;
import com.nctigba.datastudio.model.entity.SqlCodeDo;
import com.nctigba.datastudio.model.query.SqlCodeQuery;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 功能描述
 *
 * @author liupengfei
 * @since 2024/11/25
 */
@RestController
@RequestMapping(value = "/dataStudio/web/v1/sqlCode")
public class SqlCodeController {
    @Autowired
    private SqlCodeDAO sqlCodeDAO;

    /**
     * createSqlCode
     *
     * @param sqlCodeDo SqlCodeDo
     */
    @PostMapping("/create")
    public void createSqlCode(@RequestBody SqlCodeDo sqlCodeDo) {
        sqlCodeDAO.insert(sqlCodeDo);
    }

    /**
     * list
     *
     * @param query SqlCodeQuery
     * @return SqlCodeDTO
     */
    @GetMapping("/list")
    public SqlCodeDTO list(SqlCodeQuery query) {
        return sqlCodeDAO.list(query);
    }

    /**
     * list
     *
     * @param id Integer
     * @return SqlCodeDo
     */
    @GetMapping("/get")
    public SqlCodeDo list(Integer id) {
        return sqlCodeDAO.listById(id);
    }

    /**
     * update
     *
     * @param sqlCodeDo SqlCodeDo
     */
    @PostMapping("/update")
    public void update(@RequestBody SqlCodeDo sqlCodeDo) {
        sqlCodeDAO.update(sqlCodeDo);
    }

    /**
     * delete
     *
     * @param id Integer
     */
    @DeleteMapping("/delete")
    public void delete(Integer id) {
        sqlCodeDAO.delete(id);
    }
}
