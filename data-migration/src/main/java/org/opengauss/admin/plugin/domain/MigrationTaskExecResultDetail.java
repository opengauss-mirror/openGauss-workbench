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
@TableName("tb_migration_task_exec_result_detail")
@Builder
public class MigrationTaskExecResultDetail {

    /**
     * id
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    private Integer taskId;

    private String execResultDetail;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;

    //1: full 2: incremental 3: reverse
    private Integer processType;

    @Tolerate
    public MigrationTaskExecResultDetail(){

    }

}
