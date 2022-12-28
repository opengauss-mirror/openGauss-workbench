package org.opengauss.admin.system.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * Plugin Config Data Model
 * @author xielibo
 */
@Data
@TableName("sys_plugin_config_data")
public class SysPluginConfigData {
    /**
     * id
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    private String pluginId;
    private String configData;
}
