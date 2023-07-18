/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package com.nctigba.observability.instance.dto.session;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class BlockTreeDto {
    private String pathid;
    private String depth;
    private String id;
    private String parentid;
    private String tree_id;
    private String datname;
    private String usename;
    private String application_name;
    private String client_addr;
    private String state;
}
