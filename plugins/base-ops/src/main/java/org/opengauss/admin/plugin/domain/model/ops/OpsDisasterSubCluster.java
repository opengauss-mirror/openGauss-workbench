/*
 * Copyright (c) 2022-2022 Huawei Technologies Co.,Ltd.
 *
 * openGauss is licensed under Mulan PSL v2.
 * You can use this software according to the terms and conditions of the Mulan PSL v2.
 * You may obtain a copy of Mulan PSL v2 at:
 *
 *           http://license.coscl.org.cn/MulanPSL2
 *
 * THIS SOFTWARE IS PROVIDED ON AN "AS IS" BASIS, WITHOUT WARRANTIES OF ANY KIND,
 * EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO NON-INFRINGEMENT,
 * MERCHANTABILITY OR FIT FOR A PARTICULAR PURPOSE.
 * See the Mulan PSL v2 for more details.
 */

package org.opengauss.admin.plugin.domain.model.ops;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * sub cluster return response body
 *
 * @author wbd
 * @since 2024/1/26 11:39
 **/
@Data
public class OpsDisasterSubCluster {
    private String clusterName;

    private String clusterRole;

    private String version;

    private String versionNum;

    private String deployType;

    private String dbUser;

    private Integer dbPort;

    private String envPath;

    private String masterIp;

    private List<String> slaveIps = new ArrayList<>();
}
