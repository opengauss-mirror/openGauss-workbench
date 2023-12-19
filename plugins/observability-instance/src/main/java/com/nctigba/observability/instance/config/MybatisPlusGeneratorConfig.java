/*
 *  Copyright (c) GBA-NCTI-ISDC. 2022-2024.
 *
 *  openGauss DataKit is licensed under Mulan PSL v2.
 *  You can use this software according to the terms and conditions of the Mulan PSL v2.
 *  You may obtain a copy of Mulan PSL v2 at:
 *
 *  http://license.coscl.org.cn/MulanPSL2
 *
 *  THIS SOFTWARE IS PROVIDED ON AN "AS IS" BASIS, WITHOUT WARRANTIES OF ANY KIND,
 *  EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO NON-INFRINGEMENT,
 *  MERCHANTABILITY OR FITFOR A PARTICULAR PURPOSE.
 *  See the Mulan PSL v2 for more details.
 *  -------------------------------------------------------------------------
 *
 *  MybatisPlusGeneratorConfig.java
 *
 *  IDENTIFICATION
 *  plugins/observability-instance/
 *  src/main/java/com/nctigba/observability/instance/config/MybatisPlusGeneratorConfig.java
 *
 *  -------------------------------------------------------------------------
 */

package com.nctigba.observability.instance.config;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.generator.FastAutoGenerator;
import com.baomidou.mybatisplus.generator.config.OutputFile;

import java.util.Collections;

/**
 * Mybatis Plus code generator
 *
 * @since 2023/12/1
 */
public class MybatisPlusGeneratorConfig {
    private static final String URL =
            "jdbc:sqlite:data/observability-instance-data.db?date_string_format=yyyy-MM-dd HH:mm:ss";
    private static final String AUTHOR = "LouisYang";
    private static final String USERNAME = "";
    private static final String PASS = "";
    private static final String TABLE_NAME = "collect_template,collect_template_metrics,collect_template_node";
    private static final String PACKAGE_PARENT = "com.nctigba.observability.instance";
    private static final String PROJECT_PATH =
            "D:/projects/01Code/采集配置开发分支/plugins/observability-instance/src/main";

    public static void main(String[] args) {
        FastAutoGenerator.create(URL, USERNAME, PASS)
                .globalConfig(builder -> {
                    builder.outputDir(PROJECT_PATH + "/java")
                            .author(AUTHOR)
                    ;
                })
                .packageConfig(builder -> {
                    builder.parent(PACKAGE_PARENT)
                            .pathInfo(Collections.singletonMap(OutputFile.xml, PROJECT_PATH + "/resources/mapper"));
                })
                .strategyConfig(builder -> {
                    builder.addInclude(TABLE_NAME)
                            .entityBuilder()
                            .formatFileName("%sEntity")
                            .idType(IdType.AUTO)
                            .enableLombok()
                            .fileOverride()
                            .serviceBuilder()
                            .formatServiceFileName("%sService")
                            .mapperBuilder();
                })
                .templateConfig(builder -> {
                    builder.controller(null)
                            .service("/templates/service.java.vm")
                            .serviceImpl("/templates/serviceImpl.java.vm");
                })
                .execute();
    }
}
