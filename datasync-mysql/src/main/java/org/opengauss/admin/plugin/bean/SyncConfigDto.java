package org.opengauss.admin.plugin.bean;

import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * @className: SyncConfigDto
 * @description: SyncConfigDto
 * @author: xielibo
 * @date: 2022-10-28 12:55
 **/
@Data
public class SyncConfigDto {

    @NotNull
    public String mysqlHost;
    @NotNull
    public String mysqlPort;
    @NotNull
    public String mysqlUser;
    @NotNull
    public String mysqlPass;
    @NotNull
    public String mysqlSchema;
    @NotNull
    public String ogHost;
    @NotNull
    public String ogPort;
    @NotNull
    public String ogUser;
    @NotNull
    public String ogPass;
    @NotNull
    public String ogDatabase;
    @NotNull
    public String ogSchema;
}
