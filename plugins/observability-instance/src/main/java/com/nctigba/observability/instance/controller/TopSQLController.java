package com.nctigba.observability.instance.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.fastjson.JSONObject;
import com.nctigba.common.web.exception.CustomExceptionEnum;
import com.nctigba.common.web.result.AppResult;
import com.nctigba.observability.instance.dto.topsql.TopSQLInfoReq;
import com.nctigba.observability.instance.dto.topsql.TopSQLListReq;
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

    @GetMapping(value = "/connect/{nodeId}")
    public AppResult testConnection(@PathVariable("nodeId") String nodeId) {
        return AppResult.ok("success").addData(topSQLService.testConnection(nodeId));
    }

    @GetMapping(value = "/list")
    public AppResult top10(TopSQLListReq topSQLListReq) {
        List<JSONObject> list = topSQLService.getTopSQLList(topSQLListReq);
        System.out.println(list);
        if (list == null) {
            return AppResult.error(CustomExceptionEnum.TOLSQL_IS_RIGHT_PARAM.getMsg(), "top sql pre check fail");
        }
        return AppResult.ok("success").addData(list);
    }

    @GetMapping(value = "/detail")
    public AppResult detail(TopSQLInfoReq topSQLDetailReq) {
        return AppResult.ok("success").addData(topSQLService.getStatisticalInfo(topSQLDetailReq));
    }

    @GetMapping(value = "/plan")
    public AppResult plan(TopSQLInfoReq topSQLPlanReq) {
        JSONObject res = topSQLService.getExecutionPlan(topSQLPlanReq, "");
        if (res == null) {
            return AppResult.error(CustomExceptionEnum.TOLSQL_IS_RIGHT_PARAM.getMsg(), "execution plan pre check fail");
        }
        return AppResult.ok("success").addData(res);
    }

    @GetMapping(value = "/partition")
    public AppResult partition(TopSQLInfoReq topSQLPartitionReq) {
        return AppResult.ok("success").addData(topSQLService.getPartitionList(topSQLPartitionReq));
    }

    @GetMapping(value = "/index")
    public AppResult index(TopSQLInfoReq topSQLIndexReq) {
        return AppResult.ok("success").addData(topSQLService.getIndexAdvice(topSQLIndexReq));
    }

    @GetMapping(value = "/object")
    public AppResult object(TopSQLInfoReq topSQLObjectReq) {
        return AppResult.ok("success").addData(topSQLService.getObjectInfo(topSQLObjectReq));
    }

    @GetMapping(value = "/cluster")
    public AppResult cluster() {
        return AppResult.ok("success").addData(topSQLService.cluster());
    }

    @GetMapping(value = "/cluster/{id}")
    public AppResult clusterNode(@PathVariable("id") String id) {
        return AppResult.ok("success").addData(topSQLService.clusterNode(id));
    }
}
