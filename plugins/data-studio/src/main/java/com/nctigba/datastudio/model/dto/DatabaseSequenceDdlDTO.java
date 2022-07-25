package com.nctigba.datastudio.model.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Data
public class DatabaseSequenceDdlDTO {
    private String webUser;
    private String connectionName;
    private String sequenceName;
    private String schema;
}
