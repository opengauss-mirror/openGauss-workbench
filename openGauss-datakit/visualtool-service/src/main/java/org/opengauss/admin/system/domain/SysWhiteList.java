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
 * SysWhiteList.java
 *
 * IDENTIFICATION
 * openGauss-visualtool/visualtool-service/src/main/java/org/opengauss/admin/system/domain/SysWhiteList.java
 *
 * -------------------------------------------------------------------------
 */


package org.opengauss.admin.system.domain;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.Date;
import java.util.HashSet;

/**
 * White List Model
 *
 * @author xielibo
 */
@TableName(value ="sys_white_list")
@Data
public class SysWhiteList {

    /**
     * id
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * title
     */
    private String title;

    /**
     * ipList
     */
    private String ipList;

    /**
     * createTime
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;

    /**
     * check whether duplicate ip exist in the ipList
     *
     * @return result of has duplicate ip
     */
    public boolean hasDuplicateIp() {
        String[] ips = ipList.split(",");
        HashSet<String> set = new HashSet<>();
        for (String ip : ips) {
            if (!set.add(ip)) {
                return true;
            }
        }
        return false;
    }
}
