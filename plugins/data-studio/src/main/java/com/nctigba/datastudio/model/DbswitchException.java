package com.nctigba.datastudio.model;


import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class DbswitchException extends RuntimeException {

    private String message;

}
