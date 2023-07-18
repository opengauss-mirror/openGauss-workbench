/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package com.nctigba.datastudio.model.query;

import lombok.Data;
import lombok.Generated;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

/**
 * TableDataEditQuery
 *
 * @since 2023-6-26
 */
@NoArgsConstructor
@Data
@Generated
@Slf4j
public class TableDataEditQuery {
    private String winId;
    private List<TableDataDTO> data;

    @Data
@Generated
    public static class TableDataDTO {
        private Map<String, Object> columnData;
        private Integer rowNum;
        private type type;

        public enum type {
            INSERT {
                public void rs(ResultSet resultSet, TableDataDTO request) throws SQLException {
                    log.info("TableDataDTO request is: " + request);
                    resultSet.moveToInsertRow();
                    for (Map.Entry<String, Object> map : request.columnData.entrySet()) {
                        resultSet.updateObject(map.getKey(), map.getValue());
                    }
                    resultSet.insertRow();
                }
            }, DELETE {
                public void rs(ResultSet resultSet, TableDataDTO request) throws SQLException {
                    log.info("TableDataDTO request is: " + request);
                    resultSet.absolute(request.getRowNum());
                    resultSet.deleteRow();
                }
            }, UPDATE {
                public void rs(ResultSet resultSet, TableDataDTO request) throws SQLException {
                    log.info("TableDataDTO request is: " + request);
                    resultSet.absolute(request.getRowNum());
                    for (Map.Entry<String, Object> map : request.columnData.entrySet()) {
                        resultSet.updateObject(map.getKey(), map.getValue());
                    }
                    resultSet.updateRow();
                }
            };

            public abstract void rs(ResultSet resultSet, TableDataDTO request) throws SQLException;
        }
    }
}
