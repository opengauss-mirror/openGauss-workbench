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
 * base-ops/src/main/java/org/opengauss/admin/plugin/domain/model/ops/HostFile.java
 *
 * -------------------------------------------------------------------------
 */

package org.opengauss.admin.plugin.domain.model.ops;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import com.mysql.cj.jdbc.exceptions.CommunicationsException;
import lombok.extern.slf4j.Slf4j;
import org.opengauss.admin.common.utils.uuid.UUID;
import org.opengauss.admin.plugin.enums.ops.HostFileTypeEnum;
import lombok.Data;

import java.io.File;
import java.io.IOException;
import java.util.Date;

/**
 * @author lhf
 * @date 2022/8/7 22:37
 **/
@Slf4j
@Data
public class HostFile implements Comparable<HostFile> {
    private String id;
    private String name;
    private HostFileTypeEnum type;
    /**
     * size（byte）
     */
    private Long size;
    // file path
    private String path;
    // package manage name if exist
    private String pkgManagedName;
    private String openGaussVersionNum;
    private String openGaussVersion;
    private String cpuArch;
    private String os;
    private String remark;
    private Date updateTime;

    public static HostFile build(File file, OpsPackageVO vo) {
        HostFile hostFile = new HostFile();
        String fileName = file.getName();
        HostFileTypeEnum fileType = file.isFile() ? HostFileTypeEnum.FILE : HostFileTypeEnum.DIRECTORY;
        long length = file.length();
        if (StrUtil.isNotEmpty(vo.getName())) {
            hostFile.setUpdateTime(vo.getUpdateTime());
            hostFile.setOpenGaussVersionNum(vo.getPackageVersionNum());
            hostFile.setRemark(vo.getRemark());
        } else {
            hostFile.setUpdateTime(new Date(file.lastModified()));
            hostFile.setOpenGaussVersionNum(parseVersionNum(fileName));
        }
        hostFile.setId(UUID.fastUuid().toString());
        hostFile.setName(fileName);
        hostFile.setType(fileType);
        hostFile.setSize(length);
        try {
            hostFile.setPath(file.getCanonicalPath());
        } catch (IOException ex) {
            log.error("set host file path error: " + ex.getMessage());
        }

        hostFile.setCpuArch(vo.getCpuArch());
        hostFile.setOs(vo.getOs());
        hostFile.setOpenGaussVersion(vo.getPackageVersion());
        hostFile.setPkgManagedName(vo.getName());
        return hostFile;
    }

    public static HostFile of(String filename, HostFileTypeEnum hostFileTypeEnum, long size) {
        HostFile hostFile = new HostFile();
        hostFile.setName(filename);
        hostFile.setType(hostFileTypeEnum);
        hostFile.setSize(size);
        return hostFile;
    }

    private static String parseVersionNum(String fileName) {
        if (StrUtil.isNotEmpty(fileName)) {
            if (fileName.startsWith("openGauss-Lite-")) {
                String[] split = fileName.split("-");
                if (split.length >= 3) {
                    return split[2];
                } else {
                    return null;
                }
            } else if (fileName.startsWith("openGauss-")) {
                String[] split = fileName.split("-");
                if (split.length >= 2) {
                    return split[1];
                } else {
                    return null;
                }
            } else {
                //uuid
                return parseVersionNum(fileName.substring(fileName.indexOf("openGauss")));
            }
        }
        return null;
    }

    @Override
    public int compareTo(HostFile o) {
        return DateUtil.compare(o.getUpdateTime(), updateTime);
    }
}
