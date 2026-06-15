/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */

package org.opengauss.third.party.tools.repository.impl;

import org.opengauss.third.party.tools.csv.CsvUtils;
import org.opengauss.third.party.tools.enums.ThirdPartyToolEnum;
import org.opengauss.third.party.tools.repository.ThirdPartyToolRepository;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collections;

/**
 * Third party tool csv repository class
 *
 * @since 2026/6/8
 */
public class ThirdPartyToolCsvRepository implements ThirdPartyToolRepository {
    @Override
    public void register(ThirdPartyToolEnum thirdPartyToolEnum) throws IOException {
        if (thirdPartyToolEnum == null) {
            return;
        }

        if (Files.exists(Paths.get(ThirdPartyToolEnum.THIRD_PARTY_TOOLS_CSV_FILE_PATH))) {
            CsvUtils.addRowToCsv(ThirdPartyToolEnum.THIRD_PARTY_TOOLS_CSV_FILE_PATH, thirdPartyToolEnum);
        } else {
            CsvUtils.exportAsCsv(ThirdPartyToolEnum.THIRD_PARTY_TOOLS_CSV_FILE_PATH,
                    Collections.singletonList(thirdPartyToolEnum));
        }
    }
}
