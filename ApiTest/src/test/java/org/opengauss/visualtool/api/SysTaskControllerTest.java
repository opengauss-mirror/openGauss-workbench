/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */

package org.opengauss.visualtool.api;

import static org.opengauss.global.Constants.getRequestSpecification;
import static org.opengauss.global.Constants.getRequestSpecificationWithoutToken;

import io.restassured.RestAssured;
import io.restassured.response.Response;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hamcrest.Matchers;
import org.opengauss.exception.ApiTestException;
import org.opengauss.global.Constants;
import org.opengauss.utils.OpenGaussJdbcUtils;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;
import java.util.Map;
import java.util.OptionalInt;

/**
 * SysTaskController test
 *
 * @since 2026/08/18
 */
public class SysTaskControllerTest {
    private static final Logger logger = LogManager.getLogger(SysTaskControllerTest.class);

    private static final int EXEC_STATUS_CREATED = 0;
    private static final int EXEC_STATUS_PROCESSING = 1;
    private static final int EXEC_STATUS_STOP = 4;

    private static final String TEST_TASK_NAME_PREFIX = "__apitest_task_";

    /**
     * A plugin id that is never registered on the server. It must be non-null so that
     * start/stop/delete fall into the "No implementation found" branch instead of NPE,
     * while still keeping the record untouched by the historical delete implementation.
     */
    private static final String TEST_PLUGIN_ID = "__apitest_plugin__";

    private static final String INSERT_SQL = "INSERT INTO public.sys_task "
            + "(task_name, task_type, exec_status, create_time, plugin_id) VALUES (?, ?, ?, ?, ?) RETURNING id";
    private static final String DELETE_SQL = "DELETE FROM public.sys_task WHERE id = ? OR task_name = ?";

    private static Integer taskId;
    private static String taskName;

    @BeforeClass
    public void setUp() {
        taskName = TEST_TASK_NAME_PREFIX + System.currentTimeMillis();
        taskId = insertTestTask();
        logger.info("Created temporary sys_task id={}, name={}", taskId, taskName);
    }

    @AfterClass(alwaysRun = true)
    public void tearDown() {
        deleteTestTask();
    }

    @Test
    public void setTestBasePath() {
        RestAssured.basePath = "/sys/task";
        logger.info("SysTaskControllerTest start.");
    }

    @Test(priority = 1)
    public void listTest() {
        getRequestSpecification()
                .params(Constants.PAGE_PARAMS)
                .when()
                .get("/list")
                .then()
                .statusCode(200)
                .body("code", Matchers.equalTo(200))
                .body("total", Matchers.notNullValue())
                .body("rows", Matchers.notNullValue());
    }

    @Test(priority = 1)
    public void listAllTest() {
        getRequestSpecification()
                .when()
                .get("/list/all")
                .then()
                .statusCode(200)
                .body("code", Matchers.equalTo(200))
                .body("data", Matchers.notNullValue());
    }

    @Test(priority = 2, dependsOnMethods = "listAllTest")
    public void startTest() {
        getRequestSpecification()
                .pathParam("id", taskId)
                .when()
                .post("/start/{id}")
                .then()
                .statusCode(200)
                .body("code", Matchers.equalTo(200));
        org.testng.Assert.assertEquals(queryTaskExecStatus(taskId).orElseThrow(
                        () -> new ApiTestException("task exec status is not available after start")),
                EXEC_STATUS_PROCESSING,
                "task exec status should become PROCESSING after start");
    }

    @Test(priority = 2, dependsOnMethods = "startTest")
    public void stopTest() {
        getRequestSpecification()
                .pathParam("id", taskId)
                .when()
                .post("/stop/{id}")
                .then()
                .statusCode(200)
                .body("code", Matchers.equalTo(200));
        org.testng.Assert.assertEquals(queryTaskExecStatus(taskId).orElseThrow(
                        () -> new ApiTestException("task exec status is not available after stop")),
                EXEC_STATUS_STOP,
                "task exec status should become STOP after stop");
    }

    @Test(priority = 2, dependsOnMethods = "stopTest")
    public void removeTest() {
        getRequestSpecification()
                .pathParam("ids", taskId)
                .when()
                .delete("/{ids}")
                .then()
                .statusCode(200)
                .body("code", Matchers.equalTo(200));
    }

    @Test(priority = 3)
    public void listWithoutTokenTest() {
        getRequestSpecificationWithoutToken()
                .when()
                .get("/list")
                .then()
                .statusCode(200)
                .body("code", Matchers.equalTo(401));
    }

    private OptionalInt queryTaskExecStatus(int id) {
        Response response = getRequestSpecification()
                .when()
                .get("/list/all");
        if (response.jsonPath().getInt("code") != 200) {
            return OptionalInt.empty();
        }
        List<Map<String, Object>> tasks = response.jsonPath().getList("data");
        if (tasks == null) {
            return OptionalInt.empty();
        }
        for (Map<String, Object> task : tasks) {
            Object taskIdValue = task.get("id");
            if (taskIdValue != null && Integer.parseInt(String.valueOf(taskIdValue)) == id) {
                Object execStatus = task.get("execStatus");
                return execStatus == null ? OptionalInt.empty()
                        : OptionalInt.of(Integer.parseInt(String.valueOf(execStatus)));
            }
        }
        return OptionalInt.empty();
    }

    private int insertTestTask() {
        try (Connection connection = OpenGaussJdbcUtils.getConnection();
                PreparedStatement statement = connection.prepareStatement(INSERT_SQL)) {
            statement.setString(1, taskName);
            statement.setInt(2, 1);
            statement.setInt(3, EXEC_STATUS_CREATED);
            statement.setTimestamp(4, new Timestamp(System.currentTimeMillis()));
            statement.setString(5, TEST_PLUGIN_ID);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getInt("id");
                }
            }
            throw new ApiTestException("Failed to retrieve id of inserted temporary sys_task.");
        } catch (SQLException e) {
            logger.error("Failed to insert temporary sys_task.", e);
            throw new ApiTestException("Failed to insert temporary sys_task.", e);
        }
    }

    private void deleteTestTask() {
        try (Connection connection = OpenGaussJdbcUtils.getConnection();
                PreparedStatement statement = connection.prepareStatement(DELETE_SQL)) {
            statement.setObject(1, taskId);
            statement.setString(2, taskName);
            statement.executeUpdate();
            logger.info("Cleaned up temporary sys_task id={}, name={}", taskId, taskName);
        } catch (SQLException e) {
            logger.error("Failed to clean up temporary sys_task id={}, name={}", taskId, taskName, e);
        }
    }
}
