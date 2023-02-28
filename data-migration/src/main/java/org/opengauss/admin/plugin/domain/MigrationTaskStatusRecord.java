package org.opengauss.admin.plugin.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.Date;

/**
 * @author xielibo
 * @date 2023/01/14 09:01
 **/
@Data
@TableName("tb_migration_task_status_record")
public class MigrationTaskStatusRecord {

    /**
     * id
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    private Integer taskId;

    private Integer operateId;

    private Integer statusId;

    private String title;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;

    @TableField(exist = false)
    private String operateTitle;

    @TableField(exist = false)
    private Integer operateType;
}
