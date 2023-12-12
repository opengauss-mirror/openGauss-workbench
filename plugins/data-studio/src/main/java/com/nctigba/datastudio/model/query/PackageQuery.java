/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package com.nctigba.datastudio.model.query;

import lombok.Data;
import lombok.Generated;
import lombok.NoArgsConstructor;

/**
 * PackageRequest
 *
 * @since 2023-8-9
 */
@NoArgsConstructor
@Data
@Generated
public class PackageQuery {
    private String uuid;
    private String schema;
    private String packageName;
}
