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
 * OpsPackagePathDictEntity.java
 *
 * IDENTIFICATION
 * base-ops/src/main/java/org/opengauss/admin/plugin/domain/entity/ops/OpsPackagePathDictEntity.java
 *
 * -------------------------------------------------------------------------
 */

package org.opengauss.admin.plugin.domain.entity.ops;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.opengauss.admin.plugin.domain.BaseEntity;
import org.opengauss.admin.plugin.domain.model.ops.OpsPackagePathDictVO;

/**
 * @author wangchao
 * @date 2024/06/15 09:26
 */
@Data
@TableName(value = "ops_package_path_dict", autoResultMap = true)
@EqualsAndHashCode(callSuper = true)
public class OpsPackagePathDictEntity extends BaseEntity {
    @TableId
    private String id;
    private String os;
    private String osVersion;
    private String cpuArch;
    private String version;
    private String urlPath;
    private String pkgTmpUseVersion;
    private String packageNameTmp;

    public OpsPackagePathDictVO toVO() {
        OpsPackagePathDictVO vo = new OpsPackagePathDictVO();
        vo.setId(id);
        vo.setVersion(version);
        vo.setPackageNameTmp(packageNameTmp);
        vo.setOs(os);
        vo.setOsVersion(osVersion);
        vo.setPkgTmpUseVersion(pkgTmpUseVersion);
        vo.setCpuArch(cpuArch);
        vo.setUrlPath(urlPath);
        vo.setRemark(getRemark());
        return vo;
    }
}
