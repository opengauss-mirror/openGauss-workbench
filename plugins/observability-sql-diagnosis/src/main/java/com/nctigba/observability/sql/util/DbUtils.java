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
 *  DbUtils.java
 *
 *  IDENTIFICATION
 *  plugins/observability-sql-diagnosis/src/main/java/com/nctigba/observability/sql/util/DbUtils.java
 *
 *  -------------------------------------------------------------------------
 */

package com.nctigba.observability.sql.util;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.nctigba.observability.sql.model.vo.collection.DatabaseVO;
import com.nctigba.observability.sql.service.impl.ClusterManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * DbUtil
 *
 * @author luomeng
 * @since 2023/6/9
 */
@Component
@Slf4j
public class DbUtils {
    @Autowired
    private ClusterManager clusterManager;

    public Object rangQuery(String item, Date startTime, Date endTime, String nodeId) {
        if (clusterManager.getOpsClusterIdByNodeId(nodeId) == null) {
            log.info("nodeId is not exist!");
            return "error:nodeId is not exist!";
        }
        String sql;
        if (startTime != null && endTime != null) {
            SimpleDateFormat stringFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            sql = item.replace("hisDataStartTime", stringFormat.format(startTime)).replace(
                    "hisDataEndTime",
                    stringFormat.format(endTime));
        } else {
            sql = item;
        }
        List<DatabaseVO> list = new ArrayList<>();
        try (var conn = clusterManager.getConnectionByNodeId(
                nodeId); var stmt = conn.createStatement(); var rs = stmt.executeQuery(sql)) {
            DatabaseVO data = new DatabaseVO();
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("__name__", item);
            data.setSqlName(jsonObject);
            ResultSetMetaData metaData = rs.getMetaData();
            int columnCount = metaData.getColumnCount();
            List<Map<String, Object>> dataList = new ArrayList<>();
            while (rs.next()) {
                Map<String, Object> columnMap = new HashMap<>();
                for (int i = 1; i <= columnCount; i++) {
                    String key = metaData.getColumnName(i);
                    Object value = rs.getObject(key);
                    columnMap.put(key, value);
                }
                dataList.add(columnMap);
            }
            JSONArray jsonArray = new JSONArray();
            jsonArray.add(dataList);
            data.setValue(jsonArray);
            if (item.contains("where")) {
                data.setTableName(item.substring(item.indexOf("from") + 4, item.indexOf("where")).trim());
            }
            list.add(data);
        } catch (SQLException e) {
            log.info(e.getMessage());
            return "collection error:" + e.getMessage();
        }
        return list;
    }
}
