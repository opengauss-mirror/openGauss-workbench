package com.nctigba.datastudio.model.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Data
public class DatabaseDropSynonymDTO {
    private String webUser;
    private String connectionName;
    private String schema;
    private String synonymName;
}
