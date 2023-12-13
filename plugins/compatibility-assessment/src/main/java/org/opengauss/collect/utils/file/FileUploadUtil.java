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

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.ZipUtil;
import lombok.extern.slf4j.Slf4j;
import org.opengauss.admin.common.exception.ServiceException;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;

/**
 * FileUploadUtil
 *
 * @author liu
 * @since 2023-09-17
 */
@Slf4j
public class FileUploadUtil {
    /**
     * saveAndUnzipFile
     *
     * @param file file
     * @param saveDirectory saveDirectory
     * @param unzipDirectory unzipDirectory
     */
    public static void saveAndUnzipFile(MultipartFile file, String saveDirectory, String unzipDirectory) {
        File saveDir = new File(saveDirectory);
        if (!saveDir.exists()) {
            saveDir.mkdirs();
        }
        File unzipDir = new File(unzipDirectory);
        if (!unzipDir.exists()) {
            unzipDir.mkdirs();
        }
        // 保存文件到本地磁盘
        File savedFile = null;
        try {
            savedFile = saveFile(file, saveDirectory);
        } catch (IOException exception) {
            log.info("FileUploadUtil occur error {}", exception.getMessage());
            throw new ServiceException("saveAndUnzipFile occur error");
        }
        // 解压文件到指定目录
        unzipFile(savedFile, unzipDirectory);
        // 删除上传的压缩文件
        FileUtil.del(savedFile);
    }

    private static File saveFile(MultipartFile file, String saveDirectory) throws IOException {
        File savedFile = new File(saveDirectory, file.getOriginalFilename());
        FileUtil.writeFromStream(file.getInputStream(), savedFile);
        return savedFile;
    }

    private static void unzipFile(File file, String unzipDirectory) {
        ZipUtil.unzip(file, new File(unzipDirectory));
    }

    /**
     * deleteFilesInDirectory
     *
     * @param directoryPath directoryPath
     */
    public static void deleteFilesInDirectory(String directoryPath) {
        File directory = FileUtil.file(directoryPath);
        if (directory.exists() && directory.isDirectory()) {
            FileUtil.clean(directory);
        }
    }
}
