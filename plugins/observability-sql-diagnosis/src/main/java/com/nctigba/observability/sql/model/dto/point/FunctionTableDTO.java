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
 *  FunctionTableDTO.java
 *
 *  IDENTIFICATION
 *  plugins/observability-sql-diagnosis/src/main/java/com/nctigba/observability/sql/model/dto/point/FunctionTableDTO.java
 *
 *  -------------------------------------------------------------------------
 */

package com.nctigba.observability.sql.model.dto.point;

import com.nctigba.observability.sql.constant.CommonConstants;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * FunctionTableDTO
 *
 * @author luomeng
 * @since 2024/3/8
 */
@Data
@NoArgsConstructor
public class FunctionTableDTO {
    private List<Map<String, String>> columns = new ArrayList<>();
    private List<Map<String, String>> data = new ArrayList<>();

    /**
     * Table data
     *
     * @param keys info
     */
    public FunctionTableDTO(String[] keys) {
        for (String key : keys) {
            columns.add(Map.of("key", key, CommonConstants.TITLE, key));
        }
    }

    /**
     * Add data
     *
     * @param map info
     */
    public void addData(Map<String, String> map) {
        data.add(map);
    }
}
