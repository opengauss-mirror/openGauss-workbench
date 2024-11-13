/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2024-2024. All rights reserved.
 */

package org.opengauss.visualtool.api.model.ops;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * ops host
 *
 * @since 2024/10/30
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Host {
    private String hostId;
    private String publicIp;
    private String privateIp;
    private String password;
    private Boolean isRemember;
    private String azId;
    private String remark;
    private Integer port;
    private String name;
    private List<String> tags;
    private String username;
}
