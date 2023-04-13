/*
 * Copyright (c) 2022 Huawei Technologies Co.,Ltd.
 *
 * openGauss is licensed under Mulan PSL v2.
 * You can use this software according to the terms and conditions of the Mulan PSL v2.
 * You may obtain a copy of Mulan PSL v2 at:
 *
 * http://license.coscl.org.cn/MulanPSL2
 *
 * THIS SOFTWARE IS PROVIDED ON AN "AS IS" BASIS, WITHOUT WARRANTIES OF ANY KIND,
 * EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO NON-INFRINGEMENT,
 * MERCHANTABILITY OR FITFOR A PARTICULAR PURPOSE.
 * See the Mulan PSL v2 for more details.
 * -------------------------------------------------------------------------
 *
 * SessionVO.java
 *
 * IDENTIFICATION
 * base-ops/src/main/java/org/opengauss/admin/plugin/vo/ops/SessionVO.java
 *
 * -------------------------------------------------------------------------
 */

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
