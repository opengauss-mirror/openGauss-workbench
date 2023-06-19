/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package com.nctigba.datastudio.model.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@NoArgsConstructor
@Data
public class WinInfoDTO {
    private Date lastDate;
    private String uuid;
    private String schema;
    private String tableName;

    public void setWinInfoDTO(String uuid, String schema, String tableName) {
        Date date = new Date();
        this.lastDate = date;
        this.uuid = uuid;
        this.schema = schema;
        this.tableName = tableName;
    }
}
