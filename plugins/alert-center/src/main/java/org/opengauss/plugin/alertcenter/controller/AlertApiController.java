/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package org.opengauss.plugin.alertcenter.controller;

import cn.hutool.json.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.opengauss.admin.common.core.domain.AjaxResult;
import org.opengauss.plugin.alertcenter.model.api.AlertApiReq;
import org.opengauss.plugin.alertcenter.service.AlertApiService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.List;

/**
 * @author wuyuebin
 * @date 2023/4/28 08:44
 * @description
 */
@RestController
@RequestMapping("/alertCenter/api/v1")
@Slf4j
public class AlertApiController {
    @Autowired
    private AlertApiService alertApiService;

    /**
     * This is the interface for Prometheus to push data.
     * <p>
     * The fixed interface for Prometheus to push data is:/%pre/api/%v/alerts
     * <p>
     * In Prometheus configuration file, the path_prefix (%pre) for Prometheus is: /plugins/alert-center/alertCenter.
     * <p>
     * In the Prometheus configuration file, the api_version (%v) is: v1.
     * <p>
     * Note：The interface address must match the configuration of Prometheus.
     *
     * @param paramList alert info
     * @return return success info
     */
    @PostMapping("/alerts")
    public AjaxResult alerts(@RequestBody @Valid List<JSONObject> paramList) {
        log.info("alert info params is:", paramList);
        List<AlertApiReq> alertApiReqList = new ArrayList<>();
        for (JSONObject jsonObject : paramList) {
            jsonObject.put("startsAt", jsonObject.getStr("startsAt").replace("T", " ").substring(0, 19));
            jsonObject.put("endsAt", jsonObject.getStr("endsAt").replace("T", " ").substring(0, 19));
            AlertApiReq alertApiReq = jsonObject.toBean(AlertApiReq.class);
            alertApiReq.setStartsAt(alertApiReq.getStartsAt().plusHours(8));
            alertApiReq.setEndsAt(alertApiReq.getEndsAt().plusHours(8));
            alertApiReqList.add(alertApiReq);
        }
        alertApiService.alerts(alertApiReqList);
        return AjaxResult.success();
    }
}
