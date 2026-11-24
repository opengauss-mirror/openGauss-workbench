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

package org.opengauss.plugins.system;

import static org.opengauss.global.Constants.getRequestSpecification;

import io.restassured.RestAssured;

import org.hamcrest.Matchers;
import org.opengauss.global.AppConfigLoader;
import org.opengauss.global.Constants;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Explicit semantic checks for the non-destructive Issue #20 plugin APIs.
 *
 * @since 2026/08/17
 */
public class PluginReadOnlyApiTest {
    private static final String UNKNOWN_PLUGIN_ID = "issue20-plugin-does-not-exist";

    @BeforeClass
    public void setPluginBasePath() {
        AppConfigLoader.loadConfig();
        Constants.loadToken();
        RestAssured.basePath = "/system/plugins";
    }

    @Test
    public void unknownPluginReturnsSuccessfulNullPayload() {
        getRequestSpecification()
            .pathParam("id", UNKNOWN_PLUGIN_ID)
            .when()
            .post("/get/{id}")
            .then()
            .statusCode(200)
            .body("code", Matchers.equalTo(200))
            .body("data", Matchers.nullValue());
    }

    @Test
    public void baseOpsStartReturnsBooleanState() {
        getRequestSpecification()
            .when()
            .get("/isBaseOpsStart")
            .then()
            .statusCode(200)
            .body("code", Matchers.equalTo(200))
            .body("data", Matchers.anyOf(Matchers.equalTo(true), Matchers.equalTo(false)));
    }

    @Test
    public void unloadPluginsInfoReturnsListPayload() {
        getRequestSpecification()
            .when()
            .get("/unloadPluginsInfo")
            .then()
            .statusCode(200)
            .body("code", Matchers.equalTo(200))
            .body("data", Matchers.instanceOf(java.util.List.class));
    }

    @Test
    public void unknownPluginUrlReturnsApplicationError() {
        getRequestSpecification()
            .queryParam("pluginId", UNKNOWN_PLUGIN_ID)
            .when()
            .post("/getUnloadPluginUrl")
            .then()
            .statusCode(200)
            .body("code", Matchers.not(Matchers.equalTo(200)))
            .body("msg", Matchers.notNullValue());
    }
}
