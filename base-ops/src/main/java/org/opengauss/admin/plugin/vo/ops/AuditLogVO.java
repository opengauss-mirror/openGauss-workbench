package org.opengauss.admin.plugin.vo.ops;

import lombok.Data;

/**
 * @author lhf
 * @date 2022/10/12 17:39
 **/
@Data
public class AuditLogVO {
    private String time;
    private String type;
    private String result;
    private String userid;
    private String username;
    private String database;
    private String client_conninfo;
    private String object_name;
    private String detail_info;
    private String node_name;
    private String thread_id;
    private String local_port;
    private String remote_port;
}
