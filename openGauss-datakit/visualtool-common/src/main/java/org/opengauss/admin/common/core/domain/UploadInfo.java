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
 * UploadInfo.java
 *
 * IDENTIFICATION
 * openGauss-visualtool/visualtool-common/src/main/java/org/opengauss/admin/common/core/domain/UploadInfo.java
 *
 * -------------------------------------------------------------------------
 */


package org.opengauss.admin.common.core.domain;

import lombok.Data;
import org.opengauss.admin.common.core.vo.UploadInfoVO;

import jakarta.validation.constraints.NotEmpty;

/**
 * File upload common info
 *
 * @author wangyl
 */
@Data
public class UploadInfo {
    /**
     * file upload path
     */
    @NotEmpty(message = "file path cannot be empty")
    private String realPath;
    /**
     * file name for UI
     */
    private String name;

    @Override
    public String toString() {
        return String.format("Real path: %s, File name: %s", realPath, name);
    }

    public UploadInfoVO toVO() {
        UploadInfoVO vo = new UploadInfoVO();
        vo.setName(name);
        vo.setRealPath(realPath);
        return vo;
    }
}
