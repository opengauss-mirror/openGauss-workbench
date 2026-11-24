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
