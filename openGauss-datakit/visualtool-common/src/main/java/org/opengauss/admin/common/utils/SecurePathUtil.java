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

package org.opengauss.admin.common.utils;

import org.opengauss.admin.common.exception.CustomException;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.regex.Pattern;

/**
 * Unified path security validation utility.
 *
 * <p>All file read/write/delete and path concatenation operations MUST validate
 * that user-influenced paths stay inside the intended base directory via
 * {@link #requireUnder(Path, Path)} before touching the file system.
 *
 * @author wangchao
 * @date 2026/11/20
 * @since 7.0.0-RC2
 */
public final class SecurePathUtil {
    /**
     * allowed characters for a single path segment: letters, digits, dot, underscore, hyphen
     */
    private static final Pattern SAFE_SEGMENT = Pattern.compile("^[A-Za-z0-9._\\-]+$");

    private static final String SEGMENT_PATTERN_MSG = "Illegal path segment: %s";

    /**
     * separators forbidden inside a plain file name
     */
    private static final Pattern FILE_NAME_SEPARATOR = Pattern.compile("[\\\\/]");

    private static final String FILE_NAME_MSG = "Illegal file name: %s";

    private SecurePathUtil() {
    }

    /**
     * Validate that candidate is strictly inside (or equal to) base, resolving symbolic links.
     *
     * @param base base directory, must be absolute
     * @param candidate candidate path to check
     * @return true if candidate is inside base
     */
    public static boolean isUnder(Path base, Path candidate) {
        if (base == null || candidate == null) {
            return false;
        }
        Path realBase = toRealSafe(base.toAbsolutePath().normalize());
        Path realCandidate = toRealSafe(candidate.toAbsolutePath().normalize());
        return realCandidate.startsWith(realBase);
    }

    /**
     * String convenience overload of {@link #isUnder(Path, Path)}.
     *
     * @param base base directory
     * @param candidate candidate path
     * @return true if candidate is inside base
     */
    public static boolean isUnder(String base, String candidate) {
        if (base == null || candidate == null || base.isEmpty() || candidate.isEmpty()) {
            return false;
        }
        return isUnder(Paths.get(base), Paths.get(candidate));
    }

    /**
     * Validate that candidate is strictly inside base (equal to base is rejected),
     * resolving symbolic links. Use this for destructive operations such as
     * delete/walk, where the base directory itself must never be a valid target.
     *
     * @param base base directory, must be absolute
     * @param candidate candidate path to check
     * @return true if candidate is strictly inside base
     */
    public static boolean isStrictlyUnder(Path base, Path candidate) {
        if (base == null || candidate == null) {
            return false;
        }
        Path realBase = toRealSafe(base.toAbsolutePath().normalize());
        Path realCandidate = toRealSafe(candidate.toAbsolutePath().normalize());
        return !realCandidate.equals(realBase) && realCandidate.startsWith(realBase);
    }

    /**
     * String convenience overload of {@link #isStrictlyUnder(Path, Path)}.
     *
     * @param base base directory
     * @param candidate candidate path
     * @return true if candidate is strictly inside base
     */
    public static boolean isStrictlyUnder(String base, String candidate) {
        if (base == null || candidate == null || base.isEmpty() || candidate.isEmpty()) {
            return false;
        }
        return isStrictlyUnder(Paths.get(base), Paths.get(candidate));
    }

    /**
     * Validate that candidate is inside base, otherwise throw.
     * Use this at every entry point of file read/write/delete operations.
     *
     * @param base base directory
     * @param candidate candidate path to check
     * @return the validated candidate path
     * @throws CustomException if candidate escapes base
     */
    public static Path requireUnder(Path base, Path candidate) {
        if (!isUnder(base, candidate)) {
            throw new CustomException(String.format(
                "Illegal file path: %s is outside base directory %s", candidate, base));
        }
        return candidate;
    }

    /**
     * String convenience overload of {@link #requireUnder(Path, Path)}.
     *
     * @param base base directory
     * @param candidate candidate path
     * @return the validated candidate path
     * @throws CustomException if candidate escapes base
     */
    public static Path requireUnder(String base, String candidate) {
        if (base == null || candidate == null || base.isEmpty() || candidate.isEmpty()) {
            throw new CustomException("Illegal file path: base or candidate is empty");
        }
        return requireUnder(Paths.get(base), Paths.get(candidate));
    }

    /**
     * Validate that candidate is strictly inside base, otherwise throw.
     * Use this before destructive operations (delete, recursive walk) to make
     * sure the base directory itself can never become the deletion target.
     *
     * @param base base directory
     * @param candidate candidate path to check
     * @return the validated candidate path
     * @throws CustomException if candidate escapes base or equals base
     */
    public static Path requireStrictlyUnder(Path base, Path candidate) {
        if (!isStrictlyUnder(base, candidate)) {
            throw new CustomException(String.format(
                "Illegal file path: %s is outside (or equal to) base directory %s", candidate, base));
        }
        return candidate;
    }

    /**
     * String convenience overload of {@link #requireStrictlyUnder(Path, Path)}.
     *
     * @param base base directory
     * @param candidate candidate path
     * @return the validated candidate path
     * @throws CustomException if candidate escapes base or equals base
     */
    public static Path requireStrictlyUnder(String base, String candidate) {
        if (base == null || candidate == null || base.isEmpty() || candidate.isEmpty()) {
            throw new CustomException("Illegal file path: base or candidate is empty");
        }
        return requireStrictlyUnder(Paths.get(base), Paths.get(candidate));
    }

    /**
     * Sanitize a single path segment used for directory/file concatenation.
     * Rejects path separators, parent traversal, empty and blank names.
     *
     * @param segment raw segment from user input (e.g. version number, os, arch, file name)
     * @return the same segment if safe
     * @throws CustomException if the segment contains illegal characters
     */
    public static String sanitizeSegment(String segment) {
        if (segment == null || !SAFE_SEGMENT.matcher(segment).matches() || ".".equals(segment)
            || "..".equals(segment)) {
            throw new CustomException(String.format(SEGMENT_PATTERN_MSG, segment));
        }
        return segment;
    }

    /**
     * Sanitize a file name supplied by users (e.g. upload original filename or
     * package name stored in DB). Unlike {@link #sanitizeSegment(String)} which
     * enforces a strict character whitelist for directory segments, this method
     * only rejects what is dangerous for a file name:
     * <ul>
     * <li>path separators ({@code /} and {@code \})</li>
     * <li>traversal names ({@code .} and {@code ..})</li>
     * <li>control characters (including null byte, CR/LF, TAB)</li>
     * </ul>
     * Spaces, non-ASCII characters and other printable characters are allowed,
     * so legacy DB rows with such names keep working.
     *
     * @param fileName raw file name from user input or database
     * @return the same file name if safe
     * @throws CustomException if the file name contains illegal characters
     */
    public static String sanitizeFileName(String fileName) {
        if (fileName == null || fileName.trim().isEmpty()
            || ".".equals(fileName) || "..".equals(fileName)
            || FILE_NAME_SEPARATOR.matcher(fileName).find()
            || fileName.chars().anyMatch(c -> c < 0x20)) {
            throw new CustomException(String.format(FILE_NAME_MSG, fileName));
        }
        return fileName;
    }

    /**
     * Resolve a path to its real path. If the target itself does not exist yet
     * (e.g. before mkdir/transferTo), resolve the longest existing ancestor and
     * append the remainder, so parent-directory symbolic links are still honored
     * instead of silently falling back to the normalized (unresolved) path.
     *
     * @param path path to resolve
     * @return the real path, or the longest existing real ancestor with the
     *         non-existing remainder appended
     */
    private static Path toRealSafe(Path path) {
        try {
            return path.toRealPath();
        } catch (IOException ignore) {
            Path parent = path.getParent();
            if (parent == null) {
                return path;
            }
            return toRealSafe(parent).resolve(path.getFileName());
        }
    }
}
