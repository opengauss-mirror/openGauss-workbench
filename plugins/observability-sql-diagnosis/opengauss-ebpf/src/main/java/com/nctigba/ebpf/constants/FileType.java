/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package com.nctigba.ebpf.constants;

/**
 * <p>
 * file constants
 * </p>
 *
 * @author luomeng@ncti-gba.cn
 * @since 2022/10/17 09:00
 */
public class FileType {
    private FileType() {
        throw new IllegalStateException("Utility class");
    }
    public static final String DEFAULT = ".txt";
    public static final String SVG = ".svg";

    public static final String STACKS = ".stacks";
}
