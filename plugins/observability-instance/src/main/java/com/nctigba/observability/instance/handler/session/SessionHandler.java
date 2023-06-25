/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */
package com.nctigba.observability.instance.handler.session;

import com.alibaba.fastjson.JSONObject;
import com.nctigba.observability.instance.dto.session.DetailStatisticDto;
import com.nctigba.observability.instance.model.InstanceNodeInfo;

import java.io.Closeable;
import java.sql.Connection;
import java.util.List;

public interface SessionHandler {
    /**
     * target database type
     * @return database type
     */
    String getDatabaseType();

    Connection getConnection(InstanceNodeInfo nodeInfo);

    void close(Connection connection);

    JSONObject detailGeneral(Connection conn, String sessionid);

    List<DetailStatisticDto> detailStatistic(Connection conn, String sessionid);

    List<JSONObject> detailWaiting(Connection conn, String sessionid);

    List<JSONObject> detailBlockTree(Connection conn, String sessionid);

    JSONObject simpleStatistic(Connection conn);

    List<JSONObject> longTxc(Connection conn);
}
