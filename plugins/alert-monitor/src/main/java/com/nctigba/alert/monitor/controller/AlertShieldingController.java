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
 *  AlertShieldingController.java
 *
 *  IDENTIFICATION
 *  plugins/alert-monitor/src/main/java/com/nctigba/alert/monitor/controller/AlertShieldingController.java
 *
 *  -------------------------------------------------------------------------
 */

package com.nctigba.alert.monitor.controller;

import com.nctigba.alert.monitor.constant.CommonConstants;
import com.nctigba.alert.monitor.model.entity.AlertShieldingDO;
import com.nctigba.alert.monitor.model.query.AlertShieldingQuery;
import com.nctigba.alert.monitor.service.AlertShieldingService;
import org.opengauss.admin.common.core.domain.AjaxResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

/**
 * AlertShieldingController
 *
 * @author luomeng
 * @since 2024/6/30
 */
@RestController
@RequestMapping("/api/v1/alertShielding")
public class AlertShieldingController extends BaseController {
    @Autowired
    private AlertShieldingService service;

    /**
     * Select AlertShielding by page
     *
     * @param query AlertShieldingQuery
     * @return AjaxResult
     */
    @GetMapping
    public AjaxResult selectPage(AlertShieldingQuery query) {
        return AjaxResult.success(service.selectByPage(query, startPage()));
    }

    /**
     * Select AlertShielding by id
     *
     * @param id Long
     * @return AjaxResult
     */
    @GetMapping("/{id}")
    public AjaxResult selectById(@PathVariable Long id) {
        return AjaxResult.success(service.selectById(id));
    }

    /**
     * Save or update AlertShielding
     *
     * @param alertShieldingDO AlertShieldingDO
     * @return AjaxResult
     */
    @PostMapping
    public AjaxResult saveOrUpdate(@RequestBody AlertShieldingDO alertShieldingDO) {
        if (alertShieldingDO.getId() == null) {
            alertShieldingDO.setCreateTime(LocalDateTime.now());
            alertShieldingDO.setIsDeleted(CommonConstants.IS_NOT_DELETE);
        } else {
            alertShieldingDO.setUpdateTime(LocalDateTime.now());
        }
        service.saveOrUpdate(alertShieldingDO);
        return AjaxResult.success();
    }

    /**
     * Enable AlertShielding by id
     *
     * @param id Long
     * @return AjaxResult
     */
    @PostMapping("/{id}/enable")
    public AjaxResult enable(@PathVariable Long id) {
        service.updateStatusById(id, CommonConstants.IS_ENABLE);
        return AjaxResult.success();
    }

    /**
     * Disable AlertShielding by id
     *
     * @param id Long
     * @return AjaxResult
     */
    @PostMapping("/{id}/disable")
    public AjaxResult disable(@PathVariable Long id) {
        service.updateStatusById(id, CommonConstants.IS_NOT_ENABLE);
        return AjaxResult.success();
    }

    /**
     * Batch enable AlertShielding
     *
     * @param ids String
     * @return AjaxResult
     */
    @PostMapping("/enableBatch")
    public AjaxResult enableBatch(@RequestParam String ids) {
        service.batchUpdateStatus(ids, CommonConstants.IS_ENABLE);
        return AjaxResult.success();
    }

    /**
     * Batch disable AlertShielding
     *
     * @param ids String
     * @return AjaxResult
     */
    @PostMapping("/disableBatch")
    public AjaxResult disableBatch(@RequestParam String ids) {
        service.batchUpdateStatus(ids, CommonConstants.IS_NOT_ENABLE);
        return AjaxResult.success();
    }

    /**
     * Delete AlertShielding by id
     *
     * @param id Long
     * @return AjaxResult
     */
    @DeleteMapping("/{id}")
    public AjaxResult deleteById(@PathVariable Long id) {
        service.deleteById(id);
        return AjaxResult.success();
    }

    /**
     * Batch delete AlertShielding
     *
     * @param ids String
     * @return AjaxResult
     */
    @DeleteMapping
    public AjaxResult batchDelete(@RequestParam String ids) {
        service.batchDelete(ids);
        return AjaxResult.success();
    }
}
