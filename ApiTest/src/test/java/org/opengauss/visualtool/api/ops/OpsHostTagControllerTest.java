/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2024-2024. All rights reserved.
 */

package org.opengauss.visualtool.api.ops;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.opengauss.global.Constants;
import org.hamcrest.Matchers;
import org.opengauss.visualtool.api.model.ops.Host;
import org.testng.annotations.Test;

import java.util.HashMap;
import java.util.Map;

import static org.opengauss.global.Constants.getRequestSpecification;

/**
 * OpsHostTagController test
 *
 * @since 2024/10/31
 */
public class OpsHostTagControllerTest {
    private static final Logger logger = LogManager.getLogger(OpsHostTagControllerTest.class);

    private final HostControllerTest hostControllerTest = new HostControllerTest();
    private Host host;
    private Map<String, String> tag = new HashMap<>();
    private Map<String, Object> hostTagDto = new HashMap<>();
    private final String tagName = "testTag";
    private final String changeTagName = "changeTag";

    @Test
    public void setTestBasePath() {
        host = hostControllerTest.addHost();

        RestAssured.basePath = "/hostTag";
        logger.info("OpsHostTagControllerTest start.");
    }

    @Test(dependsOnMethods = "setTestBasePath")
    public void addTest() {
        tag.put("name", tagName);

        getRequestSpecification()
                .contentType(ContentType.JSON)
                .body(tag)
                .when()
                .post("/add")
                .then()
                .body("code", Matchers.equalTo(200));
    }

    @Test(dependsOnMethods = "addTest")
    public void pageTest() {
        Response response = getRequestSpecification()
                .params(Constants.PAGE_PARAMS)
                .when()
                .get("/page");

        response.then()
                .body("code", Matchers.equalTo(200))
                .body("rows.name", Matchers.hasItem(tagName));

        tag.put("tagId", response.jsonPath().getString("rows.find { it.name == '" + tagName + "' }.id"));
    }

    @Test(dependsOnMethods = "pageTest")
    public void updateTest() {
        tag.put("name", changeTagName);

        getRequestSpecification()
                .contentType(ContentType.JSON)
                .body(tag)
                .pathParam("tagId", tag.get("tagId"))
                .when()
                .put("/update/{tagId}")
                .then()
                .body("code", Matchers.equalTo(200));
    }

    @Test(dependsOnMethods = "updateTest")
    public void listAllTest() {
        getRequestSpecification()
                .when()
                .get("/listAll")
                .then()
                .body("code", Matchers.equalTo(200))
                .body("data.name", Matchers.hasItem(changeTagName));
    }

    @Test(dependsOnMethods = "listAllTest")
    public void addTagTest() {
        hostTagDto.put("names", new String[] {tag.get("name")});
        hostTagDto.put("hostIds", new String[] {host.getHostId()});

        getRequestSpecification()
                .contentType(ContentType.JSON)
                .body(hostTagDto)
                .when()
                .put("/addTag")
                .then()
                .body("code", Matchers.equalTo(200));
    }

    @Test(dependsOnMethods = "addTagTest")
    public void delTagRelationTest() {
        hostTagDto.put("names", new String[] {tag.get("tagId")});

        getRequestSpecification()
                .contentType(ContentType.JSON)
                .body(hostTagDto)
                .when()
                .put("/delTag")
                .then()
                .body("code", Matchers.equalTo(200));
    }

    @Test(dependsOnMethods = "listAllTest")
    public void delTest() {
        getRequestSpecification()
                .pathParam("tagId", tag.get("tagId"))
                .when()
                .delete("/del/{tagId}")
                .then()
                .body("code", Matchers.equalTo(200));

        hostControllerTest.deleteHost();
    }
}
