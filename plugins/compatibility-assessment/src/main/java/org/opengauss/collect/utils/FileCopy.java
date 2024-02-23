/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2012-2022. All rights reserved.
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
 *
 * -------------------------------------------------------------------------
 */

package org.opengauss.collect.utils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.opengauss.admin.common.exception.ServiceException;

/**
 * DateUtil
 *
 * @author liu
 * @since 2022-10-01
 */
@Slf4j
public class FileCopy {
    /**
     * copyFilesToDirectory
     *
     * @param fileNames            fileNames
     * @param destinationDirectory destinationDirectory
     */
    public static void copyFilesToDirectory(String[] fileNames, String destinationDirectory) {
        // destinationDirectory 格式为 /aa/bb/
        for (String fileName : fileNames) {
            try {
                // 读取资源文件
                InputStream inputStream = FileCopy.class.getClassLoader().getResourceAsStream(fileName);
                if (inputStream != null) {
                    // 检查目标文件是否已经存在
                    File destinationFile = new File(destinationDirectory + fileName);
                    if (!destinationFile.exists()) {
                        // 复制文件到指定目录
                        FileUtils.copyInputStreamToFile(inputStream, destinationFile);
                    } else {
                        log.info("File " + fileName + " already exists in destination directory. Skipping.");
                    }
                } else {
                    log.error("Failed to obtain resource for fileName: " + fileName);
                }
            } catch (IOException e) {
                log.error("Error copying file " + fileName + ": " + e.getMessage());
                throw new ServiceException(e.getMessage());
            }
        }
    }

    /**
     * copyFile
     *
     * @param sourcePath sourcePath
     * @param targetPath targetPath
     */
    public static void copyFile(String sourcePath, String targetPath) {
        Path source = Path.of(sourcePath);
        Path target = Path.of(targetPath);
        try {
            // 如果目录不存在，创建它
            if (!Files.exists(target.getParent())) {
                Files.createDirectories(target.getParent());
            }
            // 复制文件
            Files.copy(source, target, StandardCopyOption.REPLACE_EXISTING);
            log.info("During compatibility evaluation, the report.html file was successfully copied");
        } catch (IOException e) {
            log.error("Copying the report.xml file failed during compatibility evaluation-->{}", e.getMessage());
            throw new ServiceException("copyFile report occur error");
        }
    }
}
