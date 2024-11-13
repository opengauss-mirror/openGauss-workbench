/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2024-2024. All rights reserved.
 */

package org.opengauss.visualtool.api;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.opengauss.visualtool.api.model.SysSetting;
import org.hamcrest.Matchers;
import org.testng.annotations.Test;

import static org.opengauss.global.Constants.getRequestSpecification;

/**
 * SysSettingController test
 *
 * @since 2024/10/21
 */
public class SysSettingControllerTest {
    private static final Logger logger = LogManager.getLogger(SysSettingControllerTest.class);

    private SysSetting sysSetting = new SysSetting();

    @Test
    public void setTestBasePath() {
        RestAssured.basePath = "/system/setting";
        logger.info("SysSettingControllerTest start.");
    }

    @Test(priority = 1)
    public void listTest() {
        Response response = getRequestSpecification()
                .when()
                .get();

        response.then()
                .body("code", Matchers.equalTo(200))
                .body("data.id", Matchers.equalTo(1));

        sysSetting = response.jsonPath().getObject("data", SysSetting.class);
    }

    @Test(priority = 1)
    public void updateTest() {
        getRequestSpecification()
                .contentType(ContentType.JSON)
                .body(sysSetting)
                .when()
                .put()
                .then()
                .body("code", Matchers.equalTo(200));
    }

    @Test(priority = 1)
    public void checkSysUploadPathTest() {
        getRequestSpecification()
                .param("path", sysSetting.getUploadPath())
                .when()
                .get("/checkUploadPath")
                .then()
                .body("code", Matchers.equalTo(200));
    }
}
