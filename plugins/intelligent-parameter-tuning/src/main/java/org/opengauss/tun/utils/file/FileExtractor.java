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

import cn.hutool.core.util.ObjectUtil;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.ArchiveException;
import org.apache.commons.compress.archivers.ArchiveInputStream;
import org.apache.commons.compress.archivers.ArchiveStreamFactory;
import org.apache.commons.compress.utils.IOUtils;
import org.opengauss.admin.common.exception.ServiceException;
import org.opengauss.tun.common.FixedTuning;
import org.opengauss.tun.domain.TuningLog;
import org.opengauss.tun.utils.AssertUtil;
import org.opengauss.tun.utils.DateUtil;

/**
 * FileExtractor
 *
 * @author liu
 * @since 2023-09-17
 */
@Slf4j
public class FileExtractor {
    /**
     * extractResource
     *
     * @param resourceName         resourceName
     * @param destinationDirectory destinationDirectory
     */
    public static void extractResource(String resourceName, String destinationDirectory) {
        // Create only if it doesn't exist, ignore if it already exists
        try {
            File destinationDir = new File(destinationDirectory);
            if (!destinationDir.exists() || destinationDirectory.equals(FixedTuning.SYSBENCH_FILE_PATH)) {
                destinationDir.mkdirs();
                InputStream inputStream = FileExtractor.class.getClassLoader().getResourceAsStream(resourceName);
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
            }
        } catch (ArchiveException | IOException e) {
            log.error(e.getMessage());
        }
    }

    /**
     * getLog
     *
     * @param log         log
     * @param executePath executePath
     * @return TuningLog
     */
    public static TuningLog getLog(TuningLog log, String executePath) {
        // data/tuning/%s_work/opengauss_tuning/  python环境路径
        String logName = log.getTrainingId() + ".log";
        String dbPath = executePath + FixedTuning.DB_LOG + logName;
        String tunPath = executePath + FixedTuning.TUNE_LOG + logName;
        String benchMarkPath = executePath + FixedTuning.BENCHMARK_LOG + logName;
        File dbfile = new File(dbPath);
        File tunfile = new File(tunPath);
        File benchMarkfile = new File(benchMarkPath);
        log.setDbSize(getFileSizeInKB(dbfile));
        log.setTuningSize(getFileSizeInKB(tunfile));
        log.setPressureSize(getFileSizeInKB(benchMarkfile));
        log.setDbPath(dbPath);
        log.setTuningPath(tunPath);
        log.setPressurePath(benchMarkPath);
        log.setDbStartTime(getFileLastModifiedTime(dbfile));
        log.setTuningStartTime(getFileLastModifiedTime(tunfile));
        log.setPressureStartTime(getFileLastModifiedTime(benchMarkfile));
        copyIfExists(dbPath, FixedTuning.WORK_PATH + FixedTuning.DB_LOG + logName);
        copyIfExists(tunPath, FixedTuning.WORK_PATH + FixedTuning.TUNE_LOG + logName);
        copyIfExists(benchMarkPath, FixedTuning.WORK_PATH + FixedTuning.BENCHMARK_LOG + logName);
        return log;
    }

    private static String getFileSizeInKB(File file) {
        if (file.exists()) {
            return file.length() / 1024 + "kb";
        }
        return "";
    }

    private static String getFileLastModifiedTime(File file) {
        if (file.exists()) {
            return DateUtil.getTime(file.lastModified());
        }
        return DateUtil.getTimeNow();
    }

    /**
     * copyIfExists
     *
     * @param sourcePath sourcePath
     * @param targetPath targetPath
     */
    public static void copyIfExists(String sourcePath, String targetPath) {
        Path source = Path.of(sourcePath);
        Path target = Path.of(targetPath);
        try {
            // If the source file exists, copy it
            if (Files.exists(source)) {
                // If the directory does not exist, create it
                if (!Files.exists(target.getParent())) {
                    Files.createDirectories(target.getParent());
                }
                // copy file
                Files.copy(source, target, StandardCopyOption.REPLACE_EXISTING);
                log.info("During The tuning process, the logs file was successfully copied");
            } else {
                log.info("During The tuning process, Copying log file failed");
            }
        } catch (IOException e) {
            log.error("Copying the logs file failed during The tuning process-->{}", e.getMessage());
            throw new ServiceException("copyFile report occur error");
        }
    }

    private static void deleteFiles(File file) {
        if (file.isDirectory()) {
            File[] files = file.listFiles();
            if (files != null) {
                for (File child : files) {
                    deleteFiles(child);
                }
            }
        }
        file.delete();
    }
}

