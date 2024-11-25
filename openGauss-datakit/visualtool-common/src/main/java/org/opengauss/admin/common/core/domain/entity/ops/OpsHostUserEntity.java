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
 * OpsHostUserEntity.java
 *
 * IDENTIFICATION
 * openGauss-visualtool/visualtool-common/src/main/java/org/opengauss/admin/common/core/domain/entity/ops
 * /OpsHostUserEntity.java
 *
 * -------------------------------------------------------------------------
 */


package org.opengauss.admin.common.core.domain.entity.ops;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import org.opengauss.admin.common.constant.CommonConstants;
import org.opengauss.admin.common.core.domain.BaseEntity;

import cn.hutool.core.util.StrUtil;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author lhf
 * @date 2022/8/6 16:09
 **/
@Data
@TableName("ops_host_user")
@EqualsAndHashCode(callSuper = true)
public class OpsHostUserEntity extends BaseEntity {
    @TableId
    private String hostUserId;
    private String username;
    private String password;
    private String hostId;
    private Boolean sudo;

    /**
     * Check if the user is the root user.
     *
     * @return boolean
     */
    public boolean isRootUser() {
        return StrUtil.equalsIgnoreCase(username, CommonConstants.ROOT_USER);
    }
}
