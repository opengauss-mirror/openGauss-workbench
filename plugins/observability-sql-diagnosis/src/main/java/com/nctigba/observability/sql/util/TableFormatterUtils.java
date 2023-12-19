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
 *  TableFormatterUtils.java
 *
 *  IDENTIFICATION
 *  plugins/observability-sql-diagnosis/src/main/java/com/nctigba/observability/sql/util/TableFormatterUtils.java
 *
 *  -------------------------------------------------------------------------
 */

package com.nctigba.observability.sql.util;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.opengauss.admin.common.exception.CustomException;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * TableFormatterUtil
 *
 * @author luomeng
 * @since 2023/6/25
 */
public class TableFormatterUtils {
    /**
     * File to table conversion
     *
     * @param file agent file
     * @param flag line first title
     * @return tableData
     */
    public static TableData format(MultipartFile file, String flag) {
        TableData table = null;
        try (var reader = new BufferedReader(new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {
            String[] keys = null;
            while (reader.ready()) {
                var line = reader.readLine();
                if (StringUtils.isBlank(line)) {
                    continue;
                }
                if (keys == null && line.startsWith(flag)) {
                    keys = line.trim().split("\\s+");
                    table = new TableData(keys);
                    continue;
                }
                if (table == null) {
                    continue;
                }
                String[] datas = line.split("\\s+");
                if (datas.length < table.getColumns().size()) {
                    break;
                }
                var map = new HashMap<String, String>();
                if (keys.length > 0) {
                    for (int i = 0; i < table.getColumns().size(); i++) {
                        map.put(keys[i], datas[i]);
                    }
                }
                table.addData(map);
            }
        } catch (IOException e) {
            throw new CustomException("table format err");
        }
        return table;
    }

    /**
     * tableData
     *
     * @author luomeng
     * @since 2023/6/25
     */
    @Data
    @NoArgsConstructor
    public static class TableData {
        private List<Map<String, String>> columns = new ArrayList<>();
        private List<Map<String, String>> data = new ArrayList<>();

        /**
         * Table data
         *
         * @param keys Table column
         */
        public TableData(String[] keys) {
            for (String key : keys) {
                columns.add(Map.of("key", key, "title", key));
            }
        }

        /**
         * Add table data
         *
         * @param map Table column
         */
        public void addData(Map<String, String> map) {
            data.add(map);
        }
    }
}