package com.nctigba.datastudio.service;

import com.nctigba.datastudio.model.query.SelectDataQuery;

import java.util.Map;

public interface TableDataService {
    Map<String, Object> tableData(SelectDataQuery request) throws Exception;

    Map<String, Object> tableColumn(SelectDataQuery request) throws Exception;

    Map<String, Object> tableIndex(SelectDataQuery request) throws Exception;

    Map<String, Object> tableConstraint(SelectDataQuery request) throws Exception;

    Map<String, String> tableDdl(SelectDataQuery request) throws Exception;
}
