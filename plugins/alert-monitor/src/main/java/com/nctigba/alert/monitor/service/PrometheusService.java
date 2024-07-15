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
 *  PrometheusService.java
 *
 *  IDENTIFICATION
 *  plugins/alert-monitor/src/main/java/com/nctigba/alert/monitor/service/PrometheusService.java
 *
 *  -------------------------------------------------------------------------
 */

package com.nctigba.alert.monitor.service;

import com.nctigba.alert.monitor.model.entity.AlertConfigDO;
import com.nctigba.alert.monitor.model.entity.AlertTemplateRuleDO;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * PrometheusService
 *
 * @since 2023/12/8 15:44
 */
public interface PrometheusService {
    /**
     * When the program starts, it initializes the Prometheus configuration
     */
    void initPrometheusConfig();

    /**
     * updatePromConfigByAlertConfig
     *
     * @param alertConfigDO AlertConfigDO
     */
    void updatePrometheusConfig(AlertConfigDO alertConfigDO);

    /**
     * Get data from a Prometheus server based on promQL and time
     *
     * @param url http://IP:9090/api/v1/query_range?
     * @param port 9090
     * @param query query=promQL &start=1683316800 &end=1683489600 &step=691
     *
     *              step = Math.max(Math.floor(60 * 60 * 1000 / 250000), 1)
     *              Default of one hour,this.props.options.range=60 * 60 * 1000
     *
     * @param startTime startTime = endTime - this.props.options.range / 1000
     * @param endTime end = this.getEndTime().valueOf() / 1000
     * @return {"status":"success","data":{"resultType":"matrix",
     *        "result":[{"metric":{"instance":"6bd124fa-4a28-4f29-82a9-a81c880a5b32"},
     *        "values":[[1683472020,"1.5090909090907871"],[1683472365,"1.418181818182191"],
     *        [1683472710,"1.418181818182191"],[1683473055,"1.5090909090907871"],[1683473400,"1.4909090909085592"],[
     *        1683473745,"1.3454545454549844"],[1683474090,"1.3272727272727707"],[1683474435,"1.5454545454535662"],
     *        [1683474780,"1.2181818181819466"],[1683475125,"1.2545454545463883"],[1683475470,"1.399999999999153"],
     *        [1683475815,"1.87272727272682"],[1683476160,"1.836363636364041"],[1683476505,"100"],[1683476850,"100"],
     *        [1683477195,"1.45454545454578"],[1683477540,"1.5636363636374284"],[1683477885,"1.5454545454535662"],
     *        [1683478230,"1.3272727272735807"],[1683478575,"1.7454545454546206"],[1683478920,"1.472727272728008"],
     *        [1683479265,"1.4909090909085592"]]}]}}
     */
    List queryRange(String url, String port, String query, LocalDateTime startTime, LocalDateTime endTime);

    /**
     * use to update the prometheus rule config
     *
     * @param ruleConfigMap the key is templateId,the value is clusterNodeIds
     */
    void updateRuleConfig(Map<Long, String> ruleConfigMap);

    /**
     * update prometheus rule configuration by templateId
     *
     * @param templateId Long
     */
    void updateRuleConfigByTemplateId(Long templateId);

    /**
     * update prometheus rule configuration by template rule
     *
     * @param alertTemplateRuleDO AlertTemplateRule
     */
    void updateRuleByTemplateRule(AlertTemplateRuleDO alertTemplateRuleDO);

    /**
     * remove prometheus rule configuration by template rule
     *
     * @param alertTemplateRuleDO AlertTemplateRule
     */
    void removeRuleByTemplateRule(AlertTemplateRuleDO alertTemplateRuleDO);
}
