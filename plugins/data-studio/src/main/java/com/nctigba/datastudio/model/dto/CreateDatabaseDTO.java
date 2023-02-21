package com.nctigba.datastudio.model.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Data
public class CreateDatabaseDTO {

    private String uuid;
    private String databaseName;
    private String databaseCode;
}
