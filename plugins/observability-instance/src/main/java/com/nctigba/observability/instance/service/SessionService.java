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
 *  SessionService.java
 *
 *  IDENTIFICATION
 *  plugins/observability-instance/src/main/java/com/nctigba/observability/instance/service/SessionService.java
 *
 *  -------------------------------------------------------------------------
 */

package com.nctigba.observability.instance.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.nctigba.observability.instance.model.vo.BlockTreeVO;
import com.nctigba.observability.instance.model.vo.PgStatActivityVO;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;
import com.nctigba.observability.instance.aspectj.annotation.Ds;
import com.nctigba.observability.instance.model.dto.session.DetailStatisticDTO;
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
    public List<DetailStatisticDTO> detailStatistic(String id, String sessionid) {
        ArrayList<DetailStatisticDTO> list = new ArrayList<>();
        list.addAll(
                sessionMapper.statistic(sessionid).stream()
                        .map(ob -> new DetailStatisticDTO(DetailStatisticDTO.Type.STATUS,
                                ob.get("stat_name").toString(), ob.get("value").toString()))
                        .collect(Collectors.toList()));
        list.addAll(
                sessionMapper.runtime(sessionid).stream()
                        .map(ob -> new DetailStatisticDTO(DetailStatisticDTO.Type.RUNTIME,
                                ob.get("statname").toString(), ob.get("value").toString()))
                        .collect(Collectors.toList()));
        return list;
    }

    @Ds
    public List<Map<String, Object>> detailWaiting(String id, String sessionid) {
        return sessionMapper.detailWaiting(sessionid);
    }

    @Ds
    public List<BlockTreeVO> detailBlockTree(String id, String sessionid) {
        List<BlockTreeVO> queryList = sessionMapper.blockTree();
        if (StringUtils.isNotEmpty(sessionid)) {
            Set<String> treeIdSet = queryList.stream().filter(obj -> obj.getPathid().contains(sessionid))
                    .map(obj -> obj.getTreeId()).collect(Collectors.toSet());
            queryList = queryList.stream().filter(obj -> treeIdSet.contains(obj.getTreeId()))
                    .collect(Collectors.toList());
        }
        return toTreeData(queryList);
    }

    @Ds
    public Map<String, Object> simpleStatistic(String id) {
        return sessionMapper.simpleStatistic();
    }

    @Ds
    public List<PgStatActivityVO> longTxc(String id) {
        return sessionMapper.longTxc();
    }

    @Ds
    public HashMap<String, Object> blockAndLongTxc(String id) {
        HashMap<String, Object> res = new HashMap<>();
        res.put("blockTree", detailBlockTree(id, null));
        res.put("longTxc", longTxc(id));
        res.put("longTxcTotal", sessionMapper.longTxcTotal());
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
    private List<BlockTreeVO> toTreeData(List<BlockTreeVO> list) {
        // Classify by parentid
        Map<Long, BlockTreeVO> map = new HashMap<>();
        List<BlockTreeVO> result = new ArrayList<>();
        for (BlockTreeVO blockTree : list) {
            blockTree.setChildren(new ArrayList<>());
            map.put(blockTree.getId(), blockTree);
            Long parentId = blockTree.getParentid();
            if (parentId == 0) {
                result.add(blockTree);
                continue;
            }
            map.get(parentId).getChildren().add(blockTree);
        }
        return result;
    }
}