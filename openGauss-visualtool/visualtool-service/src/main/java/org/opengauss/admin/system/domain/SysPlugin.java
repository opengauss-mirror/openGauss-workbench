package org.opengauss.admin.system.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.Tolerate;

/**
 * SysPlugin model
 *
 * @author xielibo
 */
@Data
@Builder
@TableName("sys_plugins")
public class SysPlugin {

    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * pluginId
     */
    private String pluginId;

    /**
     * bootstrapClass
     */
    private String bootstrapClass;

    /**
     * pluginDesc
     */
    private String pluginDesc;

    private String pluginDescEn;

    /**
     * logoPath
     */
    private String logoPath;

    /**
     * pluginType
     */
    private Integer pluginType;

    /**
     * pluginVersion
     */
    private String pluginVersion;

    /**
     * pluginProvider
     */
    private String pluginProvider;

    /**
     * status；1：started；2：stoped
     */
    private Integer pluginStatus;

    /**
     * isNeedConfigured
     */
    private Integer isNeedConfigured;

    /**
     * theme; dark light,default light
     */
    private String theme;


    @Tolerate
    public SysPlugin() {

    }

}
