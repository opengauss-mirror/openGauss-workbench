/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package com.nctigba.observability.instance.service.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.nctigba.observability.instance.aop.Ds;
import com.nctigba.observability.instance.dto.asp.AnalysisDto;
import com.nctigba.observability.instance.dto.asp.AspCountReq;
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
    public Map<String, List<Object>> count(AspCountReq req) {
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
    public List<AnalysisDto> analysis(AspCountReq req) {
        return aspMapper.analysis(req);
    }
}
