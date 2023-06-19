/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package com.nctigba.datastudio.model.dto;


import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@NoArgsConstructor
@Data
public class DatabaseIndexDTO {
    private String uuid;
    private String webUser;
    private String connectionName;
    private String schema;
    private String sequenceName;
    private String tableName;
    private List<IndexDTO> indexs;
}

