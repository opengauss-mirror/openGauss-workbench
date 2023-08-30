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
 * IHostUserService.java
 *
 * IDENTIFICATION
 * openGauss-visualtool/visualtool-service/src/main/java/org/opengauss/admin/system/service/ops/IHostUserService.java
 *
 * -------------------------------------------------------------------------
 */


package org.opengauss.admin.system.service.ops;

import com.baomidou.mybatisplus.extension.service.IService;
import org.opengauss.admin.common.core.domain.entity.ops.OpsHostUserEntity;
import org.opengauss.admin.common.core.domain.model.ops.HostUserBody;

import java.util.List;

/**
 * @author lhf
 * @date 2022/8/8 15:39
 **/
public interface IHostUserService extends IService<OpsHostUserEntity> {
    List<OpsHostUserEntity> listHostUserByHostId(String hostId);

    boolean removeByHostId(String hostId);

    List<OpsHostUserEntity> listHostUserByHostIdList(List<String> hostIdList);

    boolean add(HostUserBody hostUserBody);

    boolean edit(String hostUserId, HostUserBody hostUserBody);

    boolean del(String hostUserId);

    OpsHostUserEntity getOmmUserByHostId(String hostId);

    OpsHostUserEntity getRootUserByHostId(String hostId);

    void cleanPassword(String hostUserId);

    OpsHostUserEntity getHostUserByUsername(String hostId, String sshUsername);
}
