/*
 * Copyright (c) 2026 Huawei Technologies Co., Ltd.
 * openGauss-workbench is licensed under Mulan PSL v2.
 * You can use this software according to the terms and conditions of the Mulan PSL v2.
 * You may obtain a copy of Mulan PSL v2 at:
 *          http://license.coscl.org.cn/MulanPSL2
 * THIS SOFTWARE IS PROVIDED ON AN "AS IS" BASIS, WITHOUT WARRANTIES OF ANY KIND,
 * EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO NON-INFRINGEMENT,
 * MERCHANTABILITY OR FIT FOR A PARTICULAR PURPOSE.
 * See the Mulan PSL v2 for more details.
 */

package com.tools.monitor.util;

import com.tools.monitor.exception.ParamsException;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.IOException;

/**
 * FileUtil
 *
 * @author liu
 * @since 2022-10-01
 */
@Slf4j
public class FileUtil {
    /**
     * createFolder
     *
     * @param folderPath folderPath
     * @return File
     */
    public static File createFolder(String folderPath) {
        File folder = null;
        folder = new File(folderPath);
        if (!folder.exists()) {
            folder.mkdirs();
        }
        return folder;
    }

    /**
     * createFile
     *
     * @param filePath filePath
     * @return File
     */
    public static File createFile(String filePath) {
        File file = new File(filePath);
        try {
            if (!file.exists()) {
                file.createNewFile();
            }
        } catch (IOException exception) {
            log.error("createFile-->{}", exception.getMessage());
            throw new ParamsException("createFile fail");
        }
        return file;
    }
}
