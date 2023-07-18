/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package com.nctigba.datastudio.model.dto;

import com.nctigba.datastudio.model.query.TablePartitionInfoQuery;
import com.nctigba.datastudio.model.query.TableUnderlyingInfoQuery;
import lombok.Data;
import lombok.Generated;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * DatabaseCreateTableDTO
 *
 * @since 2023-6-26
 */
@NoArgsConstructor
@Data
@Generated
public class DatabaseCreateTableDTO {
    private String uuid;
    private String schema;
    private TableUnderlyingInfoQuery tableInfo;
    private List<DatabaseCreUpdColumnDTO.CreUpdColumnDataDTO> column;
    private List<ConstraintDTO> constraints;
    private List<IndexDTO> indexs;
    private TablePartitionInfoQuery partitionInfo;
}
