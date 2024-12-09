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
 *  InstallSupplier.java
 *
 *  IDENTIFICATION
 *  plugins/observability-log-search/src/main/java/com/nctigba/observability/log/supplier/InstallSupplier.java
 *
 *  -------------------------------------------------------------------------
 */

package com.nctigba.observability.log.supplier;

import cn.hutool.json.JSONObject;
import com.gitee.starblues.annotation.Supplier;
import com.nctigba.observability.log.model.dto.ElasticSearchInstallDTO;
import com.nctigba.observability.log.service.impl.ElasticsearchService;
import com.nctigba.observability.log.service.impl.FilebeatService;
import lombok.extern.slf4j.Slf4j;
import org.opengauss.admin.common.core.domain.AjaxResult;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * InstallSupplier
 *
 * @author wuyuebin
 * @since 2024/12/9 11:15
 */
@Supplier("log-install")
@Slf4j
public class InstallSupplier {
    @Autowired
    private FilebeatService filebeatService;
    @Autowired
    private ElasticsearchService elasticsearchService;

    /**
     * filebeat install
     *
     * @param path Install path
     * @param nodeId nodeId
     * @param obj param
     * @return AjaxResult
     */
    @Supplier.Method("filebeat-install")
    public AjaxResult filebeatInstall(String path, String nodeId, JSONObject obj) {
        try {
            filebeatService.install(path, nodeId, obj);
            return AjaxResult.success();
        } catch (Exception e) {
            log.error("filebeat install fail!", e);
            return AjaxResult.error("filebeat install fail! " + e.getMessage());
        }
    }

    /**
     * filebeat uninstall
     *
     * @param nodeId String
     * @return AjaxResult
     */
    @Supplier.Method("filebeat-uninstall")
    public AjaxResult filebeatUninstall(String nodeId) {
        try {
            filebeatService.uninstall(nodeId);
            return AjaxResult.success();
        } catch (Exception e) {
            log.error("filebeat uninstall fail!", e);
            return AjaxResult.error("filebeat uninstall fail! " + e.getMessage());
        }
    }

    /**
     * ElasticSearch install
     *
     * @param installDTO ElasticSearchInstallDTO
     * @return AjaxResult
     */
    @Supplier.Method("elastic-install")
    public AjaxResult elasticInstall(ElasticSearchInstallDTO installDTO) {
        try {
            elasticsearchService.install(installDTO);
            return AjaxResult.success();
        } catch (Exception e) {
            log.error("elasticsearch install fail!", e);
            return AjaxResult.error("elasticsearch install fail! " + e.getMessage());
        }
    }

    /**
     * ElasticSearch uninstall
     *
     * @param envId String
     * @return AjaxResult
     */
    @Supplier.Method("elastic-uninstall")
    public AjaxResult elasticUninstall(String envId) {
        try {
            elasticsearchService.uninstall(envId);
            return AjaxResult.success();
        } catch (Exception e) {
            log.error("elasticsearch uninstall fail!", e);
            return AjaxResult.error("elasticsearch uninstall fail! " + e.getMessage());
        }
    }
}
