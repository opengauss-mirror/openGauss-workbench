package org.opengauss.admin.plugin.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.Tolerate;

import java.util.Date;

/**
 * @author xielibo
 * @date 2023/01/14 09:01
 **/
@Data
@TableName("tb_migration_task_operate_record")
@Builder
public class MigrationTaskOperateRecord {

    /**
     * id
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    private Integer taskId;

    private String title;

    private Integer operType;

    private String operUser;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date operTime;

    @Tolerate
    public MigrationTaskOperateRecord(){}
}
