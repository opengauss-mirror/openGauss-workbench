package org.opengauss.admin.system.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.Date;

/**
 * Task Model
 *
 * @author xielibo
 */
@TableName(value ="sys_task")
@Data
public class SysTask {

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
     * task type
     */
    private Integer taskType;

    /**
     * exec status
     */
    private Integer execStatus;

    /**
     * exec params
     */
    private String execParams;

    /**
     * exec progress
     */
    private Float execProgress;

    /**
     * exec hostId
     */
    private String execHostId;

    /**
     * plugin id
     */
    private String pluginId;

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
     * finish time
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date execTime;
}
