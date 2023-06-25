/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package com.nctigba.observability.sql.model.history.data;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import lombok.Data;

/**
 * ElasticSearchData
 *
 * @author luomeng
 * @since 2023/6/9
 */
@Data
public class ElasticSearchData {
    private JSONObject queryName;
    private JSONArray value;
    private JSONArray values;
}