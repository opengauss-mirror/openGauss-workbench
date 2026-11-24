/*
 * Copyright (c) 2026 Huawei Technologies Co., Ltd.
 * openGauss-workbench is licensed under Mulan PSL v2.
 * You can use this software according to the terms and conditions of the Mulan PSL v2.
 * You may obtain a copy of Mulan PSL v2 at:
 *          http://license.coscl.org.cn/MulanPSL2
 * THIS SOFTWARE IS PROVIDED ON AN "AS IS" BASIS, WITHOUT WARRANTIES OF ANY KIND,
 * EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO NON-INFRINGEMENT,
 * MERCHANTABILITY OR FIT FOR A PARTICULAR PURPOSE.
 * See the Mulan PSL v2 for more details.
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

import java.util.HashMap;
import java.util.Map;

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

    @Test(dependsOnMethods = "listTest")
    public void updateTest() {
        getRequestSpecification()
                .contentType(ContentType.JSON)
                .body(sysSetting)
                .when()
                .put()
                .then()
                .body("code", Matchers.equalTo(200));
    }

    @Test(dependsOnMethods = "listTest")
    public void checkSysUploadPathTest() {
        getRequestSpecification()
                .param("path", sysSetting.getUploadPath())
                .when()
                .get("/checkUploadPath")
                .then()
                .body("code", Matchers.equalTo(200));
    }

    @Test(priority = 1)
    public void updateWithMissingRequiredFieldTest() {
        Map<String, Object> invalidSetting = new HashMap<>();
        invalidSetting.put("uploadPath", "/home/datakit/upload");

        getRequestSpecification()
                .contentType(ContentType.JSON)
                .body(invalidSetting)
                .when()
                .put()
                .then()
                .body("code", Matchers.equalTo(400));
    }
}
