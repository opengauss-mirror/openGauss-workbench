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
 * IOpsPackageManagerV2Service.java
 *
 * IDENTIFICATION
 * base-ops/src/main/java/org/opengauss/admin/plugin/service/ops/IOpsPackageManagerV2Service.java
 *
 * -------------------------------------------------------------------------
 */

package org.opengauss.admin.plugin.service.ops;

import com.baomidou.mybatisplus.extension.service.IService;
import org.opengauss.admin.common.core.dto.ops.PackageDto;
import org.opengauss.admin.plugin.domain.entity.ops.OpsPackageManagerEntity;
import org.opengauss.admin.plugin.domain.model.ops.OpsPackageVO;

import java.util.List;

/**
 * @author wangchao
 * @date 2024/06/15 09:26
 */
public interface IOpsPackageManagerV2Service extends IService<OpsPackageManagerEntity> {
    /**
     * Query the list of installed packages
     *
     * @param packageDict packageDict
     * @return List<OpsPackageVO>
     */
    List<OpsPackageVO> queryOpsPackageList(PackageDto packageDict);

    /**
     * Query the list of packages version number
     *
     * @return List<String>
     */
    List<String> listVersionNumber();

    /**
     * checking package list
     *
     * @param packageIds packageIds
     */
    void checkingPackageList(List<String> packageIds);

    /**
     * checking package
     *
     * @param packageId packageId
     * @return boolean
     */
    boolean checkingPackage(String packageId);

    /**
     * download package and save package info
     *
     * @param entity       entity
     * @param userId
     * @param wsBusinessId wsBusinessId
     */
    void saveOnlinePackage(OpsPackageManagerEntity entity, Integer userId, String wsBusinessId);

    /**
     * update package info
     *
     * @param packageEntity packageEntity
     * @param userId        userId
     * @param wsBusinessId  wsBusinessId
     */
    void updateOnlinePackage(OpsPackageManagerEntity packageEntity, Integer userId, String wsBusinessId);

    /**
     * checking package name is exist
     *
     * @param packageId packageId
     * @param name      name
     * @return boolean
     */
    boolean hasName(String packageId, String name);

    /**
     * save package info
     *
     * @param pkg    pkg
     * @param userId userId
     */
    void saveUploadPackage(OpsPackageManagerEntity pkg, Integer userId);

    /**
     * update package info
     *
     * @param pkg    pkg
     * @param userId userId
     */
    void updateUploadPackage(OpsPackageManagerEntity pkg, Integer userId);

    /**
     * del package list
     *
     * @param packageIds packageIds
     */
    void delPackage(List<String> packageIds);
}
