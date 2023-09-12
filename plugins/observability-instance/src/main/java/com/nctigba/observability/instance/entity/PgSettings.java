/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package com.nctigba.observability.instance.entity;

import com.baomidou.mybatisplus.annotation.TableName;

import lombok.Data;

/**
 * PgSettings.java
 *
 * @since 2023-08-25
 */
@Data
@TableName("pg_settings")
public class PgSettings {
    String name;
    String setting;
    String unit;
    String category;
    String shortDesc;
    String extraDesc;
    String context;
    String vartype;
    String source;
    String minVal;
    String maxVal;
    String enumvals;
    String bootVal;
    String resetVal;
    String sourcefile;
    String sourceline;
}