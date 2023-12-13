/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2012-2022. All rights reserved.
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
 *
 * -------------------------------------------------------------------------
 */

package org.opengauss.collect.domain;

import com.alibaba.fastjson.annotation.JSONField;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

/**
 * CacheConfig
 *
 * @author liu
 * @since 2023-10-15
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@TableName("assessment")
public class Assessment {
    @TableId(value = "assessment_id")
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Long assessmentId;
    private String sqlInputType;
    private String proccessPid;
    private String assessmenttype;
    private String filedir;
    private String sqltype;
    private String mysqlPassword;
    private String mysqlUser;
    private String mysqlPort;
    private String mysqlHost;
    private String mysqlDbname;
    private String opengaussUser;
    private String opengaussPassword;
    private String opengaussPort;
    private String opengaussHost;
    private String opengaussDbname;
    private String startTime;
    private String reportFileName;
    @TableField(exist = false)
    @JsonIgnore
    @JSONField(serialize = false, deserialize = false)
    private MultipartFile file;
}