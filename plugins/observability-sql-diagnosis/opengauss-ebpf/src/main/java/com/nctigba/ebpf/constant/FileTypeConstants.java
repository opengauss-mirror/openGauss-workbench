/*
 *  Copyright (c) GBA-NCTI-ISDC. 2022-2024.
 *
 *  openGauss DataKit is licensed under Mulan PSL v2.
 *  You can use this software according to the terms and conditions of the Mulan PSL v2.
 *  You may obtain a copy of Mulan PSL v2 at:
 *
 *  http://license.coscl.org.cn/MulanPSL2
 *
 *  THIS SOFTWARE IS PROVIDED ON AN "AS IS" BASIS, WITHOUT WARRANTIES OF ANY KIND,
 *  EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO NON-INFRINGEMENT,
 *  MERCHANTABILITY OR FITFOR A PARTICULAR PURPOSE.
 *  See the Mulan PSL v2 for more details.
 *  -------------------------------------------------------------------------
 *
 *  FileTypeConstants.java
 *
 *  IDENTIFICATION
 *  plugins/observability-sql-diagnosis/opengauss-ebpf/src/main/java/com/nctigba/ebpf/constant/FileTypeConstants.java
 *
 *  -------------------------------------------------------------------------
 */

package com.nctigba.ebpf.constant;

/**
 * <p>
 * file constants
 * </p>
 *
 * @author luomeng@ncti-gba.cn
 * @since 2022/10/17 09:00
 */
public class FileTypeConstants {
    /**
     * Generate file names of .txt type
     */
    public static final String DEFAULT = ".txt";

    /**
     * Generate file names of .svg type
     */
    public static final String SVG = ".svg";

    /**
     * Generate file names of .STACKS type
     */
    public static final String STACKS = ".stacks";
}
