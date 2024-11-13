/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2024-2024. All rights reserved.
 */

package org.opengauss.visualtool.api.ops;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.opengauss.exception.ApiTestException;
import org.opengauss.global.AppConfigLoader;
import org.opengauss.global.Constants;
import org.opengauss.utils.FileUtils;
import org.opengauss.visualtool.api.model.ops.JdbcDbCluster;
import org.opengauss.visualtool.api.model.ops.JdbcDbClusterNode;
import org.hamcrest.Matchers;
import org.testng.annotations.Test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Locale;

import static org.opengauss.global.Constants.getRequestSpecification;

/**
 * JdbcDbClusterController test
 *
 * @since 2024/10/30
 */
public class JdbcDbClusterControllerTest {
    private static final Logger logger = LogManager.getLogger(JdbcDbClusterControllerTest.class);

    /**
     * MySQL url model
     */
    public static final String MYSQL_URL_MODEL = "jdbc:mysql://%s:%d";

    /**
     * openGauss url model
     */
    public static final String OPENGAUSS_URL_MODEL = "jdbc:opengauss://%s:%d/postgres";

    private JdbcDbCluster mysqlDbCluster;
    private JdbcDbClusterNode mysqlDbClusterNode;
    private JdbcDbCluster opengaussDbCluster;
    private JdbcDbClusterNode opengaussDbClusterNode;
    private final String singleNodeDeployType = "SINGLE_NODE";
    private final String clusterNameModel = "%s(%d)-1";

    private final File csvFile = new File("src/test/resources/temp/jdbc-template.csv");

    @Test
    public void setTestBasePath() {
        RestAssured.basePath = "/jdbcDbCluster";
        logger.info("JdbcDbClusterControllerTest start.");
    }

    @Test(priority = 1)
    public void addTest() {
        generateMysqlCluster();
        add(mysqlDbCluster);
    }

    private void add(JdbcDbCluster jdbcDbCluster) {
        getRequestSpecification()
                .contentType(ContentType.JSON)
                .body(jdbcDbCluster)
                .when()
                .post("/add")
                .then()
                .body("code", Matchers.equalTo(200));
    }

    private void generateMysqlCluster() {
        mysqlDbClusterNode = JdbcDbClusterNode.builder()
                .username(AppConfigLoader.getAppConfig().getDatabase().getJdbc().getMysql().getUsername())
                .password(AppConfigLoader.getAppConfig().getDatabase().getJdbc().getMysql().getPassword())
                .url(String.format(Locale.ROOT, MYSQL_URL_MODEL,
                        AppConfigLoader.getAppConfig().getDatabase().getJdbc().getMysql().getHostIp(),
                        AppConfigLoader.getAppConfig().getDatabase().getJdbc().getMysql().getPort()))
                .build();

        mysqlDbCluster = JdbcDbCluster.builder()
                .clusterName(String.format(Locale.ROOT, clusterNameModel,
                        AppConfigLoader.getAppConfig().getDatabase().getJdbc().getMysql().getHostIp(),
                        AppConfigLoader.getAppConfig().getDatabase().getJdbc().getMysql().getPort()))
                .deployType(singleNodeDeployType)
                .nodes(Collections.singletonList(mysqlDbClusterNode))
                .build();
    }

    private void generateOpengaussCluster() {
        opengaussDbClusterNode = JdbcDbClusterNode.builder()
                .username(AppConfigLoader.getAppConfig().getDatabase().getJdbc().getOpengauss().getUsername())
                .password(AppConfigLoader.getAppConfig().getDatabase().getJdbc().getOpengauss().getPassword())
                .url(String.format(Locale.ROOT, OPENGAUSS_URL_MODEL,
                        AppConfigLoader.getAppConfig().getDatabase().getJdbc().getOpengauss().getHostIp(),
                        AppConfigLoader.getAppConfig().getDatabase().getJdbc().getOpengauss().getPort()))
                .build();

        opengaussDbCluster = JdbcDbCluster.builder()
                .clusterName(String.format(Locale.ROOT, clusterNameModel,
                        AppConfigLoader.getAppConfig().getDatabase().getJdbc().getOpengauss().getHostIp(),
                        AppConfigLoader.getAppConfig().getDatabase().getJdbc().getOpengauss().getPort()))
                .deployType(singleNodeDeployType)
                .nodes(Collections.singletonList(opengaussDbClusterNode))
                .build();
    }

    @Test(dependsOnMethods = "addTest")
    public void pageTest() {
        page(mysqlDbCluster);
    }

    private void page(JdbcDbCluster jdbcDbCluster) {
        Response response = getRequestSpecification()
                .params(Constants.PAGE_PARAMS)
                .when()
                .get("/page");

        response.then()
                .body("code", Matchers.equalTo(200))
                .body("rows.name", Matchers.hasItem(jdbcDbCluster.getClusterName()));

        jdbcDbCluster.setClusterId(response.jsonPath()
                .getString("rows.find { it.name == '" + jdbcDbCluster.getClusterName() + "' }.clusterId"));
        JdbcDbClusterNode jdbcDbClusterNode = jdbcDbCluster.getNodes().get(0);
        jdbcDbClusterNode.setClusterNodeId(response.jsonPath()
                .getString("rows.find { it.name == '" + jdbcDbCluster.getClusterName() + "' }.nodes"
                        + ".find { it.url == '" + jdbcDbClusterNode.getUrl() + "' }.clusterNodeId"));
    }

    @Test(dependsOnMethods = "pageTest")
    public void updateTest() {
        String changeClusterNameModel = "%s(%d)-update";
        mysqlDbCluster.setClusterName(String.format(Locale.ROOT, changeClusterNameModel,
                AppConfigLoader.getAppConfig().getDatabase().getJdbc().getMysql().getHostIp(),
                AppConfigLoader.getAppConfig().getDatabase().getJdbc().getMysql().getPort()));

        getRequestSpecification()
                .contentType(ContentType.JSON)
                .pathParam("clusterId", mysqlDbCluster.getClusterId())
                .body(mysqlDbCluster)
                .when()
                .put("/{clusterId}")
                .then()
                .body("code", Matchers.equalTo(200));

        page(mysqlDbCluster);
    }

    @Test(dependsOnMethods = "updateTest")
    public void delTest() {
        delete(mysqlDbCluster);
    }

    private void delete(JdbcDbCluster jdbcDbCluster) {
        getRequestSpecification()
                .pathParam("clusterId", jdbcDbCluster.getClusterId())
                .when()
                .delete("/{clusterId}")
                .then()
                .body("code", Matchers.equalTo(200));
    }

    @Test(dependsOnMethods = "delTest")
    public void downloadTemplateTest() {
        Response response = getRequestSpecification()
                .when()
                .get("/downloadTemplate");

        response.then()
                .statusCode(200)
                .contentType(Matchers.containsString(ContentType.BINARY.toString()))
                .header("Content-Length", Matchers.not(Matchers.equalTo("0")));

        try (InputStream inputStream = response.asInputStream()) {
            FileUtils.createParentDirectoryIfNotExists(csvFile.getPath());
            FileUtils.writeToFile(inputStream, csvFile.getPath());
        } catch (IOException e) {
            throw new ApiTestException("Write jdbc-template.csv failed. ", e);
        }
    }

    @Test(dependsOnMethods = "downloadTemplateTest")
    public void importAnalysisTest() {
        generateOpengaussCluster();
        writeRecord();

        getRequestSpecification()
                .multiPart("file", csvFile)
                .when()
                .post("/importAnalysis")
                .then()
                .body("code", Matchers.equalTo(200))
                .body("data.succNum", Matchers.equalTo(1));
    }

    private void writeRecord() {
        String header = "";
        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(new FileInputStream(csvFile), StandardCharsets.UTF_8))) {
            header = br.readLine();
        } catch (IOException e) {
            throw new ApiTestException("Read jdbc-template.csv file failed. ", e);
        }

        String[] data = {opengaussDbCluster.getClusterName(), opengaussDbClusterNode.getUrl(),
                AppConfigLoader.getAppConfig().getDatabase().getJdbc().getOpengauss().getUsername(),
                AppConfigLoader.getAppConfig().getDatabase().getJdbc().getOpengauss().getPassword()};

        try (PrintWriter pw = new PrintWriter(
                new OutputStreamWriter(new FileOutputStream(csvFile), StandardCharsets.UTF_8))) {
            pw.println(header);
            pw.println(String.join(",", data));
        } catch (IOException e) {
            throw new ApiTestException("Write jdbc-template.csv file failed. ", e);
        }
    }

    @Test(dependsOnMethods = "importAnalysisTest")
    public void importClusterTest() {
        getRequestSpecification()
                .multiPart("file", csvFile)
                .when()
                .post("/importCluster")
                .then()
                .body("code", Matchers.equalTo(200));

        page(opengaussDbCluster);
        delete(opengaussDbCluster);
        FileUtils.deleteFile(csvFile.getPath());
    }

    /**
     * add MySql jdbc database cluster
     *
     * @return MySql JdbcDbCluster
     */
    public JdbcDbCluster addMysqlCluster() {
        RestAssured.basePath = "/jdbcDbCluster";
        generateMysqlCluster();
        add(mysqlDbCluster);
        page(mysqlDbCluster);
        return mysqlDbCluster;
    }

    /**
     * add openGauss jdbc database cluster
     *
     * @return openGauss JdbcDbCluster
     */
    public JdbcDbCluster addOpengaussCluster() {
        RestAssured.basePath = "/jdbcDbCluster";
        generateOpengaussCluster();
        add(opengaussDbCluster);
        page(opengaussDbCluster);
        return opengaussDbCluster;
    }

    /**
     * delete mysql jdbc database cluster
     */
    public void deleteMysqlCluster() {
        RestAssured.basePath = "/jdbcDbCluster";
        delete(mysqlDbCluster);
    }

    /**
     * delete openGauss jdbc database cluster
     */
    public void deleteOpengaussCluster() {
        RestAssured.basePath = "/jdbcDbCluster";
        delete(opengaussDbCluster);
    }
}
