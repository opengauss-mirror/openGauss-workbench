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
