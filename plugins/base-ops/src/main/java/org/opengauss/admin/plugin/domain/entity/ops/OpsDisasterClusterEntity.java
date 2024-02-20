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

package org.opengauss.admin.plugin.domain.entity.ops;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * disaster cluster entity
 *
 * @author wbd
 * @since 2024/1/19 09:01
 **/
@Data
@TableName("ops_disaster_cluster")
public class OpsDisasterClusterEntity {
    @TableId
    private String clusterId;

    private String primaryClusterId;

    private String primaryClusterDeviceManagerName;

    private String standbyClusterId;

    private String standbyClusterDeviceManagerName;

    private String primaryJsonPath;

    private String standbyJsonPath;
}
