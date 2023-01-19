package com.tools.monitor.entity;

import lombok.Data;

import java.util.List;

/**
 * JsonConfig
 *
 * @author liu
 * @since 2022-10-01
 */
@Data
public class JsonConfig {
    private List<SysConfig> sysConfigs;
}
