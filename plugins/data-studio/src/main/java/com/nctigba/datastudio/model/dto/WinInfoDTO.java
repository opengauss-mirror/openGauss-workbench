/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
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
