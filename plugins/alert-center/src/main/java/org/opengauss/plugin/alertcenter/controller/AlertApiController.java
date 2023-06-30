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
     * 这是prometheus推送数据的接口
     * prometheus固定推送的接口是：/%pre/api/%v/alerts
     * prometheus配置文件中的path_prefix(%pre)是: /alertCenter
     * prometheus配置文件中的api_version(%v)是: v1
     * <p>
     * appcation.yml中配置的context-path是/alertCenter/api/v1
     * <p>
     * 注：context-path的"/alertCenter" 和 "v1" 必须和prometheus配置文件中的path_prefix、api_version匹配，
     * 后续如果需要修改context-path，需要去修改prometheus的配置或本接口的url路径
     *
     * @param paramList 告警信息
     * @return 返回成功信息
     */
    @PostMapping("/alerts")
    public AjaxResult alerts(@RequestBody @Valid List<JSONObject> paramList) {
        log.info("告警接口参数：" + paramList);
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
