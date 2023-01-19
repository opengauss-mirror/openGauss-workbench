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
     * @param folderPath
     * @return
     */
    public static File createFolder(String folderPath) {
        File folder = null;
        try {
            folder = new File(folderPath);
            if (!folder.exists()) {
                folder.mkdirs();
            }
        } catch (Exception exception) {
            log.error("createFolder--->，{}", exception.getMessage());
        }
        return folder;
    }

    /**
     * createFile
     *
     * @param filePath
     * @return
     */
    public static File createFile(String filePath) {
        File file = new File(filePath);
        try {
            if (!file.exists()) {
                file.createNewFile();
            }
        } catch (IOException exception) {
            log.error("createFile-->{}", exception.getMessage());
            throw new ParamsException("File initialization failed");
        }
        return file;
    }
}
