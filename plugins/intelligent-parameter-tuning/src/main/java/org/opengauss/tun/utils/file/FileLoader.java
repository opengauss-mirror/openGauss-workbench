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

package org.opengauss.tun.utils.file;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;

/**
 * FileLoader
 *
 * @author liu
 * @since 2023-09-17
 */
@Slf4j
public class FileLoader {
    /**
     * getFilesInDirectory
     *
     * @param directoryPath directoryPath
     * @param names names
     * @return List<Map<String, Object>>
     */
    public static List<Map<String, Object>> getFilesInDirectory(String directoryPath, List<String> names) {
        File directory = new File(directoryPath);
        File[] files = directory.listFiles();
        List<Map<String, Object>> maps = new ArrayList<>();
        int index = 1;
        if (files != null) {
            for (File file : files) {
                if (file.isFile() && names.contains(file.getName())) {
                    long fileSize = file.length();
                    String size = String.format("%,d", fileSize);
                    Map<String, Object> map = new HashMap<>();
                    map.put("uid", String.valueOf(index));
                    map.put("name", file.getName());
                    map.put("file", Map.of("name", file.getName(), "type", ".sql", "size", size));
                    maps.add(map);
                    index++;
                }
            }
        }
        return maps;
    }

    /**
     * getAllFilenames
     *
     * @param directoryPath directoryPath
     * @return List<String>
     */
    public static List<String> getAllFilenames(String directoryPath) {
        List<String> filenames = new ArrayList<>();
        File directory = new File(directoryPath);
        // 检查目录是否存在
        if (!directory.exists()) {
            return filenames;
        }
        // 获取目录中的所有文件名称
        File[] files = directory.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isFile()) {
                    filenames.add(file.getName());
                }
            }
        }
        return filenames;
    }

    /**
     * getFilesFromNames
     *
     * @param fileNames     fileNames
     * @param directoryPath directoryPath
     * @return List<File>
     */
    public static List<File> getFilesFromNames(List<String> fileNames, String directoryPath) {
        List<File> fileList = new ArrayList<>();
        for (String fileName : fileNames) {
            File file = new File(directoryPath, fileName);
            if (file.exists() && file.isFile()) {
                fileList.add(file);
            } else {
                // Handle the case when file does not exist or is not a regular file
                log.error("File " + fileName + " does not exist or is not a regular file.");
            }
        }
        return fileList;
    }
}
