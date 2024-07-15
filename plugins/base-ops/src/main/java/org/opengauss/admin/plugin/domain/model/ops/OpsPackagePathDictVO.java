/*
 * Copyright (c) 2024 Huawei Technologies Co.,Ltd.
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
 * OpsPackagePathDictVO.java
 *
 * IDENTIFICATION
 * base-ops/src/main/java/org/opengauss/admin/plugin/domain/model/ops/OpsPackagePathDictVO.java
 *
 * -------------------------------------------------------------------------
 */

package org.opengauss.admin.plugin.domain.model.ops;

import lombok.Data;
import org.opengauss.admin.plugin.domain.entity.ops.OpsPackagePathDictEntity;

/**
 * @author wangchao
 * @date 2024/06/15 09:26
 */
@Data
public class OpsPackagePathDictVO {
    private String id;
    private String os;
    private String cpuArch;
    private String version;
    private String urlPath;
    private String packageNameTmp;
    private String remark;

    public OpsPackagePathDictEntity toEntity() {
        OpsPackagePathDictEntity entity = new OpsPackagePathDictEntity();
        entity.setId(id);
        entity.setOs(os);
        entity.setCpuArch(cpuArch);
        entity.setVersion(version);
        entity.setUrlPath(urlPath);
        entity.setPackageNameTmp(packageNameTmp);
        entity.setRemark(remark);
        return entity;
    }

    public String buildFullPackageUrl(String installPackageUrlPrefix, String packageVersionNum, boolean isLatest) {
        if (isLatest) {
            return installPackageUrlPrefix + "/latest/" + urlPath
                    + "/" + String.format(packageNameTmp, packageVersionNum);
        } else {
            return installPackageUrlPrefix + "/" + packageVersionNum + "/" + urlPath
                    + "/" + String.format(packageNameTmp, packageVersionNum);
        }

    }
}
