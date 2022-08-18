package org.opengauss.admin.system.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * Plugin Config Model
 * @author xielibo
 */
@Data
@TableName("sys_plugin_config")
public class SysPluginConfig {
    /**
     * id
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    private String pluginId;
    private String configJson;
}
