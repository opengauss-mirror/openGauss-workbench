/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package com.nctigba.observability.sql.util;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.nctigba.observability.sql.model.history.data.DatabaseData;
import com.nctigba.observability.sql.service.ClusterManager;
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
public class DbUtil {
    @Autowired
    private ClusterManager clusterManager;

    public Object rangQuery(String item, Date startTime, Date endTime, String nodeId) {
        if (clusterManager.getOpsClusterIdByNodeId(nodeId) == null) {
            log.info("nodeId is not exist!");
            return "error:nodeId is not exist!";
        }
        SimpleDateFormat stringFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String sql = item.replace("taskStartTime", stringFormat.format(startTime)).replace("hisDataEndTime",
                stringFormat.format(endTime));
        List<DatabaseData> list = new ArrayList<>();
        try (var conn = clusterManager.getConnectionByNodeId(
                nodeId); var stmt = conn.createStatement(); var rs = stmt.executeQuery(sql)) {
            DatabaseData data = new DatabaseData();
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
            data.setTableName(item.substring(item.indexOf("from") + 4, item.indexOf("where")).trim());
            list.add(data);
        } catch (SQLException e) {
            log.info(e.getMessage());
            return "error:" + e.getMessage();
        }
        return list;
    }
}
