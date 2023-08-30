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
 * AuditLogVO.java
 *
 * IDENTIFICATION
 * base-ops/src/main/java/org/opengauss/admin/plugin/vo/ops/AuditLogVO.java
 *
 * -------------------------------------------------------------------------
 */

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
