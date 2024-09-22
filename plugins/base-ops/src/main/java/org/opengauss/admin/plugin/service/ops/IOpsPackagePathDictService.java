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
 * IOpsPackagePathDictService.java
 *
 * IDENTIFICATION
 * base-ops/src/main/java/org/opengauss/admin/plugin/service/ops/IOpsPackagePathDictService.java
 *
 * -------------------------------------------------------------------------
 */

package org.opengauss.admin.plugin.service.ops;

import com.baomidou.mybatisplus.extension.service.IService;
import org.opengauss.admin.common.core.dto.ops.PackageDto;
import org.opengauss.admin.plugin.domain.entity.ops.OpsPackagePathDictEntity;
import org.opengauss.admin.plugin.domain.model.ops.OpsPackagePathDictVO;
import org.opengauss.admin.plugin.domain.model.ops.OpsPackageVO;

import java.util.List;

/**
 * @author wangchao
 * @date 2024/06/15 09:26
 */
public interface IOpsPackagePathDictService extends IService<OpsPackagePathDictEntity> {

    List<OpsPackagePathDictVO> listPackagePathDict();

    /**
     * Check if the OS exists
     *
     * @param os os
     * @return boolean
     */
    boolean checkOsExists(String os);

    /**
     * Check if the cpuArch exists
     *
     * @param cpuArch cpuArch
     * @return boolean
     */
    boolean checkCpuArchExists(String cpuArch);

    /**
     * query package path dict
     *
     * @param packageDto         packageDto
     * @return OpsPackagePathDictVO
     */
    OpsPackagePathDictVO queryPackagePathDict(PackageDto packageDto);

    /**
     * Build and check package url is valid
     *
     * @param installPackageUrlPrefix installPackageUrlPrefix
     * @param packageVersionNum       packageVersionNum
     * @param packageDict             packageDict
     * @return OpsPackageVO
     */
    OpsPackageVO buildOpsPackage(String installPackageUrlPrefix, String packageVersionNum,
                                 OpsPackagePathDictVO packageDict);

    /**
     * check current environment is online
     *
     * @return boolean
     */
    boolean checkCurrentEnvironmentIsOnline();
}
