package com.tools.monitor.config;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

/**
 * FileConfig
 *
 * @author liu
 * @since 2022-10-01
 */
@Configuration
@Data
public class FileConfig {

    public static String relationConfig;

    public static String dataSourceConfig;

    public static String taskConfig;

    @Value("${file.setting.relation}")
    public void setRelationConfig(String relationConfig) {
        this.relationConfig = relationConfig;
    }

    @Value("${file.setting.config}")
    public void setDataSourceConfig(String dataSourceConfig) {
        this.dataSourceConfig = dataSourceConfig;
    }

    @Value("${file.setting.task}")
    public void setTaskConfig(String taskConfig) {
        this.taskConfig = taskConfig;
    }

}
