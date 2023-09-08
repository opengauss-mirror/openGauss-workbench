/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package com.nctigba.datastudio.model.query;

import lombok.Data;
import lombok.Generated;

import java.util.List;
import java.util.Map;

/**
 * UserQuery
 *
 * @since 2023-6-26
 */
@Data
@Generated
public class UserQuery {
    private List<Map<String, String>> user;
    private List<Map<String, String>> role;
}
