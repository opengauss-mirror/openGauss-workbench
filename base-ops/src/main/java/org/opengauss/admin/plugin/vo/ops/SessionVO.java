package org.opengauss.admin.plugin.vo.ops;

import lombok.Data;

/**
 * @author lhf
 * @date 2022/10/21 15:00
 **/
@Data
public class SessionVO {
    private String datid;
    private String datname;
    private String pid;
    private String sessionid;
    private String usesysid;
    private String usename;
    private String application_name;
    private String client_addr;
    private String client_hostname;
    private String client_port;
    private String backend_start;
    private String xact_start;
    private String query_start;
    private String state_change;
    private String waiting;
    private String enqueue;
    private String state;
    private String resource_pool;
    private String query_id;
    private String query;
    private String connection_info;
    private String unique_sql_id;
    private String trace_id;
}
