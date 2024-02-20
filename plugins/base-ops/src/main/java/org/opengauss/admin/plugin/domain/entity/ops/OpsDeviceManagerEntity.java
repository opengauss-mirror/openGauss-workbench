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

import javax.validation.constraints.Max;
import javax.validation.constraints.NotEmpty;

/**
 * device manager entity
 *
 * @author wbd
 * @since 2024/1/19 10:01
 **/
@Data
@TableName("ops_device_manager")
public class OpsDeviceManagerEntity {
    @TableId
    @NotEmpty
    @Max(64)
    private String name;

    @NotEmpty
    @Max(64)
    private String hostIp;

    @NotEmpty
    @Max(5)
    private String port;

    @NotEmpty
    @Max(64)
    private String userName;

    @NotEmpty
    @Max(256)
    private String password;

    @NotEmpty
    @Max(64)
    private String pairId;
}
