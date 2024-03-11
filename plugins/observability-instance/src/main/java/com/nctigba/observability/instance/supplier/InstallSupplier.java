package com.nctigba.observability.instance.supplier;

import com.gitee.starblues.annotation.Supplier;
import com.nctigba.observability.instance.model.dto.ExporterInstallDTO;
import com.nctigba.observability.instance.service.ExporterInstallService;
import org.opengauss.admin.common.core.domain.AjaxResult;
import org.springframework.beans.factory.annotation.Autowired;

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
        return exporterInstallService.uninstallExporter(envId);
    }
}
