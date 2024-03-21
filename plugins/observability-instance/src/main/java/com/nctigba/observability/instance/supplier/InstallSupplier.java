package com.nctigba.observability.instance.supplier;

import com.gitee.starblues.annotation.Supplier;
import com.nctigba.observability.instance.model.dto.ExporterInstallDTO;
import com.nctigba.observability.instance.service.ExporterInstallService;
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
public class InstallSupplier {
    @Autowired
    private ExporterInstallService exporterInstallService;

    @Supplier.Method("agent-install")
    public AjaxResult agentInstall(ExporterInstallDTO exporterInstall) {
        return exporterInstallService.install(exporterInstall);
    }

    @Supplier.Method("agent-uninstall")
    public AjaxResult agentUninstall(String envId) {
        try {
            exporterInstallService.uninstallExporter(envId);
            return AjaxResult.success();
        } catch (IOException | CustomException e) {
            return AjaxResult.error(e.getMessage());
        }
    }
}
