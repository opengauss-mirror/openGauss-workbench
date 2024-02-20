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

import com.jcraft.jsch.Session;
import lombok.Data;
import org.opengauss.admin.plugin.domain.entity.ops.OpsClusterEntity;

/**
 * for context passing
 *
 * @author wbd
 * @since 2024/1/29 17:05
 **/
@Data
public class DisasterContext {
    // 执行过程传递使用
    // 主集群信息
    private OpsClusterEntity primaryClusterEntity;

    // 备集群信息
    private OpsClusterEntity standbyClusterEntity;

    // 主集群session对象
    private Session primarySession;

    // 备集群session对象
    private Session standbySession;

    // 主集群json路径
    private String primaryJsonPath;

    // 备集群json路径
    private String standbyJsonPath;
}
