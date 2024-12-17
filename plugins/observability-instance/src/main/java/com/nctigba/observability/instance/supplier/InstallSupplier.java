package com.nctigba.observability.instance.supplier;

import com.gitee.starblues.annotation.Supplier;
import com.nctigba.observability.instance.model.dto.ExporterInstallDTO;
import com.nctigba.observability.instance.model.dto.PromInstallDTO;
import com.nctigba.observability.instance.service.ExporterInstallService;
import com.nctigba.observability.instance.service.PrometheusService;
import lombok.extern.slf4j.Slf4j;
import org.opengauss.admin.common.core.domain.AjaxResult;
import org.opengauss.admin.common.exception.CustomException;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;

/**
 * InstallSupplier
 *
 * @since 2024/3/6 17:31
 */
@Supplier("instance-install")
@Slf4j
public class InstallSupplier {
    @Autowired
    private PrometheusService prometheusService;
    @Autowired
    private ExporterInstallService exporterInstallService;

    /**
     * agent install
     *
     * @param exporterInstall ExporterInstallDTO
     * @return AjaxResult
     */
    @Supplier.Method("agent-install")
    public AjaxResult agentInstall(ExporterInstallDTO exporterInstall) {
        try {
            exporterInstallService.install(exporterInstall);
            return AjaxResult.success();
        } catch (Exception e) {
            log.error("agent install fail!", e);
            return AjaxResult.error("agent install fail! " + e.getMessage());
        }
    }

    /**
     * agent uninstall
     *
     * @param envId String
     * @return AjaxResult
     */
    @Supplier.Method("agent-uninstall")
    public AjaxResult agentUninstall(String envId) {
        try {
            exporterInstallService.uninstallExporter(envId);
            return AjaxResult.success();
        } catch (IOException | CustomException e) {
            log.error("agent uninstall fail!", e);
            return AjaxResult.error("agent uninstall fail! " + e.getMessage());
        }
    }

    /**
     * second prometheus install
     *
     * @param promInstall PromInstallDTO
     * @return AjaxResult
     */
    @Supplier.Method("prom-install")
    public AjaxResult promInstall(PromInstallDTO promInstall) {
        try {
            prometheusService.install(promInstall);
            return AjaxResult.success();
        } catch (Exception e) {
            log.error("prometheus install fail!", e);
            return AjaxResult.error("prometheus install fail! " + e.getMessage());
        }
    }

    /**
     * second prometheus uninstall
     *
     * @param envId String
     * @return AjaxResult
     */
    @Supplier.Method("prom-uninstall")
    public AjaxResult promUninstall(String envId) {
        try {
            prometheusService.uninstall(envId);
            return AjaxResult.success();
        } catch (Exception e) {
            log.error("prometheus uninstall fail!", e);
            return AjaxResult.error("prometheus uninstall fail! " + e.getMessage());
        }
    }
}
