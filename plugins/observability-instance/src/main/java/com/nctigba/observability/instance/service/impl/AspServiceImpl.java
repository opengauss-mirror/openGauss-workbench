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
 *  AspServiceImpl.java
 *
 *  IDENTIFICATION
 *  plugins/observability-instance/src/main/java/com/nctigba/observability/instance/service/impl/AspServiceImpl.java
 *
 *  -------------------------------------------------------------------------
 */

package com.nctigba.observability.instance.service.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.nctigba.observability.instance.aspectj.annotation.Ds;
import com.nctigba.observability.instance.model.dto.asp.AnalysisDTO;
import com.nctigba.observability.instance.model.query.AspCountQuery;
import com.nctigba.observability.instance.mapper.AspMapper;
import com.nctigba.observability.instance.service.AspService;

/**
 * AspServiceImpl
 *
 * @author liupengfei
 * @since 2023/8/11
 */
@Service
public class AspServiceImpl implements AspService {
    @Autowired
    private AspMapper aspMapper;

    @Override
    @Ds("id")
    public Map<String, List<Object>> count(AspCountQuery req) {
        List<Map<String, Object>> searchRes = aspMapper.count(req);
        ArrayList<Object> time = new ArrayList<>();
        ArrayList<Object> count = new ArrayList<>();
        Map<String, List<Object>> res = new HashMap<>();
        for (Map<String, Object> map : searchRes) {
            time.add(map.get("sample_time"));
            count.add(map.get("session_count"));
        }
        res.put("sessionCount", count);
        res.put("sampleTime", time);
        return res;
    }

    @Override
    @Ds("id")
    public List<AnalysisDTO> analysis(AspCountQuery req) {
        return aspMapper.analysis(req);
    }
}
