/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2024-2024. All rights reserved.
 */

package org.opengauss.visualtool.api.ops;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.opengauss.global.AppConfigLoader;
import org.opengauss.visualtool.api.model.ops.JdbcDbCluster;
import org.opengauss.visualtool.api.model.ops.JdbcDbClusterNode;
import org.hamcrest.Matchers;
import org.testng.annotations.Test;

import static org.opengauss.global.Constants.getRequestSpecification;

/**
 * JdbcDbClusterNodeController test
 *
 * @since 2024/10/30
 */
public class JdbcDbClusterNodeControllerTest {
    private static final Logger logger = LogManager.getLogger(JdbcDbClusterNodeControllerTest.class);

    private final JdbcDbClusterControllerTest jdbcDbClusterControllerTest = new JdbcDbClusterControllerTest();
    private JdbcDbCluster dbCluster;
    private JdbcDbClusterNode mysqlClusterNode;
    private JdbcDbClusterNode openGaussClusterNode;

    @Test
    public void setTestBasePath() {
        dbCluster = jdbcDbClusterControllerTest.addMysqlCluster();

        RestAssured.basePath = "/jdbcDbClusterNode";
        logger.info("JdbcDbClusterNodeControllerTest start.");
    }

    @Test(priority = 1)
    public void pingTest() {
        mysqlClusterNode = JdbcDbClusterNode.builder()
                .username(AppConfigLoader.getAppConfig().getDatabase().getJdbc().getMysql().getUsername())
                .password(AppConfigLoader.getAppConfig().getDatabase().getJdbc().getMysql().getPassword())
                .url(String.format(JdbcDbClusterControllerTest.MYSQL_URL_MODEL,
                        AppConfigLoader.getAppConfig().getDatabase().getJdbc().getMysql().getHostIp(),
                        AppConfigLoader.getAppConfig().getDatabase().getJdbc().getMysql().getPort()))
                .build();

        openGaussClusterNode = JdbcDbClusterNode.builder()
                .username(AppConfigLoader.getAppConfig().getDatabase().getJdbc().getOpengauss().getUsername())
                .password(AppConfigLoader.getAppConfig().getDatabase().getJdbc().getOpengauss().getPassword())
                .url(String.format(JdbcDbClusterControllerTest.OPENGAUSS_URL_MODEL,
                        AppConfigLoader.getAppConfig().getDatabase().getJdbc().getOpengauss().getHostIp(),
                        AppConfigLoader.getAppConfig().getDatabase().getJdbc().getOpengauss().getPort()))
                .build();

        getRequestSpecification()
                .contentType(ContentType.JSON)
                .body(mysqlClusterNode)
                .when()
                .post("/ping")
                .then()
                .body("code", Matchers.equalTo(200))
                .body("data", Matchers.equalTo(true));

        getRequestSpecification()
                .contentType(ContentType.JSON)
                .body(openGaussClusterNode)
                .when()
                .post("/ping")
                .then()
                .body("code", Matchers.equalTo(200))
                .body("data", Matchers.equalTo(true));
    }

    @Test(dependsOnMethods = "pingTest")
    public void addTest() {
        getRequestSpecification()
                .contentType(ContentType.JSON)
                .pathParam("clusterId", dbCluster.getClusterId())
                .body(mysqlClusterNode)
                .when()
                .post("/add/{clusterId}")
                .then()
                .body("code", Matchers.equalTo(500));
    }

    @Test(dependsOnMethods = "addTest")
    public void updateTest() {
        mysqlClusterNode = dbCluster.getNodes().get(0);
        mysqlClusterNode.setPassword("");

        getRequestSpecification()
                .contentType(ContentType.JSON)
                .pathParam("clusterNodeId", mysqlClusterNode.getClusterNodeId())
                .body(mysqlClusterNode)
                .when()
                .put("/{clusterNodeId}")
                .then()
                .body("code", Matchers.equalTo(200));
    }

    @Test(dependsOnMethods = "updateTest")
    public void delTest() {
        getRequestSpecification()
                .pathParam("clusterNodeId", mysqlClusterNode.getClusterNodeId())
                .when()
                .delete("/{clusterNodeId}")
                .then()
                .body("code", Matchers.equalTo(200));
    }
}
