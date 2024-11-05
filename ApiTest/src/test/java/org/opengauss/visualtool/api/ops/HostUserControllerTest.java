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

    @Test
    public void setTestBasePath() {
        host = hostControllerTest.addHost();

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
                .body("code", Matchers.equalTo(200));
    }

    @Test(dependsOnMethods = "addTest")
    public void pageTest() {
        Response response = page();
        response.then()
                .body("rows.username", Matchers.hasItem(hostUser.getUsername()));

        hostUser.setHostUserId(response.jsonPath()
                        .getString("rows.find { it.username == '" + hostUser.getUsername() + "' }.hostUserId"));
    }

    private Response page() {
        Response response = getRequestSpecification()
                .pathParam("hostId", host.getHostId())
                .params(Constants.PAGE_PARAMS)
                .when()
                .get("/page/{hostId}");

        response.then()
                .body("code", Matchers.equalTo(200))
                .body("rows.hostId", Matchers.hasItem(host.getHostId()));

        return response;
    }

    @Test(dependsOnMethods = "pageTest")
    public void editTest() {
        hostUser.setPassword("");

        getRequestSpecification()
                .contentType(ContentType.JSON)
                .body(hostUser)
                .pathParam("hostUserId", hostUser.getHostUserId())
                .when()
                .put("/{hostUserId}")
                .then()
                .body("code", Matchers.equalTo(500));
    }

    @Test(dependsOnMethods = "editTest")
    public void listAllTest() {
        getRequestSpecification()
                .pathParam("hostId", host.getHostId())
                .when()
                .get("/listAll/{hostId}")
                .then()
                .body("code", Matchers.equalTo(200))
                .body("data.username", Matchers.hasItem(hostUser.getUsername()));
    }

    @Test(dependsOnMethods = "listAllTest")
    public void listAllWithoutRootTest() {
        getRequestSpecification()
                .pathParam("hostId", host.getHostId())
                .when()
                .get("/listAllWithoutRoot/{hostId}")
                .then()
                .body("code", Matchers.equalTo(200))
                .body("data.username", Matchers.hasItem(hostUser.getUsername()));
    }

    @Test(dependsOnMethods = "listAllWithoutRootTest")
    public void delTest() {
        getRequestSpecification()
                .pathParam("hostUserId", hostUser.getHostUserId())
                .when()
                .delete("/{hostUserId}")
                .then()
                .body("code", Matchers.equalTo(200));

        hostControllerTest.deleteHost();
    }
}
