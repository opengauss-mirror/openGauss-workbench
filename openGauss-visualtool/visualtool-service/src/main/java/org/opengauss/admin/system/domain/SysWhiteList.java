package org.opengauss.admin.system.domain;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.Date;

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
}
