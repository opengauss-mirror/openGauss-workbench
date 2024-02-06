/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2012-2022. All rights reserved.
 *
 * openGauss is licensed under Mulan PSL v2.
 * You can use this software according to the terms and conditions of the Mulan PSL v2.
 * You may obtain a copy of Mulan PSL v2 at:
 *
 * http://license.coscl.org.cn/MulanPSL2
 *
 * THIS SOFTWARE IS PROVIDED ON AN "AS IS" BASIS, WITHOUT WARRANTIES OF ANY KIND,
 * EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO NON-INFRINGEMENT,
 * MERCHANTABILITY OR FITFOR A PARTICULAR PURPOSE.
 * See the Mulan PSL v2 for more details.
 * -------------------------------------------------------------------------
 *
 *
 * -------------------------------------------------------------------------
 */

package org.opengauss.collect.web.controller;

import org.opengauss.collect.domain.Assessment;
import org.opengauss.collect.service.Evaluate;
import org.opengauss.collect.utils.response.RespBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * LinuxConfigController
 *
 * @author liu
 * @since 2022-10-01
 */
@RestController
@RequestMapping("/assess")
public class AssessmentController {
    @Autowired
    private Evaluate evaluate;

    /**
     * getAssessRes
     *
     * @param assessment * @param assessment
     * @param pageNum    pageNum
     * @param pageSize   pageSize
     * @return RespBean
     */
    @PostMapping("/all/result/{pageNum}/{pageSize}")
    public RespBean getAssessRes(@ModelAttribute Assessment assessment, @PathVariable int pageNum,
                                 @PathVariable int pageSize) {
        return evaluate.obtainAllEvaluationResults(assessment, pageNum, pageSize);
    }

    /**
     * getMysqlConfig
     *
     * @param ip ip
     * @return RespBean
     */
    @GetMapping("/mysql")
    public RespBean getMysqlConfig(@RequestParam String ip) {
        return evaluate.obtainMysqlByIP(ip);
    }

    /**
     * getOpenGauss
     *
     * @param ip ip
     * @return RespBean
     */
    @GetMapping("/openGauss")
    public RespBean getOpenGauss(@RequestParam String ip) {
        return evaluate.obtainOpenGaussByIP(ip);
    }

    /**
     * getMysqlIPs
     *
     * @return RespBean
     */
    @GetMapping("/mysql/ip")
    public RespBean getMysqlIPs() {
        return evaluate.obtainMysqlIp();
    }

    /**
     * getOpenGaussIPs
     *
     * @return RespBean
     */
    @GetMapping("/opengauss/ip")
    public RespBean getOpenGaussIPs() {
        return evaluate.openGaussIp();
    }

    /**
     * deleteAssess
     *
     * @param assessmentId assessmentId
     * @return RespBean
     */
    @GetMapping("/delete/{assessmentId}")
    public RespBean deleteAssess(@PathVariable Long assessmentId) {
        return evaluate.deleteAssess(assessmentId);
    }
}
