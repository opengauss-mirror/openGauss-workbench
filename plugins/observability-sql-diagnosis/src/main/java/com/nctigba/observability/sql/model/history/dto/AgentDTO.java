/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package com.nctigba.observability.sql.model.history.dto;

import lombok.Data;

/**
 * AgentDTO
 *
 * @author luomeng
 * @since 2023/6/9
 */
@Data
public class AgentDTO {
    private String res;
    private String mem;
    private String pr;
    private String cpu;
    private String state;
    private String pid;
    private String command;
    private String ni;
    private String user;
    private String shr;
    private String time;
    private String virt;
}
