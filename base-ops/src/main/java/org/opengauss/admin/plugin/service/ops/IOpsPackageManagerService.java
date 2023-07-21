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
import org.opengauss.admin.common.core.domain.UploadInfo;
import org.opengauss.admin.common.exception.ops.OpsException;
import org.opengauss.admin.plugin.domain.entity.ops.OpsPackageManagerEntity;
import org.opengauss.admin.plugin.domain.model.ops.OpsPackageVO;
import org.opengauss.admin.plugin.enums.ops.OpenGaussVersionEnum;
import org.springframework.web.multipart.MultipartFile;

/**
 * @author lhf
 * @date 2022/12/11 16:14
 **/
public interface IOpsPackageManagerService extends IService<OpsPackageManagerEntity> {
    String getCpuArchByPackagePath(String installPackagePath, OpenGaussVersionEnum version);

    void savePackage(OpsPackageManagerEntity pkg, Integer userId) throws OpsException;

    void updatePackage(OpsPackageManagerEntity pkg, Integer userId) throws OpsException;

    void delPackage(String id);

    OpsPackageVO analysisPkg(String pkgName, String pkgType);

    @Deprecated
    UploadInfo upload(MultipartFile file, Integer userId) throws OpsException;

    boolean deletePkgTar(String path, String id);

    String getSysUploadPath(Integer userId);

    boolean checkUploadPath(String path, Integer userId);

    boolean hasName(String name);
}
