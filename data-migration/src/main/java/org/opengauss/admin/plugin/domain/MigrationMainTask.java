package org.opengauss.admin.plugin.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.Date;

/**
 * Main Task Model
 *
 * @author xielibo
 */
@TableName(value ="tb_migration_main_task")
@Data
public class MigrationMainTask {

    /**
     * id
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * task name
     */
    private String taskName;

    /**
     * exec status
     */
    private Integer execStatus;

    /**
     * exec progress
     */
    private String execProgress;

    private String createUser;

    /**
     * create time
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;

    /**
     * finish time
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date finishTime;

    /**
     * execTime time
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date execTime;
}
