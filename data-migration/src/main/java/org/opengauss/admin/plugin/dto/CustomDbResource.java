package org.opengauss.admin.plugin.dto;

import lombok.Data;

/**
 * @className: CustomDbResource
 * @author: xielibo
 * @date: 2023-02-20 21:18
 **/
@Data
public class CustomDbResource {

    private String clusterName;

    private String dbUrl;

    private String userName;

    private String password;
}
