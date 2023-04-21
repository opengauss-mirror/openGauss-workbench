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
 * SysOperLog.java
 *
 * IDENTIFICATION
 * openGauss-visualtool/visualtool-service/src/main/java/org/opengauss/admin/system/domain/SysOperLog.java
 *
 * -------------------------------------------------------------------------
 */


package org.opengauss.admin.system.domain;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.Date;

/**
 * OperLog Model
 *
 * @author xielibo
 */
@Data
public class SysOperLog {
    private static final long serialVersionUID = 1L;

    /**
     * id
     */
    @TableId(value = "oper_id", type = IdType.AUTO)
    private Integer operId;

    /**
     * title
     */
    private String title;

    /**
     * businessType(0:other 1:save 2:update 3:delete)
     */
    private Integer businessType;

    /**
     * businessTypes array
     */
    @TableField(exist = false)
    private Integer[] businessTypes;

    /**
     * method
     */
    private String method;

    /**
     * requestMethod
     */
    private String requestMethod;

    /**
     * operatorType(0:other 1:SysUser)
     */
    private Integer operatorType;

    /**
     * operName
     */
    private String operName;


    /**
     * operUrl
     */
    private String operUrl;

    /**
     * operIp
     */
    private String operIp;

    /**
     * operLocation
     */
    private String operLocation;

    /**
     * operParam
     */
    private String operParam;

    /**
     * jsonResult
     */
    private String jsonResult;

    /**
     * status(0: success 1:error)
     */
    private Integer status;

    /**
     * errorMsg
     */
    private String errorMsg;

    /**
     * operTime
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @TableField(fill = FieldFill.INSERT)
    private Date operTime;

    /**
     * createBy
     */
    @TableField(exist = false)
    private String createBy;

    /**
     * createTime
     */
    @TableField(exist = false)
    private Date createTime;

    /**
     * updateBy
     */
    @TableField(exist = false)
    private String updateBy;

    /**
     * updateTime
     */
    @TableField(exist = false)
    private Date updateTime;

    /**
     * remark
     */
    @TableField(exist = false)
    private String remark;
}
