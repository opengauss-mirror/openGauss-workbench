/*
 * Copyright (c) 2026 Huawei Technologies Co., Ltd.
 * openGauss-workbench is licensed under Mulan PSL v2.
 * You can use this software according to the terms and conditions of the Mulan PSL v2.
 * You may obtain a copy of Mulan PSL v2 at:
 *          http://license.coscl.org.cn/MulanPSL2
 * THIS SOFTWARE IS PROVIDED ON AN "AS IS" BASIS, WITHOUT WARRANTIES OF ANY KIND,
 * EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO NON-INFRINGEMENT,
 * MERCHANTABILITY OR FIT FOR A PARTICULAR PURPOSE.
 * See the Mulan PSL v2 for more details.
 */

package com.nctigba.observability.instance.model.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * BlockTreeVO
 *
 * @author wuyuebin
 * @since 2024/3/25 15:23
 */
@Data
@Accessors(chain = true)
public class BlockTreeVO {
    private String pathid;
    private String depth;
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;
    @JsonSerialize(using = ToStringSerializer.class)
    private Long parentid;
    private String treeId;
    private String tree;
    private String datname;
    private String usename;
    private String applicationName;
    private String clientAddr;
    private String state;
    private String backendStart;
    private String query;
    private String waitStatus;
    private String waitEvent;
    private String lockmode;
    private List<BlockTreeVO> children;
}
