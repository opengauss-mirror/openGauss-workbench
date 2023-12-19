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
 *  WinInfoDTO.java
 *
 *  IDENTIFICATION
 *  plugins/data-studio/src/main/java/com/nctigba/datastudio/model/dto/WinInfoDTO.java
 *
 *  -------------------------------------------------------------------------
 */

package com.nctigba.datastudio.model.dto;

import lombok.Data;
import lombok.Generated;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * WinInfoDTO
 *
 * @since 2023-6-26
 */
@NoArgsConstructor
@Data
@Generated
public class WinInfoDTO {
    private Date lastDate;
    private String uuid;
    private String schema;
    private String tableName;

    /**
     * set win info dto
     *
     * @param uuid uuid
     * @param schema schema
     * @param tableName tableName
     */
    public void setWinInfoDTO(String uuid, String schema, String tableName) {
        this.lastDate = new Date();
        this.uuid = uuid;
        this.schema = schema;
        this.tableName = tableName;
    }
}
