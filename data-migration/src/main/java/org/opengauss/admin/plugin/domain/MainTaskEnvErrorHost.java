package org.opengauss.admin.plugin.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * @author xielibo
 * @date 2023/01/14 09:01
 **/
@Data
@TableName("tb_main_task_env_error_host")
public class MainTaskEnvErrorHost {

    /**
     * id
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    private Integer mainTaskId;

    private String runHostId;

    private String runHost;

    private Integer runPort;

    private String runUser;

    private String runPass;

}
