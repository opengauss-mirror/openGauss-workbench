/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */
package com.nctigba.observability.instance.service;

import java.util.List;
import java.util.Map;

import com.alibaba.fastjson.JSONObject;
import com.nctigba.observability.instance.dto.session.DetailStatisticDto;

public interface SessionService {
    JSONObject detailGeneral(String id, String sessionid);

    List<DetailStatisticDto> detailStatistic(String id, String sessionid);

    List<JSONObject> detailWaiting(String id, String sessionid);

    List<JSONObject> detailBlockTree(String id, String sessionid);

    JSONObject simpleStatistic(String id);

    List<JSONObject> longTxc(String id);

    Map<String, List<JSONObject>> blockAndLongTxc(String id);

    Map<String, Object> detail(String id, String sessionid);
}
