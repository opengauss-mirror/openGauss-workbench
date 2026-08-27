/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */

package org.opengauss.agent;

import static org.opengauss.global.Constants.getRequestSpecification;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;

import org.hamcrest.Matchers;
import org.opengauss.global.AppConfigLoader;
import org.opengauss.global.Constants;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.time.Instant;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Non-destructive Agent API contract checks that do not require an SSH fixture.
 *
 * @since 2026/08/17
 */
public class AgentReadOnlyApiTest {
    private static final String UNKNOWN_AGENT_ID = "issue20-agent-does-not-exist";

    @BeforeClass
    public void setAgentBasePath() {
        AppConfigLoader.loadConfig();
        Constants.loadToken();
        RestAssured.basePath = "/agent";
    }

    @Test
    public void listReturnsListPayload() {
        getRequestSpecification().when().get("/list")
            .then().statusCode(200).body("code", Matchers.equalTo(200))
            .body("data", Matchers.instanceOf(List.class));
    }

    @Test
    public void anomalyCheckingUnknownAgentReturnsNoInstallRecord() {
        getRequestSpecification().queryParam("agentId", UNKNOWN_AGENT_ID).when()
            .post("/server/anomaly/checking")
            .then().statusCode(200).body("code", Matchers.equalTo(200))
            .body("data.server_install_record", Matchers.equalTo("false"));
    }

    @Test
    public void heartbeatForUnknownAgentIsAcceptedWithoutCreatingFixture() {
        Map<String, Object> heartbeat = new HashMap<>();
        heartbeat.put("agentId", UNKNOWN_AGENT_ID);
        heartbeat.put("timestamps", Instant.now().toString());
        heartbeat.put("status", "UP");
        getRequestSpecification().contentType(ContentType.JSON).body(heartbeat).when()
            .post("/heartbeat")
            .then().statusCode(200).body("code", Matchers.equalTo(200));
    }

    @Test
    public void taskListReturnsAgentTaskMapPayload() {
        getRequestSpecification().when().get("/task/list")
            .then().statusCode(200).body("code", Matchers.equalTo(200))
            .body("data", Matchers.instanceOf(Map.class));
    }

    @Test
    public void taskDefinitionListsReturnListPayloads() {
        for (String endpoint : new String[]{
            "/taskMetricsDefinition/list",
            "/taskSchemaDefinition/list",
            "/taskTemplateDefinition/list"}) {
            getRequestSpecification().when().get(endpoint)
                .then().statusCode(200).body("code", Matchers.equalTo(200))
                .body("data", Matchers.instanceOf(List.class));
        }
    }

    @Test
    public void lifecycleActionsRejectUnknownAgentWithoutRemoteSideEffect() {
        for (String endpoint : new String[]{"/start", "/stop", "/uninstall", "/upgrade"}) {
            getRequestSpecification().queryParam("agentId", UNKNOWN_AGENT_ID).when()
                .post(endpoint)
                .then().statusCode(200).body("code", Matchers.not(Matchers.equalTo(200)));
        }
    }

    @Test
    public void updatePortRejectsUnknownAgentWithoutRemoteSideEffect() {
        getRequestSpecification().queryParam("agentId", UNKNOWN_AGENT_ID)
            .queryParam("agentPort", "19021").when().post("/updateAgentPort")
            .then().statusCode(200).body("code", Matchers.not(Matchers.equalTo(200)));
    }

    @Test
    public void emptyDefinitionBatchesAreNoOpSuccess() {
        for (String endpoint : new String[]{
            "/taskMetricsDefinition/save",
            "/taskSchemaDefinition/save",
            "/taskTemplateDefinition/save"}) {
            getRequestSpecification().contentType(ContentType.JSON)
                .body(Collections.emptyList()).when().post(endpoint)
                .then().statusCode(200).body("code", Matchers.equalTo(200));
        }
    }
}
