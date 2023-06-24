/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */
package com.nctigba.observability.instance.controller;

import java.util.List;

import org.opengauss.admin.common.core.domain.AjaxResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.fastjson.JSONObject;
import com.nctigba.common.web.exception.CustomExceptionEnum;
import com.nctigba.observability.instance.dto.topsql.TopSQLInfoReq;
import com.nctigba.observability.instance.dto.topsql.TopSQLListReq;
import com.nctigba.observability.instance.service.ClusterManager;
import com.nctigba.observability.instance.service.TopSQLService;

import lombok.RequiredArgsConstructor;

/**
 * <p>
 * TopSQL Controller
 * </p>
 *
 * @author zhanggr@vastdata.com.cn
 * @since 2022/9/15 17:05
 */
@RestController
@RequestMapping("/observability/v1/topsql")
@RequiredArgsConstructor
public class TopSQLController {
    private final TopSQLService topSQLService;
    private final ClusterManager clusterManager;

    @GetMapping(value = "/connect/{nodeId}")
    public AjaxResult testConnection(@PathVariable("nodeId") String nodeId) {
        return AjaxResult.success(topSQLService.testConnection(nodeId));
    }

    @GetMapping(value = "/list")
    public AjaxResult top10(TopSQLListReq topSQLListReq) {
        List<JSONObject> list = topSQLService.getTopSQLList(topSQLListReq);
        if (list == null) {
            return AjaxResult.error(CustomExceptionEnum.TOLSQL_IS_RIGHT_PARAM.getMsg(), "top sql pre check fail");
        }
        return AjaxResult.success(list);
    }

    @GetMapping(value = "/detail")
    public AjaxResult detail(TopSQLInfoReq topSQLDetailReq) {
        return AjaxResult.success(topSQLService.getStatisticalInfo(topSQLDetailReq));
    }

    @GetMapping(value = "/plan")
    public AjaxResult plan(TopSQLInfoReq topSQLPlanReq) {
        JSONObject res = topSQLService.getExecutionPlan(topSQLPlanReq, "");
        if (res == null) {
            return AjaxResult.error(CustomExceptionEnum.TOLSQL_IS_RIGHT_PARAM.getMsg(),
                    "execution plan pre check fail");
        }
        return AjaxResult.success(res);
    }

    @GetMapping(value = "/partition")
    public AjaxResult partition(TopSQLInfoReq topSQLPartitionReq) {
        return AjaxResult.success(topSQLService.getPartitionList(topSQLPartitionReq));
    }

    @GetMapping(value = "/index")
    public AjaxResult index(TopSQLInfoReq topSQLIndexReq) {
        return AjaxResult.success(topSQLService.getIndexAdvice(topSQLIndexReq));
    }

    @GetMapping(value = "/object")
    public AjaxResult object(TopSQLInfoReq topSQLObjectReq) {
        return AjaxResult.success(topSQLService.getObjectInfo(topSQLObjectReq));
    }

    @GetMapping(value = "/cluster")
    public AjaxResult cluster() {
        return AjaxResult.success(clusterManager.getAllOpsCluster());
    }

    @GetMapping(value = "/cluster/{id}")
    public AjaxResult clusterNode(@PathVariable("id") String id) {
        return AjaxResult.success(clusterManager.getOpsNodeById(id));
    }

    @GetMapping(value = "/waitevent")
    public AjaxResult waitevent(String id, String sqlId) {
        return AjaxResult.success(topSQLService.waitEvent(id, sqlId));
    }
}
