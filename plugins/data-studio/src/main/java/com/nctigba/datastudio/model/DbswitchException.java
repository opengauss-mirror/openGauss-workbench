/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package com.nctigba.datastudio.model;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Generated;

/**
 * DbswitchException
 *
 * @since 2023-6-26
 */
@Data
@Generated
@AllArgsConstructor
public class DbswitchException extends RuntimeException {
    private String message;
}
