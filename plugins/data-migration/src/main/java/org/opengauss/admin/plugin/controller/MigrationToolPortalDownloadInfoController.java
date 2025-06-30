package org.opengauss.admin.plugin.controller;

import org.opengauss.admin.common.core.domain.AjaxResult;
import org.opengauss.admin.plugin.enums.PortalType;
import org.opengauss.admin.plugin.service.MigrationToolPortalDownloadInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * MigrationToolPortalDownloadInfoController
 *
 * @author duanguoqiang
 * @date 2024/1/31
 * @description MigrationToolPortalDownloadInfoController
 */
@RestController
@RequestMapping("/portalDownloadInfo")
public class MigrationToolPortalDownloadInfoController {
    @Autowired
    private MigrationToolPortalDownloadInfoService portalDownloadInfoService;

    /**
     * get PortalDownloadInfo list based on the hostId
     *
     * @param hostId Host ID
     * @param portalType Portal Type
     * @return AjaxResult Response
     */
    @GetMapping("/list/{hostId}")
    public AjaxResult getPortalDownloadInfoList(
            @PathVariable("hostId") String hostId, @RequestParam(required = false) PortalType portalType) {
        return AjaxResult.success(portalDownloadInfoService.getPortalDownloadInfoList(hostId, portalType));
    }
}
