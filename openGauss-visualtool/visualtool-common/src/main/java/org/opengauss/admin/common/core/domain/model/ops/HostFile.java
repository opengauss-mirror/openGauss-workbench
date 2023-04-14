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
 * HostFile.java
 *
 * IDENTIFICATION
 * openGauss-visualtool/visualtool-common/src/main/java/org/opengauss/admin/common/core/domain/model/ops/HostFile.java
 *
 * -------------------------------------------------------------------------
 */


package org.opengauss.admin.common.core.domain.model.ops;

import lombok.Data;
import org.opengauss.admin.common.enums.ops.HostFileTypeEnum;

import java.io.File;

/**
 * @author lhf
 * @date 2022/8/7 22:37
 **/
@Data
public class HostFile {

    private String name;

    private HostFileTypeEnum type;
    /**
     * size（byte）
     */
    private Long size;

    public static HostFile build(File file) {
        HostFile hostFile = new HostFile();
        String fileName = file.getName();
        HostFileTypeEnum fileType = file.isFile() ? HostFileTypeEnum.FILE : HostFileTypeEnum.DIRECTORY;
        long length = file.length();

        hostFile.setName(fileName);
        hostFile.setType(fileType);
        hostFile.setSize(length);

        return hostFile;
    }

    public static HostFile of(String filename, HostFileTypeEnum hostFileTypeEnum, long size) {
        HostFile hostFile = new HostFile();
        hostFile.setName(filename);
        hostFile.setType(hostFileTypeEnum);
        hostFile.setSize(size);
        return hostFile;
    }
}
