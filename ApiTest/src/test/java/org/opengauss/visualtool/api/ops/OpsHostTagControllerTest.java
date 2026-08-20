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
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.opengauss.global.Constants.getRequestSpecification;

/**
 * OpsHostTagController test
 *
 * @since 2024/10/31
 */
public class OpsHostTagControllerTest {
    private static final Logger logger = LogManager.getLogger(OpsHostTagControllerTest.class);

    private static final String TAG_NAME = "tag-" + UUID.randomUUID().toString().replace("-", "");
    private static final String CHANGE_TAG_NAME = "tag-" + UUID.randomUUID().toString().replace("-", "");

    private final HostControllerTest hostControllerTest = new HostControllerTest();
    private Host host;
    private String tagId;

    private boolean isHostCreated;
    private boolean isRelationAdded;
    private boolean isRelationDeleted;
    private boolean isTagDeleted;

    @BeforeClass
    public void setUp() {
        host = hostControllerTest.addHost();
        isHostCreated = host != null && host.getHostId() != null;
        RestAssured.basePath = "/hostTag";
        logger.info("OpsHostTagControllerTest start.");
    }

    @Test
    public void addTest() {
        Map<String, String> tag = new HashMap<>();
        tag.put("name", TAG_NAME);

        getRequestSpecification()
                .contentType(ContentType.JSON)
                .body(tag)
                .when()
                .post("/add")
                .then()
                .statusCode(200)
                .body("code", Matchers.equalTo(200));
    }

    @Test(dependsOnMethods = "addTest")
    public void pageTest() {
        Response response = getRequestSpecification()
                .params(Constants.PAGE_PARAMS)
                .param("name", TAG_NAME)
                .when()
                .get("/page");

        response.then()
                .statusCode(200)
                .body("code", Matchers.equalTo(200))
                .body("rows.name", Matchers.hasItem(TAG_NAME));

        tagId = response.jsonPath().getString("rows.find { it.name == '" + TAG_NAME + "' }.id");
    }

    @Test(dependsOnMethods = "pageTest")
    public void updateTest() {
        Map<String, String> tag = new HashMap<>();
        tag.put("name", CHANGE_TAG_NAME);

        getRequestSpecification()
                .contentType(ContentType.JSON)
                .body(tag)
                .pathParam("tagId", tagId)
                .when()
                .put("/update/{tagId}")
                .then()
                .statusCode(200)
                .body("code", Matchers.equalTo(200));
    }

    @Test(dependsOnMethods = "updateTest")
    public void listAllTest() {
        getRequestSpecification()
                .when()
                .get("/listAll")
                .then()
                .statusCode(200)
                .body("code", Matchers.equalTo(200))
                .body("data.name", Matchers.hasItem(CHANGE_TAG_NAME));
    }

    @Test(dependsOnMethods = "listAllTest")
    public void addTagTest() {
        Map<String, Object> hostTagDto = new HashMap<>();
        hostTagDto.put("names", new String[] {CHANGE_TAG_NAME});
        hostTagDto.put("hostIds", new String[] {host.getHostId()});

        getRequestSpecification()
                .contentType(ContentType.JSON)
                .body(hostTagDto)
                .when()
                .put("/addTag")
                .then()
                .statusCode(200)
                .body("code", Matchers.equalTo(200));

        isRelationAdded = true;
    }

    @Test(dependsOnMethods = "addTagTest")
    public void verifyRelationTest() {
        Response response = getRequestSpecification()
                .params(Constants.PAGE_PARAMS)
                .param("name", CHANGE_TAG_NAME)
                .when()
                .get("/page");

        response.then()
                .statusCode(200)
                .body("code", Matchers.equalTo(200))
                .body("rows.find { it.name == '" + CHANGE_TAG_NAME + "' }.relNum", Matchers.equalTo(1));
    }

    @Test(dependsOnMethods = "verifyRelationTest")
    public void delTagRelationTest() {
        Map<String, Object> hostTagDto = new HashMap<>();
        hostTagDto.put("names", new String[] {tagId});
        hostTagDto.put("hostIds", new String[] {host.getHostId()});

        getRequestSpecification()
                .contentType(ContentType.JSON)
                .body(hostTagDto)
                .when()
                .put("/delTag")
                .then()
                .statusCode(200)
                .body("code", Matchers.equalTo(200));

        isRelationDeleted = true;
    }

    @Test(dependsOnMethods = "delTagRelationTest")
    public void delTest() {
        getRequestSpecification()
                .pathParam("tagId", tagId)
                .when()
                .delete("/del/{tagId}")
                .then()
                .statusCode(200)
                .body("code", Matchers.equalTo(200));

        isTagDeleted = true;
    }

    /**
     * Best-effort cleanup so a failed chain does not leave the tag/relation/host created by this test.
     */
    @AfterClass(alwaysRun = true)
    public void cleanUp() {
        if (!isTagDeleted) {
            String id = tagId;
            if (id == null || id.isEmpty()) {
                id = findTagIdByName().orElse(null);
            }
            if (id != null && !id.isEmpty()) {
                if (isRelationAdded && !isRelationDeleted) {
                    delTagRelationQuietly(id);
                }
                delTagQuietly(id);
            }
        }
        if (isHostCreated) {
            deleteHostQuietly();
        }
    }

    private Optional<String> findTagIdByName() {
        Optional<String> id = findTagIdByName(CHANGE_TAG_NAME);
        if (id.isEmpty()) {
            id = findTagIdByName(TAG_NAME);
        }
        return id;
    }

    private Optional<String> findTagIdByName(String name) {
        try {
            RestAssured.basePath = "/hostTag";
            Response response = getRequestSpecification()
                    .params(Constants.PAGE_PARAMS)
                    .param("name", name)
                    .when()
                    .get("/page");
            return Optional.ofNullable(response.jsonPath()
                    .getString("rows.find { it.name == '" + name + "' }.id"));
        } catch (RuntimeException | AssertionError e) {
            return Optional.empty();
        }
    }

    private void delTagRelationQuietly(String id) {
        try {
            RestAssured.basePath = "/hostTag";
            Map<String, Object> hostTagDto = new HashMap<>();
            hostTagDto.put("names", new String[] {id});
            hostTagDto.put("hostIds", new String[] {host.getHostId()});
            getRequestSpecification()
                    .contentType(ContentType.JSON)
                    .body(hostTagDto)
                    .when()
                    .put("/delTag");
        } catch (Exception | AssertionError e) {
            logger.warn("Failed to clean up host tag relation {}: {}", id, e.getMessage());
        }
    }

    private void delTagQuietly(String id) {
        try {
            RestAssured.basePath = "/hostTag";
            getRequestSpecification()
                    .pathParam("tagId", id)
                    .when()
                    .delete("/del/{tagId}");
        } catch (Exception | AssertionError e) {
            logger.warn("Failed to clean up host tag {}: {}", id, e.getMessage());
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
