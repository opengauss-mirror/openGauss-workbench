/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2024-2024. All rights reserved.
 */

package org.opengauss.visualtool.api.ops;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hamcrest.Matchers;
import org.opengauss.global.AppConfigLoader;
import org.opengauss.global.Constants;
import org.opengauss.utils.EncryptionUtils;
import org.opengauss.visualtool.api.model.ops.Host;
import org.opengauss.visualtool.api.model.ops.HostUser;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import static org.opengauss.global.Constants.getRequestSpecification;

/**
 * HostUserController test
 *
 * @since 2024/10/30
 */
public class HostUserControllerTest {
    private static final Logger logger = LogManager.getLogger(HostUserControllerTest.class);

    private final HostControllerTest hostControllerTest = new HostControllerTest();
    private Host host;
    private HostUser rootUser;
    private HostUser hostUser;

    private boolean isHostCreated;
    private boolean isUserDeleted;

    @BeforeClass
    public void setUp() {
        host = hostControllerTest.addHost();
        isHostCreated = host != null && host.getHostId() != null;
        RestAssured.basePath = "/hostUser";
        logger.info("HostUserControllerTest start.");
    }

    @Test(priority = 1)
    public void addTest() {
        rootUser = page().jsonPath().getObject("rows.find { it.username == 'root' }", HostUser.class);

        EncryptionUtils.getEncryptionKey();
        hostUser = HostUser.builder()
                .hostId(host.getHostId())
                .username(AppConfigLoader.getAppConfig().getOpsHost().getUser().getName())
                .password(EncryptionUtils.encrypt(AppConfigLoader.getAppConfig().getOpsHost().getUser().getPassword()))
                .rootPassword(rootUser.getPassword())
                .build();

        getRequestSpecification()
                .contentType(ContentType.JSON)
                .body(hostUser)
                .when()
                .post()
                .then()
                .statusCode(200)
                .body("code", Matchers.equalTo(200));
    }

    private Response page() {
        Response response = getRequestSpecification()
                .pathParam("hostId", host.getHostId())
                .params(Constants.PAGE_PARAMS)
                .when()
                .get("/page/{hostId}");

        response.then()
                .statusCode(200)
                .body("code", Matchers.equalTo(200))
                .body("rows.hostId", Matchers.hasItem(host.getHostId()));

        return response;
    }

    @Test(dependsOnMethods = "addTest")
    public void pageTest() {
        Response response = page();
        response.then()
                .body("rows.username", Matchers.hasItem(hostUser.getUsername()));

        hostUser.setHostUserId(response.jsonPath()
                        .getString("rows.find { it.username == '" + hostUser.getUsername() + "' }.hostUserId"));
    }

    @Test(dependsOnMethods = "pageTest")
    public void editTest() {
        String validPassword = hostUser.getPassword();
        try {
            hostUser.setPassword("");
            getRequestSpecification()
                    .contentType(ContentType.JSON)
                    .body(hostUser)
                    .pathParam("hostUserId", hostUser.getHostUserId())
                    .when()
                    .put("/{hostUserId}")
                    .then()
                    .body("code", Matchers.equalTo(500));
        } finally {
            hostUser.setPassword(validPassword);
        }

        getRequestSpecification()
                .contentType(ContentType.JSON)
                .body(hostUser)
                .pathParam("hostUserId", hostUser.getHostUserId())
                .when()
                .put("/{hostUserId}")
                .then()
                .statusCode(200)
                .body("code", Matchers.equalTo(200));
    }

    @Test(dependsOnMethods = "editTest")
    public void listAllTest() {
        getRequestSpecification()
                .pathParam("hostId", host.getHostId())
                .when()
                .get("/listAll/{hostId}")
                .then()
                .statusCode(200)
                .body("code", Matchers.equalTo(200))
                .body("data.hostId", Matchers.hasItem(host.getHostId()))
                .body("data.username", Matchers.hasItem(hostUser.getUsername()))
                .body("data.username", Matchers.hasItem("root"));
    }

    @Test(dependsOnMethods = "listAllTest")
    public void listAllWithoutRootTest() {
        getRequestSpecification()
                .pathParam("hostId", host.getHostId())
                .when()
                .get("/listAllWithoutRoot/{hostId}")
                .then()
                .statusCode(200)
                .body("code", Matchers.equalTo(200))
                .body("data.hostId", Matchers.hasItem(host.getHostId()))
                .body("data.username", Matchers.hasItem(hostUser.getUsername()))
                .body("data.username", Matchers.not(Matchers.hasItem("root")));
    }

    @Test(dependsOnMethods = "listAllWithoutRootTest")
    public void hasRootPermissionTest() {
        getRequestSpecification()
                .pathParam("userId", rootUser.getHostUserId())
                .when()
                .get("/hasRootPermission/{userId}")
                .then()
                .statusCode(200)
                .body("code", Matchers.equalTo(200))
                .body("data", Matchers.equalTo(true));
    }

    @Test(dependsOnMethods = "hasRootPermissionTest")
    public void delTest() {
        getRequestSpecification()
                .pathParam("hostUserId", hostUser.getHostUserId())
                .when()
                .delete("/{hostUserId}")
                .then()
                .statusCode(200)
                .body("code", Matchers.equalTo(200));

        isUserDeleted = true;
    }

    /**
     * Best-effort cleanup so a failed chain does not leave the ordinary host user
     * and the host created by this test behind.
     */
    @AfterClass(alwaysRun = true)
    public void cleanUp() {
        if (!isUserDeleted) {
            deleteHostUserQuietly();
        }
        if (isHostCreated) {
            deleteHostQuietly();
        }
    }

    private void deleteHostUserQuietly() {
        String hostUserId = hostUser != null ? hostUser.getHostUserId() : null;
        if (hostUserId == null || hostUserId.isEmpty()) {
            return;
        }
        try {
            RestAssured.basePath = "/hostUser";
            getRequestSpecification()
                    .pathParam("hostUserId", hostUserId)
                    .when()
                    .delete("/{hostUserId}");
        } catch (Exception | AssertionError e) {
            logger.warn("Failed to clean up host user {}: {}", hostUserId, e.getMessage());
        }
    }

    private void deleteHostQuietly() {
        try {
            RestAssured.basePath = "/host";
            hostControllerTest.deleteHost();
        } catch (Exception | AssertionError e) {
            logger.warn("Failed to clean up host {}: {}", host != null ? host.getHostId() : null, e.getMessage());
        }
    }
}
