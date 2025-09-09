/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
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
 */

package org.opengauss.admin.common.utils;

import lombok.extern.slf4j.Slf4j;

import org.opengauss.admin.common.exception.ops.OpsException;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Locale;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Description: DataKit base tools common class
 * reference : org.opengauss.admin.plugin.service.impl.MigrationTaskHostRefServiceImpl
 * #checkJavaEnv(org.opengauss.admin.system.plugin.beans.SshLogin)
 *
 * @author: wangchao
 * @Date: 2025/6/17 17:31
 * @since 7.0.0-RC2
 */
@Slf4j
public class CommonUtils {
    private static final Pattern JDK_VERSION_PATTERN = Pattern.compile(
        "(?:openjdk|java) version \"(?:1\\.)?([8-9]|1\\d|\\d{2,})(?:[\\.-]|$|\\\")");
    private static final Pattern JDK_VERSION_SEC_PATTERN = Pattern.compile(
        "(?:1\\.)?([8-9]|1\\d|\\d{2,})(?:[\\.-]|$|\\\")");

    /**
     * java version info by command "java -version"
     * some example {@link org.opengauss.admin.system.service.agent.impl.JavaVersionParseTest}
     *
     * @param version version info by command "java -version"
     * @return java version major
     */
    public static int getJavaVersionMajor(String version) {
        if (version == null || version.isEmpty()) {
            return -1;
        }
        Matcher matcher = JDK_VERSION_PATTERN.matcher(version);
        if (matcher.find()) {
            String majorVersionStr = matcher.group(1);
            try {
                return Integer.parseInt(majorVersionStr);
            } catch (NumberFormatException e) {
                log.error("Failed to parse Java version: {}", majorVersionStr, e);
            }
        }
        // 备选匹配
        matcher = JDK_VERSION_SEC_PATTERN.matcher(version);
        if (matcher.find()) {
            String majorVersionStr = matcher.group(1);
            try {
                return Integer.parseInt(majorVersionStr);
            } catch (NumberFormatException e) {
                log.error("Failed to parse Java version: {}", majorVersionStr, e);
            }
        }
        log.error("Unrecognized Java version format: {}", version);
        return -1;
    }

    /**
     * <pre>
     * path validate contans :
     *     path null or empty validate
     *     path illegal char validate
     *     path length validate
     *     path deep validate
     *     path normalize validate
     *     path os validate
     * some example {@link org.opengauss.admin.system.service.agent.impl.PahtValidateTest}
     * </pre>
     *
     * @param path path
     * @throws OpsException OpsException
     */
    public static void pathValidate(String path) throws OpsException {
        PathValidatorHolder.combinedValidator().validate(path);
    }

    /**
     * path validator holder
     */
    private static class PathValidatorHolder {
        private static final PathValidator PATH_NULL_CHECK = path -> {
            if (Objects.isNull(path) || path.isEmpty()) {
                throw new OpsException("path is null or empty");
            }
        };

        /**
         * The blacklist character filtering intercepts paths containing special characters
         * (such as ;, |, $, \0) to prevent command injection
         */
        private static final Pattern ILLEGAL_CHARS = Pattern.compile("[;|$\0]");
        private static final PathValidator PATH_ILLEGAL_CHECK = path -> {
            if (ILLEGAL_CHARS.matcher(path).find()) {
                throw new OpsException("path contain illegal char: " + path);
            }
        };
        private static final PathValidator PATH_LENGTH_CHECK = path -> {
            if (Objects.isNull(path)) {
                throw new OpsException("path length must be less than 255, but is null");
            }
            if (path.length() > 255) {
                throw new OpsException(
                    String.format(Locale.getDefault(), "path length must be less than 255, but got %d", path.length()));
            }
        };
        private static final PathValidator PATH_DEEP_CHECK = path -> {
            Path normalizedPath = Paths.get(path).normalize();
            if (normalizedPath.getNameCount() > 5) {
                throw new OpsException("path deep must be less than 5, but got " + normalizedPath.getNameCount());
            }
        };
        private static final PathValidator PATH_NORMALIZE_CHECK = path -> {
            if (Paths.get(path).isAbsolute()) { // 仅检查绝对路径
                Path normalizedPath = Paths.get(path).normalize();
                if (!isPathEqual(Paths.get(path), normalizedPath)) {
                    throw new OpsException(
                        String.format(Locale.getDefault(), "Path normalized mismatch! Input: '%s' -> Normalized: '%s'",
                            path, normalizedPath.toString()));
                }
            }
        };
        private static final PathValidator PATH_OS_REGEX_CHECK = path -> {
            // Regular expression verification matches path formats according to system types
            // (e.g., Linux paths must start with '/', while Windows paths include a drive letter)
            String regex = isLinux() ? "^/[a-zA-Z0-9_./-]+$" : "^[a-zA-Z]:\\\\[a-zA-Z0-9_ .\\\\-]+$";
            if (!path.matches(regex)) {
                throw new OpsException("path regex not match: " + path);
            }
        };

        private static boolean isPathEqual(Path path1, Path path2) {
            if (isLinux()) {
                return path1.toAbsolutePath().normalize().equals(path2.toAbsolutePath());
            }
            return path1.toAbsolutePath().normalize().toString().equalsIgnoreCase(path2.toAbsolutePath().toString());
        }

        private static boolean isLinux() {
            return !System.getProperty("os.name").toLowerCase().contains("win");
        }

        /**
         * combined validator
         *
         * @return PathValidator
         */
        public static PathValidator combinedValidator() {
            return PATH_NULL_CHECK.andThen(PATH_ILLEGAL_CHECK)
                .andThen(PATH_LENGTH_CHECK)
                .andThen(PATH_NORMALIZE_CHECK)
                .andThen(PATH_DEEP_CHECK);
        }

        /**
         * PathValidator
         */
        @FunctionalInterface
        public interface PathValidator {
            /**
             * validate path
             *
             * @param path path
             * @throws OpsException OpsException
             */
            void validate(String path) throws OpsException;

            /**
             * combined validator
             *
             * @param after after
             * @return PathValidator
             */
            default PathValidator andThen(PathValidator after) {
                Objects.requireNonNull(after);
                return (String path) -> {
                    this.validate(path);
                    after.validate(path);
                };
            }
        }
    }
}
