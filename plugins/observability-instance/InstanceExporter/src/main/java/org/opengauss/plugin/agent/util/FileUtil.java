/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package org.opengauss.plugin.agent.util;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class FileUtil {
    /**
     * read file to line skip blank line
     */
    public static final void readFileLine(String name, Consumer<String> consumer)
            throws FileNotFoundException, IOException {
        if (consumer != null)
            readFileLine(name, (i, line) -> consumer.accept(line));
    }

    /**
     * read file to line skip blank line
     */
    public static final void readFileLine(String name, BiConsumer<Integer, String> consumer)
            throws FileNotFoundException, IOException {
        CmdUtil.readFromCmd("cat " + name, consumer);
    }
}