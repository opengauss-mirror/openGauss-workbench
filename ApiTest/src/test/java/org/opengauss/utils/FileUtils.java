/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2024-2024. All rights reserved.
 */

package org.opengauss.utils;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.opengauss.exception.ApiTestException;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * FileUtils
 *
 * @since 2024/11/5
 */
public class FileUtils {
    private static final Logger logger = LogManager.getLogger(FileUtils.class);

    /**
     * Writes data from an InputStream to a file at the specified path.
     *
     * @param inputStream inputStream.
     * @param filePath filePath.
     * @throws IOException IOException
     */
    public static void writeToFile(InputStream inputStream, String filePath) throws IOException {
        try (FileOutputStream outputStream = new FileOutputStream(filePath)) {
            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }
        }
    }

    /**
     * Attempts to delete the file at the specified path.
     *
     * @param filePath filePath
     */
    public static void deleteFile(String filePath) {
        if (!new File(filePath).delete()) {
            logger.error("Failed to delete the file: " + filePath);
        }
    }

    /**
     * create parent directory if not exists
     *
     * @param filePath filePath
     */
    public static void createParentDirectoryIfNotExists(String filePath) {
        Path path = Paths.get(filePath);
        Path parent = path.getParent();

        if (parent != null && !Files.exists(parent)) {
            try {
                Files.createDirectories(parent);
            } catch (IOException e) {
                throw new ApiTestException("Unable to create the parent directory: " + parent, e);
            }
        }
    }
}
