package org.opengauss.admin.common.core.dto;

import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * @className: PluginConfigDataDto
 * @description: PluginConfigDataDto
 * @author: xielibo
 * @date: 2022-09-25 3:14 PM
 **/
@Data
public class PluginConfigDataDto {

    @NotNull
    private String pluginId;
    @NotNull
    private String configData;
}
