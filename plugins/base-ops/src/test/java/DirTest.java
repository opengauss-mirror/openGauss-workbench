/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

import lombok.extern.slf4j.Slf4j;

import org.junit.jupiter.api.Test;
import org.opengauss.admin.plugin.utils.PathUtils;

/**
 * DirTest
 *
 * @author: wangchao
 * @Date: 2025/9/11 21:21
 * @since 7.0.0-RC2
 **/
@Slf4j
public class DirTest {
    @Test
    void checkDir() {
        String dir = "/data2/testog1/opengauss";
        String[] split = dir.split("/");
        int length = split.length;
        if (length > 0) {
            String[] subString = new String[length - 1];
            System.arraycopy(split, 0, subString, 0, length - 1);
            String join = String.join("/", subString);
            log.info(join);
        }
    }

    private int chmodStringToInt(String perm) {
        int permInt = 0;
        for (int i = 0; i < perm.length(); i++) {
            char c = perm.charAt(i);
            switch (c) {
                case 'd':
                    permInt += 1000;
                    break;
                case 'r':
                    permInt += 100;
                    break;
                case 'w':
                    permInt += 10;
                    break;
                case 'x':
                    permInt += 1;
                    break;
                case '-':
                    break;
                default:
                    throw new IllegalArgumentException("Invalid permission character: " + c);
            }
        }
        return permInt;
    }

    @Test
    void testChmodStringToInt() {
        String perm = "drwxr-xr-x";
        log.info("Permission integer: " + chmodStringToInt(perm));
        log.info("Permission integer: " + convertPermission(perm));
    }

    private static int convertPermission(String permissionString) {
        if (permissionString == null || permissionString.length() != 10) {
            throw new IllegalArgumentException("Invalid permission string");
        }
        int owner = 0;
        if (permissionString.charAt(1) == 'r') {
            owner += 4;
        }
        if (permissionString.charAt(2) == 'w') {
            owner += 2;
        }
        if (permissionString.charAt(3) == 'x') {
            owner += 1;
        }
        int group = 0;
        if (permissionString.charAt(4) == 'r') {
            group += 4;
        }
        if (permissionString.charAt(5) == 'w') {
            group += 2;
        }
        if (permissionString.charAt(6) == 'x') {
            group += 1;
        }
        int others = 0;
        if (permissionString.charAt(7) == 'r') {
            others += 4;
        }
        if (permissionString.charAt(8) == 'w') {
            others += 2;
        }
        if (permissionString.charAt(9) == 'x') {
            others += 1;
        }
        return Integer.valueOf(owner + String.valueOf(group) + others);
    }

    @Test
    void checkDirConflict1() {
        String path1 = "/a/b/c";
        String path2 = "/a/b/c/d/e";
        String path3 = "/a/b";
        PathUtils pathUtils = new PathUtils();
        log.info("Conflict: " + pathUtils.checkDirNoConflict(path1, path2, path3));
    }

    @Test
    void checkDirNoConflict() {
        String path1 = "/a/b/c";
        String path2 = "/a/b/d/e";
        String path3 = "/a/b/e";
        PathUtils pathUtils = new PathUtils();
        log.info("Conflict: " + pathUtils.checkDirNoConflict(path1, path2, path3));
    }
}
