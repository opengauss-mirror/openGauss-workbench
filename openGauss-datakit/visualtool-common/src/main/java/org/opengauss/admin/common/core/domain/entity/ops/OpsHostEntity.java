/*
 * Copyright (c) 2022 Huawei Technologies Co.,Ltd.
 *
 * openGauss is licensed under Mulan PSL v2.
 * You can use this software according to the terms and conditions of the Mulan PSL v2.
 * You may obtain a copy of Mulan PSL v2 at:
 *
 * http://license.coscl.org.cn/MulanPSL2
 *
 * THIS SOFTWARE IS PROVIDED ON AN "AS IS" BASIS, WITHOUT WARRANTIES OF ANY KIND,
 * EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO NON-INFRINGEMENT,
 * MERCHANTABILITY OR FITFOR A PARTICULAR PURPOSE.
 * See the Mulan PSL v2 for more details.
 * -------------------------------------------------------------------------
 *
 * OpsHostEntity.java
 *
 * IDENTIFICATION
 * openGauss-visualtool/visualtool-common/src/main/java/org/opengauss/admin/common/core/domain/entity/ops
 * /OpsHostEntity.java
 *
 * -------------------------------------------------------------------------
 */


package org.opengauss.admin.common.core.domain.entity.ops;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import org.opengauss.admin.common.core.domain.BaseEntity;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author lhf
 * @date 2022/8/6 16:08
 **/
@Data
@TableName("ops_host")
@EqualsAndHashCode(callSuper = true)
public class OpsHostEntity extends BaseEntity {
    @TableId
    private String hostId;
    private String hostname;
    private String privateIp;
    private String publicIp;
    private Integer port;
    private String os;
    private String osVersion;
    private String osBuild;
    private String cpuArch;
    private String cpuModel;
    private String cpuFreq;
    private int physicalCores;
    private int logicalCores;
    @TableField(exist = false)
    private Boolean isRemember;
    private String name;
    @TableField(exist = false)
    private String displayIp;

    /**
     * Get the display IP address.
     *
     * @return display info
     */
    public String getDisplayIp() {
        return publicIp != null && !publicIp.isEmpty() ? publicIp + "(" + privateIp + ")" : publicIp;
    }
}
