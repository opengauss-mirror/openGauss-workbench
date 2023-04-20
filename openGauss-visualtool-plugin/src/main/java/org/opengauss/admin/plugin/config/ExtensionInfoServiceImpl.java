package org.opengauss.admin.plugin.config;


import com.gitee.starblues.annotation.Extract;
import lombok.extern.slf4j.Slf4j;
import org.opengauss.admin.common.enums.PluginLicenseType;
import org.opengauss.admin.system.plugin.beans.PluginExtensionInfoDto;
import org.opengauss.admin.system.plugin.extract.PluginExtensionInfoExtract;
import org.springframework.stereotype.Service;

import java.util.Date;

import static org.opengauss.admin.plugin.config.PluginExtensionInfoConfig.PLUGIN_ID;

@Slf4j
@Service
@Extract(bus = PLUGIN_ID)
public class ExtensionInfoServiceImpl implements PluginExtensionInfoExtract {
    
    @Override
    public PluginExtensionInfoDto getPluginExtensionInfo() {
        PluginExtensionInfoDto dto = new PluginExtensionInfoDto();
        dto.setPluginId(PLUGIN_ID);
        dto.setPluginName("插件DEMO");
        dto.setPluginHome(PLUGIN_ID);
        dto.setPluginDevelopmentCompany("openGauss社区");
        dto.setPhoneNumber("400-123-4567");
        dto.setEmail("community@opengauss.org");
        dto.setCompanyAddress("opengauss.org");
        dto.setAuthAddress("/license");
        dto.setPluginLicenseType(PluginLicenseType.TRIAL);
        dto.setPluginExpirationTime(new Date());
        return dto;
    }
}
