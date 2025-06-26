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

package org.opengauss.collect.utils.file;

import cn.hutool.core.util.ObjectUtil;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.ArchiveException;
import org.apache.commons.compress.archivers.ArchiveInputStream;
import org.apache.commons.compress.archivers.ArchiveStreamFactory;
import org.apache.commons.compress.utils.IOUtils;
import org.opengauss.collect.config.common.Constant;
import org.opengauss.collect.utils.AssertUtil;
import org.opengauss.collect.utils.CommandLineRunner;
import org.springframework.web.multipart.MultipartFile;

/**
 * FileUploadUtil
 *
 * @author liu
 * @since 2023-09-17
 */
@Slf4j
public class FileUploadUtil {
    /**
     * extractResource
     *
     * @param file                 file
     * @param destinationDirectory destinationDirectory
     */
    public static void extractResource(MultipartFile file, String destinationDirectory) {
        // Create only if it doesn't exist, ignore if it already exists
        try {
            InputStream inputStream = file.getInputStream();
            AssertUtil.isTrue(ObjectUtil.isEmpty(inputStream), "Resource does not exist");
            ArchiveInputStream ais = new ArchiveStreamFactory().createArchiveInputStream("zip", inputStream);
            ArchiveEntry entry;
            while ((entry = ais.getNextEntry()) != null) {
                if (!ais.canReadEntryData(entry)) {
                    continue;
                }
                File entryFile = new File(destinationDirectory, entry.getName());
                if (entry.isDirectory()) {
                    entryFile.mkdirs();
                } else {
                    FileOutputStream fos = new FileOutputStream(entryFile);
                    IOUtils.copy(ais, fos);
                    fos.close();
                }
            }
            ais.close();
        } catch (ArchiveException | IOException e) {
            log.error(e.getMessage());
        }
    }

    /**
     * delCache
     *
     * @param dataKitPath dataKitPath
     */
    public static void delCache(String dataKitPath) {
        // 评估结束后，删除assess目录下的缓存文件
        String command = String.format(Constant.CREATE_PATH, dataKitPath, dataKitPath, dataKitPath);
        boolean isSuccess = CommandLineRunner.runCommand("", "",
                command, Constant.ENV_PATH, Constant.TIME_OUT);
        AssertUtil.isTrue(!isSuccess, "Failed to create upload "
                + "directory or delete cache file, directory is" + dataKitPath
                + "Please check permissions");
    }
}
