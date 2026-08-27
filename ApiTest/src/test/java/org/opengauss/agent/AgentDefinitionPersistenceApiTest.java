/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */

package org.opengauss.agent;

import static org.opengauss.global.Constants.getRequestSpecification;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;

import org.hamcrest.Matchers;
import org.opengauss.global.AppConfigLoader;
import org.opengauss.global.Constants;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Non-empty Agent definition persistence checks. The disposable harness owns
 * the stop/JDBC-delete/restart finally path because embedded IntarkDB cannot
 * be opened by a second process while DataKit is running.
 *
 * @since 2026/08/17
 */
public class AgentDefinitionPersistenceApiTest {
    private String metricsName;
    private long schemaId;
    private String schemaName;
    private String templateName;

    @BeforeClass
    public void initializeRuntimeAndKeys() throws IOException {
        AppConfigLoader.loadConfig();
        Constants.loadToken();
        RestAssured.basePath = "/agent";
        String suffix = Long.toUnsignedString(Instant.now().toEpochMilli(), 36);
        metricsName = "issue20_metrics_" + suffix;
        schemaName = "issue20_schema_" + suffix;
        templateName = "issue20_template_" + suffix;
        schemaId = 900000000L + Math.floorMod(Instant.now().toEpochMilli(), 9999999L);
        String keyFile = System.getenv("ISSUE20_DEFINITION_KEYS_FILE");
        if (keyFile == null || keyFile.isBlank()) {
            throw new IllegalStateException("ISSUE20_DEFINITION_KEYS_FILE is required");
        }
        Files.writeString(Path.of(keyFile), String.join("\n",
            "METRICS_NAME=" + metricsName,
            "SCHEMA_ID=" + schemaId,
            "TEMPLATE_NAME=" + templateName) + "\n");
    }

    @Test
    public void saveNonEmptyMetricsAndVerifyFields() {
        Map<String, Object> body = new HashMap<>();
        body.put("name", metricsName);
        body.put("description", "Issue 20 disposable metric");
        body.put("fieldName", "issue20Field");
        body.put("unit", "count");
        body.put("dataType", "long");
        body.put("prop", "issue20");
        body.put("collectCmd", "select 20");
        save("/taskMetricsDefinition/save", List.of(body));
        Map<String, Object> row = findBy("/taskMetricsDefinition/list", "name", metricsName);
        assertField(row, "description", "Issue 20 disposable metric");
        assertField(row, "fieldName", "issue20Field");
    }

    @Test(dependsOnMethods = "saveNonEmptyMetricsAndVerifyFields")
    public void saveNonEmptySchemaAndVerifyFields() {
        Map<String, Object> body = new HashMap<>();
        body.put("id", schemaId);
        body.put("name", schemaName);
        body.put("metric", metricsName);
        save("/taskSchemaDefinition/save", List.of(body));
        Map<String, Object> row = findBy("/taskSchemaDefinition/list", "id", schemaId);
        assertField(row, "name", schemaName);
        assertField(row, "metric", metricsName);
    }

    @Test(dependsOnMethods = "saveNonEmptySchemaAndVerifyFields")
    public void saveNonEmptyTemplateAndVerifyFields() {
        Map<String, Object> body = new HashMap<>();
        body.put("name", templateName);
        body.put("type", "OSHI_FIXED_METRIC");
        body.put("groupTag", "issue20");
        body.put("pluginsTag", "base-ops");
        body.put("operateObjType", "OSHI");
        body.put("period", "PT0S");
        body.put("collectMetric", metricsName);
        body.put("storagePolicy", "CUSTOM");
        body.put("receiveApi", "/receive/issue20");
        save("/taskTemplateDefinition/save", List.of(body));
        Map<String, Object> row = findBy("/taskTemplateDefinition/list", "name", templateName);
        assertField(row, "groupTag", "issue20");
        assertField(row, "collectMetric", metricsName);
    }

    private void save(String endpoint, Object body) {
        getRequestSpecification().contentType(ContentType.JSON).body(body).when().post(endpoint)
            .then().statusCode(200).body("code", Matchers.equalTo(200));
    }

    private Map<String, Object> findBy(String endpoint, String key, Object expected) {
        Response response = getRequestSpecification().when().get(endpoint);
        response.then().statusCode(200).body("code", Matchers.equalTo(200));
        List<Map<String, Object>> rows = response.jsonPath().getList("data");
        return rows.stream().filter(row -> expected.toString().equals(String.valueOf(row.get(key)))).findFirst()
            .orElseThrow(() -> new AssertionError("missing disposable definition " + key + "=" + expected));
    }

    private void assertField(Map<String, Object> row, String key, Object expected) {
        if (!expected.toString().equals(String.valueOf(row.get(key)))) {
            throw new AssertionError("unexpected " + key + ": " + row.get(key));
        }
    }
}
