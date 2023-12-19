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
 *  FileUtils.java
 *
 *  IDENTIFICATION
 *  plugins/observability-instance/InstanceExporter/
 *  src/main/java/com/nctigba/observability/instance/agent/util/FileUtils.java
 *
 *  -------------------------------------------------------------------------
 */

package com.nctigba.observability.instance.agent.util;

import java.io.IOException;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * Read file content util
 *
 * @since 2023/12/1
 */
public class FileUtils {
    /**
     * read file to line skip blank line
     *
     * @param nodeId   Node Id for database
     * @param name     name of file
     * @param consumer consumer
     * @throws IOException Read file error
     * @since 2023/12/1
     */
    public static final void readFileLine(String nodeId, String name, Consumer<String> consumer)
            throws IOException {
        if (consumer != null) {
            readFileLine(nodeId, name, (i, line) -> consumer.accept(line));
        }
    }

    /**
     * read file to line skip blank line
     *
     * @param nodeId Node Id for database
     * @param name name of file
     * @param consumer consumer
     * @throws IOException Read yml file error
     */
    public static final void readFileLine(String nodeId, String name, BiConsumer<Integer, String> consumer)
            throws IOException {
        CmdUtils.readFromCmd(nodeId, "cat " + name, consumer);
    }
}