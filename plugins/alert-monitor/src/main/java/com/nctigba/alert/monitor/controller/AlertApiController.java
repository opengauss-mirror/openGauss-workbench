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
 *  AlertApiController.java
 *
 *  IDENTIFICATION
 *  plugins/alert-monitor/src/main/java/com/nctigba/alert/monitor/controller/AlertApiController.java
 *
 *  -------------------------------------------------------------------------
 */

package com.nctigba.alert.monitor.controller;

import cn.hutool.json.JSONObject;
import com.nctigba.alert.monitor.constant.CommonConstants;
import com.nctigba.alert.monitor.service.AlertApiService;
import lombok.extern.slf4j.Slf4j;
import org.opengauss.admin.common.core.domain.AjaxResult;
import com.nctigba.alert.monitor.model.query.api.AlertApiReq;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * @author wuyuebin
 * @date 2023/4/28 08:44
 * @description
 */
@RestController
@RequestMapping("/api/v1")
@Slf4j
public class AlertApiController {
    @Autowired
    private AlertApiService alertApiService;

    /**
     * This is the interface for Prometheus to push data.
     * <p>
     * The fixed interface for Prometheus to push data is:/%pre/api/%v/alerts
     * <p>
     * In Prometheus configuration file, the path_prefix (%pre) for Prometheus is: /plugins/alert-monitor.
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
            LocalDateTime endAt = alertApiReq.getEndsAt().plusHours(8);
            if (endAt.isAfter(LocalDateTime.now())) {
                alertApiReq.setEndsAt(LocalDateTime.now());
                alertApiReq.setAlertStatus(CommonConstants.FIRING_STATUS);
            } else {
                alertApiReq.setEndsAt(endAt);
                alertApiReq.setAlertStatus(CommonConstants.RECOVER_STATUS);
            }
            alertApiReqList.add(alertApiReq);
        }
        alertApiService.alerts(alertApiReqList);
        return AjaxResult.success();
    }
}
