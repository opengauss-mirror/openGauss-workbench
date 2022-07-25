package com.nctigba.datastudio.model.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Data
public class DatabaseSynonymAttributeDTO {
    private String webUser;
    private String connectionName;
    private String synonymName;
}
