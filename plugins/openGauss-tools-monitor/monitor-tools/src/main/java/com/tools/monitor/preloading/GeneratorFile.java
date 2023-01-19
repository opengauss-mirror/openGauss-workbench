package com.tools.monitor.preloading;

import ch.ethz.ssh2.Connection;
import com.tools.monitor.util.FileUtil;
import java.io.File;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

/**
 * GeneratorFile
 *
 * @author liu
 * @since 2022-10-01
 */
@Component
@Slf4j
public class GeneratorFile {

    @Value("${file.setting.config}")
    private String config;

    @Value("${file.setting.task}")
    private String jobConfig;

    @Value("${file.setting.relation}")
    private String relationConfig;

    private static Connection conn;

    @PostConstruct
    public void init() {
        File soucre = FileUtil.createFile(config);
        File job = FileUtil.createFile(jobConfig);
        File relation = FileUtil.createFile(relationConfig);
    }
}
