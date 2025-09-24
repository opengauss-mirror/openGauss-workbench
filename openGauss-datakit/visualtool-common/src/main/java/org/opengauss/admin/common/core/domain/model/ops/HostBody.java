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
 * HostBody.java
 *
 * IDENTIFICATION
 * openGauss-visualtool/visualtool-common/src/main/java/org/opengauss/admin/common/core/domain/model/ops/HostBody.java
 *
 * -------------------------------------------------------------------------
 */


package org.opengauss.admin.common.core.domain.model.ops;

import cn.hutool.core.util.StrUtil;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import org.opengauss.admin.common.constant.CommonConstants;
import org.opengauss.admin.common.core.domain.entity.ops.OpsHostEntity;
import org.opengauss.admin.common.core.domain.entity.ops.OpsHostUserEntity;
import org.opengauss.admin.common.core.vo.HostInfoVo;

import jakarta.validation.constraints.NotEmpty;

import java.util.List;
import java.util.Objects;

/**
 * @author lhf
 * @date 2022/8/11 20:07
 **/
@Slf4j
@Data
public class HostBody {
    @NotEmpty(message = "The IP address cannot be empty")
    private String publicIp;
    @NotEmpty(message = "The Intranet IP address cannot be empty")
    private String privateIp;
    private String password;
    private Boolean isRemember;
    private String azId;
    private String remark;
    private Integer port;
    private String name;
    private List<String> tags;
    private String username;

    public OpsHostEntity toHostEntity(HostInfoVo hostInfoVo) {
        OpsHostEntity hostEntity = new OpsHostEntity();
        hostEntity.setPublicIp(publicIp);
        hostEntity.setPrivateIp(privateIp);
        hostEntity.setPort(port);
        hostEntity.setHostname(hostInfoVo.getHostname());
        hostEntity.setRemark(remark);
        hostEntity.setOs(hostInfoVo.getOs());
        hostEntity.setOsVersion(hostInfoVo.getOsVersion());
        hostEntity.setCpuArch(hostInfoVo.getCpuArch());
        hostEntity.setName(name);
        return hostEntity;
    }

    /**
     * Convert to user entity
     *
     * @param hostId hostId
     * @return User
     */
    public OpsHostUserEntity toUser(String hostId) {
        OpsHostUserEntity hostUserEntity = new OpsHostUserEntity();
        hostUserEntity.setUsername(username);
        hostUserEntity.setHostId(hostId);
        if (hostUserEntity.isRootUser()) {
            hostUserEntity.setSudo(Boolean.TRUE);
        }
        if (Objects.nonNull(isRemember) && isRemember) {
            hostUserEntity.setPassword(password);
        }
        return hostUserEntity;
    }

    /**
     * Convert to user body
     *
     * @param hostId hostId
     * @return User
     */
    public HostUserBody toUserBody(String hostId) {
        HostUserBody hostUserBody = new HostUserBody();
        hostUserBody.setUsername(username);
        hostUserBody.setHostId(hostId);
        if (StrUtil.equalsIgnoreCase(username, CommonConstants.ROOT_USER)) {
            hostUserBody.setSudo(Boolean.TRUE);
        }
        hostUserBody.setPassword(password);
        return hostUserBody;
    }
}
