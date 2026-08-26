/*
 * Copyright (c) 2025 Huawei Technologies Co.,Ltd.
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

package org.opengauss.admin.plugin.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

import org.opengauss.admin.plugin.exception.NoSupportTranscribeReplayVersionException;

import java.util.Arrays;

/**
 * TranscribeReplayVersion
 *
 * @since 2025/02/11
 */
@AllArgsConstructor
@Getter
public enum TranscribeReplayVersion {
    /**
     * replay version 7.0.0
     */
    LATEST("latest", "7.0.0");

    private final String path;
    private final String version;

    /**
     * get TranscribeReplayVersion of version
     *
     * @param version version
     * @return TranscribeReplayVersion
     */
    public static TranscribeReplayVersion versionOf(String version) {
        return Arrays.stream(TranscribeReplayVersion.values())
            .filter(ver -> ver.getVersion().equalsIgnoreCase(version))
            .findAny()
            .orElseThrow((() -> new NoSupportTranscribeReplayVersionException(version)));
    }
}
