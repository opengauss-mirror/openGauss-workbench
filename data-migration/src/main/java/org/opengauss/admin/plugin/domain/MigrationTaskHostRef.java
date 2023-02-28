package org.opengauss.admin.plugin.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
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
@Builder
@TableName("tb_migration_task_host_ref")
public class MigrationTaskHostRef {

    /**
     * id
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    private Integer mainTaskId;

    private String runHostId;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;

    @TableField(exist = false)
    private Integer runningTaskCount;
    @TableField(exist = false)
    private String host;
    @TableField(exist = false)
    private String hostName;
    @TableField(exist = false)
    private String password;
    @TableField(exist = false)
    private Integer port;
    @TableField(exist = false)
    private String user;

    @TableField(exist = false)
    private Integer tempPlaceholderCount = 0;
    @TableField(exist = false)
    private Integer maxRunCount = 6;


    public Integer getRunnableCount() {
        return maxRunCount - runningTaskCount - tempPlaceholderCount;
    }

    public void addPlaceHolderCount(){
        tempPlaceholderCount++;
    }
    @Tolerate
    public MigrationTaskHostRef(){

    }

}
