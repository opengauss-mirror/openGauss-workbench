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

package org.opengauss.third.party.tools.repository.impl;

import org.opengauss.third.party.tools.enums.ThirdPartyToolEnum;
import org.opengauss.third.party.tools.csv.CsvUtils;
import org.opengauss.third.party.tools.entity.BaseInstallInfo;
import org.opengauss.third.party.tools.repository.InstallInfoRepository;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Install info csv repository class
 *
 * @since 2026/6/8
 */
public class InstallInfoCsvRepository implements InstallInfoRepository {
    @Override
    public void save(ThirdPartyToolEnum thirdPartyToolEnum, BaseInstallInfo installInfo) throws IOException {
        if (installInfo == null) {
            return;
        }
        if (thirdPartyToolEnum == null) {
            throw new IllegalArgumentException("Argument 'thirdPartyToolEnum' can not be null");
        }

        String csvFilePath = thirdPartyToolEnum.getInstallInfoCsvFilePath();
        if (Files.exists(Paths.get(csvFilePath))) {
            CsvUtils.addRowToCsv(csvFilePath, installInfo);
        } else {
            CsvUtils.exportAsCsv(csvFilePath, Collections.singletonList(installInfo));
        }
    }

    @Override
    public void deleteById(ThirdPartyToolEnum thirdPartyToolEnum, String id) throws IOException {
        if (id == null || id.isBlank()) {
            return;
        }
        if (thirdPartyToolEnum == null) {
            throw new IllegalArgumentException("Argument 'thirdPartyToolEnum' can not be null");
        }

        String csvFilePath = thirdPartyToolEnum.getInstallInfoCsvFilePath();
        List<String> oldCsvLines = CsvUtils.loadCsvLines(csvFilePath);
        if (oldCsvLines == null || oldCsvLines.isEmpty()) {
            return;
        }

        List<String> newCsvLines = new ArrayList<>();
        newCsvLines.add(oldCsvLines.get(0));
        newCsvLines.addAll(oldCsvLines.stream()
                .skip(1)
                .filter(line -> !parseId(line).equals(id))
                .toList());
        CsvUtils.writeAllCsvLines(csvFilePath, newCsvLines);
    }

    private String parseId(String csvLine) {
        if (csvLine == null || csvLine.isBlank()) {
            return "";
        }
        return csvLine.split(",")[0];
    }
}
