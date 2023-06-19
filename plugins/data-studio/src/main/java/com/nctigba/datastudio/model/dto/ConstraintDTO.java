/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package com.nctigba.datastudio.model.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Data
public class ConstraintDTO {
    private String attname;       //attribute  name
    private String conname;      //Constraint name
    private String oldConname;      //old Constraint name
    private String contype;      //Constraint type
    private String constraintdef;
    private Boolean condeferrable; //DEFERRED
    private String nspname;    //Foreign name space name
    private String tbname;    //Foreign table name
    private String colname;  //Foreign primary column
    private String description;
    private int type;  //1=add  2=delete 3=update
}
