package com.nctigba.datastudio.model.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Data
public class CreateDatabaseDTO {

    private String uuid;
    private String databaseName;
    private String databaseCode;
    private String compatibleType;
    private String collation;
    private String characterType;
    private String conRestrictions;
}
