/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package com.nctigba.datastudio.model.dto;

import lombok.Data;
import lombok.Generated;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * DatabaseUserInfoDTO
 *
 * @since 2023-6-26
 */
@NoArgsConstructor
@Data
@Generated
public class DatabaseUserInfoDTO {
    private String oid;
    private String name;
    private String type;
    private String password;
    private String beginDate;
    private String endDate;
    private int connectionLimit;
    private String resourcePool;
    private List<String> power;
    private String comment;
    private List<String> role;;
    private List<String> administrator;
    private List<String> belong;
}
