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
 * IOpsPackageManagerService.java
 *
 * IDENTIFICATION
 * base-ops/src/main/java/org/opengauss/admin/plugin/service/ops/IOpsPackageManagerService.java
 *
 * -------------------------------------------------------------------------
 */

package org.opengauss.admin.plugin.service.ops;

import com.baomidou.mybatisplus.extension.service.IService;
import org.opengauss.admin.common.exception.ops.OpsException;
import org.opengauss.admin.plugin.domain.entity.ops.OpsPackageManagerEntity;
import org.opengauss.admin.plugin.domain.model.ops.OpsPackageVO;
import org.opengauss.admin.plugin.enums.ops.OpenGaussVersionEnum;

/**
 * @author lhf
 * @date 2022/12/11 16:14
 **/
public interface IOpsPackageManagerService extends IService<OpsPackageManagerEntity> {
    /**
     * get the cpu arch of the package located at the given path.
     *
     * @param installPackagePath package path, resolved and validated against the upload root
     * @param version package version enum
     * @param userId current user id, used to locate the upload root
     * @return cpu arch string (e.g. x86_64 / aarch64)
     */
    String getCpuArchByPackagePath(String installPackagePath, OpenGaussVersionEnum version, Integer userId);

    /**
     * save a package record and its tar file on disk.
     *
     * @param pkg package entity to save
     * @param userId current user id, used to locate the upload root
     * @throws OpsException if the package path is illegal or saving fails
     */
    void savePackage(OpsPackageManagerEntity pkg, Integer userId) throws OpsException;

    /**
     * update a package record and its tar file on disk.
     *
     * @param pkg package entity to update
     * @param userId current user id, used to locate the upload root
     * @throws OpsException if the package path is illegal or updating fails
     */
    void updatePackage(OpsPackageManagerEntity pkg, Integer userId) throws OpsException;

    /**
     * delete a package record by id.
     *
     * @param id package record id
     */
    void delPackage(String id);

    /**
     * analyze the given package file name and infer its version info.
     *
     * @param pkgName package file name
     * @param pkgType package type
     * @return parsed package info
     */
    OpsPackageVO analysisPkg(String pkgName, String pkgType);

    /**
     * get the system upload path of the given user.
     *
     * @param userId current user id
     * @return configured upload path, empty string when the setting is missing
     */
    String getSysUploadPath(Integer userId);

    /**
     * check whether the given upload path is usable by the given user.
     *
     * @param path upload path to check
     * @param userId current user id
     * @return true if the path is available
     */
    boolean checkUploadPath(String path, Integer userId);

    /**
     * check whether a package with the given name already exists.
     *
     * @param name package name
     * @return true if the name is already taken
     */
    boolean hasName(String name);
}
