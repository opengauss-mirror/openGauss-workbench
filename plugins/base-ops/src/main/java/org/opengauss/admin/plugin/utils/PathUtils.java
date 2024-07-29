/*
 * Copyright (c) 2024 Huawei Technologies Co.,Ltd.
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
 * PathUtils.java
 *
 * IDENTIFICATION
 * plugins/base-ops/src/main/java/org/opengauss/admin/plugin/utils/PathUtils.java
 *
 * -------------------------------------------------------------------------
 */

package org.opengauss.admin.plugin.utils;

import org.springframework.stereotype.Component;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;

/**
 * PathUtils
 *
 * @author wangchao
 * @date 2024/6/22 9:41
 **/
@Component
public class PathUtils {
    private final boolean isLocalDevelopmentEnv;
    private final String rootDir;

    /**
     * constructor , init isLocalDevelopmentEnv
     */
    public PathUtils() {
        rootDir = getUserRootDir();
        isLocalDevelopmentEnv = checkLocalDevelopmentEnv();
    }

    private String getUserRootDir() {
        final String userDir = System.getProperty("user.dir");
        File[] roots = File.listRoots();
        return Arrays.stream(roots).map(File::getAbsolutePath).filter(userDir::contains).findFirst().orElse("");
    }

    /**
     * @param sysUploadPath
     * @return
     */
    public String getPath(String sysUploadPath) {
        return isLocalDevelopmentEnv ? rootDir + sysUploadPath : sysUploadPath;
    }

    /**
     * Check if the local development environment is Windows.
     *
     * @return boolean
     */
    private boolean checkLocalDevelopmentEnv() {
        String osName = System.getProperty("os.name").toLowerCase();
        return osName.contains("win");
    }

    /**
     * Check  if the three directories do not conflict.
     *
     * @param path1 path1
     * @param path2 path2
     * @param path3 path2
     * @return boolean
     */
    public boolean checkDirNoConflict(String path1, String path2, String path3) {
        Path pathOne = Paths.get(path1);
        Path pathTwo = Paths.get(path2);
        Path pathThree = Paths.get(path3);
        boolean isPath12Conflict = contains(pathOne, pathTwo, isLocalDevelopmentEnv);
        boolean isPath13Conflict = contains(pathOne, pathThree, isLocalDevelopmentEnv);
        boolean isPath21Conflict = contains(pathTwo, pathOne, isLocalDevelopmentEnv);
        boolean isPath23Conflict = contains(pathTwo, pathThree, isLocalDevelopmentEnv);
        boolean isPath31Conflict = contains(pathThree, pathOne, isLocalDevelopmentEnv);
        boolean isPath32Conflict = contains(pathThree, pathOne, isLocalDevelopmentEnv);
        return isPath12Conflict || isPath13Conflict || isPath21Conflict || isPath23Conflict || isPath31Conflict || isPath32Conflict;
    }

    private boolean contains(Path parent, Path child, boolean isLocalDevelopmentEnv) {
        if (isLocalDevelopmentEnv) {
            return parent.startsWith(child);
        }
        if (parent.equals(child)) {
            return true;
        }
        if (parent.isAbsolute() != child.isAbsolute()) {
            return false;
        }
        Path relative = parent.relativize(child);
        return relative.equals(Paths.get("."));
    }
}
