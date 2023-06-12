/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package com.nctigba.datastudio.model.query;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

@NoArgsConstructor
@Data
@Slf4j
public class TableDataEditQuery {
    private String winId;
    private List<TableDataDTO> data;

    @Data
    public static class TableDataDTO {
        private Map<String, Object> columnData;
        private Integer rowNum;
        private type type;

        public enum type {
            Insert {
                public void rs(ResultSet resultSet, TableDataDTO requst) throws SQLException {
                    log.info("TableDataDTO request is: {}" + requst);
                    resultSet.moveToInsertRow();
                    Iterator iterator = requst.columnData.entrySet().iterator();
                    while (iterator.hasNext()) {
                        Map.Entry map = (Map.Entry) iterator.next();
                        resultSet.updateObject((String) map.getKey(), map.getValue());
                    }
                    resultSet.insertRow();
                }
            }, Delete {
                public void rs(ResultSet resultSet, TableDataDTO requst) throws SQLException {
                    log.info("TableDataDTO request is: {}" + requst);
                    resultSet.absolute(requst.getRowNum());
                    resultSet.deleteRow();
                }
            }, Update {
                public void rs(ResultSet resultSet, TableDataDTO requst) throws SQLException {
                    log.info("TableDataDTO request is: {}" + requst);
                    resultSet.absolute(requst.getRowNum());
                    Iterator iterator = requst.columnData.entrySet().iterator();
                    while (iterator.hasNext()) {
                        Map.Entry map = (Map.Entry) iterator.next();
                        resultSet.updateObject((String) map.getKey(), map.getValue());
                    }
                    resultSet.updateRow();
                }
            };

            public abstract void rs(ResultSet resultSet, TableDataDTO requst) throws SQLException;
        }
    }
}
