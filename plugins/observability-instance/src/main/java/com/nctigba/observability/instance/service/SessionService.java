/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package com.nctigba.observability.instance.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;
import com.nctigba.observability.instance.aop.Ds;
import com.nctigba.observability.instance.dto.session.DetailStatisticDto;
import com.nctigba.observability.instance.exception.InstanceException;
import com.nctigba.observability.instance.mapper.SessionMapper;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SessionService {
    private final SessionMapper sessionMapper;

    @Ds
    public Map<String, Object> detailGeneral(String id, String sessionid) {
        Map<String, Object> res = new JSONObject();
        List<Map<String, Object>> generalMesList = sessionMapper.generalMesList(sessionid);
        if (generalMesList.size() == 0) {
            throw new InstanceException("session.detail.general.message");
        }
        res.putAll(generalMesList.get(0));
        if (sessionMapper.sessionIsWaiting(sessionid) == 0) {
            return res;
        }
        List<Map<String, Object>> blockList = sessionMapper.blockList(sessionid);
        if (blockList.size() != 0) {
            res.putAll(blockList.get(0));
        }
        return res;
    }

    @Ds
    public List<DetailStatisticDto> detailStatistic(String id, String sessionid) {
        ArrayList<DetailStatisticDto> list = new ArrayList<>();
        list.addAll(
                sessionMapper.statistic(sessionid).stream()
                        .map(ob -> new DetailStatisticDto(DetailStatisticDto.Type.STATUS,
                                ob.get("stat_name").toString(), ob.get("value").toString()))
                        .collect(Collectors.toList()));
        list.addAll(
                sessionMapper.runtime(sessionid).stream()
                        .map(ob -> new DetailStatisticDto(DetailStatisticDto.Type.RUNTIME,
                                ob.get("statname").toString(), ob.get("value").toString()))
                        .collect(Collectors.toList()));
        return list;
    }

    @Ds
    public List<Map<String, Object>> detailWaiting(String id, String sessionid) {
        return sessionMapper.detailWaiting(sessionid);
    }

    @Ds
    public List<Map<String, Object>> detailBlockTree(String id, String sessionid) {
        List<Map<String, Object>> queryList = sessionMapper.blockTree();
        if (StringUtils.isNotEmpty(sessionid)) {
            Set<String> treeIdSet = queryList.stream().filter(obj -> obj.get("pathid").toString().contains(sessionid))
                    .map(obj -> obj.get("tree_id").toString()).collect(Collectors.toSet());
            queryList = queryList.stream().filter(obj -> treeIdSet.contains(obj.get("tree_id").toString()))
                    .collect(Collectors.toList());
        }
        return toTreeData(queryList);
    }

    @Ds
    public Map<String, Object> simpleStatistic(String id) {
        return sessionMapper.simpleStatistic();
    }

    @Ds
    public List<Map<String, Object>> longTxc(String id) {
        return sessionMapper.longTxc();
    }

    @Ds
    public HashMap<String, List<Map<String, Object>>> blockAndLongTxc(String id) {
        HashMap<String, List<Map<String, Object>>> res = new HashMap<>();
        res.put("blockTree", detailBlockTree(id, null));
        res.put("longTxc", longTxc(id));
        return res;
    }

    @Ds
    public Map<String, Object> detail(String id, String sessionid) {
        Map<String, Object> resMap = new HashMap<>();
        resMap.put("general", detailGeneral(id, sessionid));
        resMap.put("statistic", detailStatistic(id, sessionid));
        resMap.put("blockTree", detailBlockTree(id, sessionid));
        resMap.put("waiting", detailWaiting(id, sessionid));
        return resMap;
    }

    @SuppressWarnings({
            "unchecked",
            "rawtypes"
    })
    private List<Map<String, Object>> toTreeData(List<Map<String, Object>> list) {
        // Classify by parentid
        Map<Long, Map<String, Object>> map = new HashMap<>();
        List<Map<String, Object>> result = new ArrayList<>();
        for (Map<String, Object> object : list) {
            object.put("children", new ArrayList<>());
            map.put(Long.valueOf(object.get("id").toString()), object);
            Long parentId = Long.valueOf(object.get("parentid").toString());
            if (parentId == 0) {
                result.add(object);
                continue;
            }
            Object children = map.get(parentId).get("children");
            if (children instanceof List) {
                ((List) children).add(object);
            }
        }
        return result;
    }
}