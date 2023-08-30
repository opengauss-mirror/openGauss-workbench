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
 * OpsClusterVO.java
 *
 * IDENTIFICATION
 * base-ops/src/main/java/org/opengauss/admin/plugin/domain/model/ops/OpsClusterVO.java
 *
 * -------------------------------------------------------------------------
 */

package org.opengauss.admin.plugin.domain.model.ops;

import com.fasterxml.jackson.annotation.JsonFormat;
import org.opengauss.admin.plugin.domain.entity.ops.OpsClusterEntity;
import org.opengauss.admin.plugin.enums.ops.DeployTypeEnum;
import lombok.Data;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * @author lhf
 * @date 2022/9/26 17:34
 **/
@Data
public class OpsClusterVO {

    private String clusterId;
    private String clusterName;
    private String version;
    private String versionNum;
    private String databasePassword;
    private DeployTypeEnum deployType;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date lastCheckAt;

    private Map<String, Integer> checkSummary;

    private List<OpsClusterNodeVO> clusterNodes;

    public static OpsClusterVO of(OpsClusterEntity opsClusterEntity) {
        OpsClusterVO opsClusterVO = new OpsClusterVO();
        opsClusterVO.setClusterId(opsClusterEntity.getClusterId());
        opsClusterVO.setClusterName(opsClusterEntity.getClusterName());
        opsClusterVO.setVersion(opsClusterEntity.getVersion().name());
        opsClusterVO.setVersionNum(opsClusterEntity.getVersionNum());
        opsClusterVO.setDatabasePassword(opsClusterEntity.getDatabasePassword());
        opsClusterVO.setDeployType(opsClusterEntity.getDeployType());
        return opsClusterVO;
    }
}
