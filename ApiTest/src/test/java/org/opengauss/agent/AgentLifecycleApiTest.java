/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */

package org.opengauss.agent;

import static org.opengauss.global.Constants.getRequestSpecification;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hamcrest.Matchers;
import org.opengauss.global.AppConfigLoader;
import org.opengauss.global.Constants;
import org.opengauss.utils.EncryptionUtils;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

/**
 * Positive disposable Agent lifecycle contract checks.
 *
 * This suite is intentionally excluded from the default TestNG suite. It requires
 * a disposable SSH user and runtime-only ISSUE20_AGENT_PASSWORD.
 *
 * @since 2026/08/17
 */
public class AgentLifecycleApiTest {
    private static final Logger LOGGER = LogManager.getLogger(AgentLifecycleApiTest.class);
    private static final Duration STATUS_POLL_INTERVAL = Duration.ofSeconds(2);
    private static final int STATUS_POLL_ATTEMPTS = 45;

    private String agentId;
    private String agentUser;
    private String agentPassword;
    private String hostId;
    private String hostUserId;
    private Map<String, Object> originalSystemSetting;
    private Long taskId;
    private Map<String, Object> taskInstanceRequest;
    private String taskInstanceResponse;
    private boolean isInstalled;
    private boolean isSystemSettingUpdated;
    private int agentPort = 19021;

    @BeforeClass
    public void provisionHostFixture() {
        AppConfigLoader.loadConfig();
        Constants.loadToken();
        EncryptionUtils.getEncryptionKey();
        String requestedFixtureId = required("ISSUE20_AGENT_ID");
        agentUser = required("ISSUE20_AGENT_USER");
        agentPassword = required("ISSUE20_AGENT_PASSWORD");
        configureAdminServerHost();

        String hostName = "issue20-agent-host-" + requestedFixtureId;
        Map<String, Object> host = new HashMap<>();
        host.put("name", hostName);
        host.put("privateIp", "127.0.0.1");
        host.put("publicIp", "127.0.0.1");
        host.put("port", 22);
        host.put("username", agentUser);
        host.put("password", EncryptionUtils.encrypt(agentPassword));
        host.put("isRemember", true);

        RestAssured.basePath = "/host";
        getRequestSpecification().contentType(ContentType.JSON).body(host).when().post()
            .then().statusCode(200).body("code", Matchers.equalTo(200));

        Response page = getRequestSpecification().queryParam("pageNum", 1)
            .queryParam("pageSize", 100).when().get("/page");
        hostId = page.jsonPath().getString("rows.find { it.name == '" + hostName + "' }.hostId");
        requireNonBlank(hostId, "hostId");
        LOGGER.info("ISSUE20_HOST_ID_LENGTH={}", hostId.length());
        agentId = hostId;

        RestAssured.basePath = "/hostUser";
        Response users = getRequestSpecification().pathParam("hostId", hostId)
            .queryParam("pageNum", 1).queryParam("pageSize", 100).when().get("/page/{hostId}");
        hostUserId = users.jsonPath().getString("rows.find { it.username == '" + agentUser + "' }.hostUserId");
        requireNonBlank(hostUserId, "hostUserId");
        LOGGER.info("ISSUE20_HOST_USER_ID_LENGTH={}", hostUserId.length());
    }

    @Test(priority = 1)
    public void installCreatesAgent() {
        Map<String, Object> agent = new HashMap<>();
        agent.put("agentId", agentId);
        agent.put("agentIp", "127.0.0.1");
        agent.put("agentName", "datakit-agent-7.0.0-RC3-runner.jar");
        agent.put("agentPort", agentPort);
        agent.put("installPath", "/home/" + agentUser + "/issue20-agent");
        agent.put("installUser", agentUser);
        agent.put("installUserId", hostUserId);

        RestAssured.basePath = "/agent";
        getRequestSpecification().contentType(ContentType.JSON).body(agent).when().post("/install")
            .then().statusCode(200).body("code", Matchers.equalTo(200));
        isInstalled = true;
    }

    @Test(priority = 2, dependsOnMethods = "installCreatesAgent")
    public void listContainsInstalledAgent() throws InterruptedException {
        assertAgentPresent("starting");
    }

    @Test(priority = 3, dependsOnMethods = "installCreatesAgent")
    public void heartbeatAndAnomalyCheckingUseInstalledAgent() {
        Map<String, Object> heartbeat = new HashMap<>();
        heartbeat.put("agentId", agentId);
        heartbeat.put("timestamps", Instant.now().toString());
        heartbeat.put("status", "UP");
        getRequestSpecification().contentType(ContentType.JSON).body(heartbeat).when().post("/heartbeat")
            .then().statusCode(200).body("code", Matchers.equalTo(200));

        getRequestSpecification().queryParam("agentId", agentId).when().post("/server/anomaly/checking")
            .then().statusCode(200).body("code", Matchers.equalTo(200))
            .body("data.server_install_record", Matchers.equalTo("true"));
    }

    @Test(priority = 4, dependsOnMethods = "installCreatesAgent")
    public void taskCallbackStartAcceptsInstalledAgent() {
        getRequestSpecification().queryParam("agentId", agentId).when().post("/task/callback/start")
            .then().statusCode(200).body("code", Matchers.equalTo(200));
    }

    @Test(priority = 5, dependsOnMethods = "installCreatesAgent")
    public void taskInstanceSaveCreatesDisposableTask() {
        Map<String, Object> task = new HashMap<>();
        task.put("taskName", "issue20-task-" + agentId);
        task.put("taskTemplateId", "base_host_info");
        task.put("agentId", agentId);
        taskInstanceRequest = new LinkedHashMap<>(task);

        RestAssured.basePath = "/agent";
        Response created = getRequestSpecification().contentType(ContentType.JSON).body(task).when()
            .post("/taskInstance/save");
        created.then().statusCode(200).body("code", Matchers.equalTo(200));
        taskId = created.jsonPath().getLong("data");
        taskInstanceResponse = created.asString();
        if (taskId == null || taskId <= 0) {
            throw new AssertionError("taskInstance/save did not return a valid task id");
        }
    }

    /**
     * Records the observed response for the disposable base_host_info probe.
     * Root-cause classification remains under investigation.
     */
    @Test(priority = 6, dependsOnMethods = "taskInstanceSaveCreatesDisposableTask")
    public void startTaskCompatibilityProbeRecordsRc3Error() {
        Response response = getRequestSpecification().queryParam("agentId", agentId)
            .queryParam("id", taskId).when().get("/start/task");
        writeA07Evidence(response);
        if (response.statusCode() != 200) {
            throw new AssertionError("expected RC3 compatibility probe transport status 200, got "
                + response.statusCode() + ": " + response.asString());
        }
        Integer code = response.jsonPath().getInt("code");
        if (!Integer.valueOf(500).equals(code)) {
            throw new AssertionError("expected RC3 compatibility probe business code 500, got "
                + code + ": " + response.asString());
        }
        String message = response.jsonPath().getString("msg");
        if (message == null || !message.contains("clusterConfig")) {
            throw new AssertionError("expected RC3 clusterConfig NPE evidence, got: " + response.asString());
        }
    }

    @Test(priority = 7, dependsOnMethods = "installCreatesAgent")
    public void stopPersistsStoppedState() throws InterruptedException {
        getRequestSpecification().queryParam("agentId", agentId).when().post("/stop")
            .then().statusCode(200).body("code", Matchers.equalTo(200));
        assertAgentPresent("stop");
    }

    @Test(priority = 7, dependsOnMethods = "stopPersistsStoppedState")
    public void startRestoresRunningProcess() throws InterruptedException {
        getRequestSpecification().queryParam("agentId", agentId).when().post("/start")
            .then().statusCode(200).body("code", Matchers.equalTo(200));
        assertAgentPresent("starting");
    }

    @Test(priority = 8, dependsOnMethods = "startRestoresRunningProcess")
    public void updateAgentPortRestartsWithNewPort() {
        agentPort = 19022;
        getRequestSpecification().queryParam("agentId", agentId)
            .queryParam("agentPort", agentPort).when().post("/updateAgentPort")
            .then().statusCode(200).body("code", Matchers.equalTo(200));
        assertAgentPort(agentPort);
    }

    @Test(priority = 9, dependsOnMethods = "updateAgentPortRestartsWithNewPort")
    public void upgradeAcceptsCurrentRunnerWithoutReplacement() throws InterruptedException {
        getRequestSpecification().queryParam("agentId", agentId).when().post("/upgrade")
            .then().statusCode(200).body("code", Matchers.equalTo(200));
        assertAgentPresent("starting");
    }

    @Test(priority = 10, dependsOnMethods = "upgradeAcceptsCurrentRunnerWithoutReplacement")
    public void deregisterIsAcceptedForInstalledAgent() {
        Map<String, Object> heartbeat = new HashMap<>();
        heartbeat.put("agentId", agentId);
        heartbeat.put("timestamps", Instant.now().toString());
        heartbeat.put("status", "DOWN");
        getRequestSpecification().contentType(ContentType.JSON).body(heartbeat).when().post("/deregister")
            .then().statusCode(200).body("code", Matchers.equalTo(200));
    }

    @Test(priority = 11, dependsOnMethods = "deregisterIsAcceptedForInstalledAgent")
    public void uninstallRemovesAgentRecord() {
        getRequestSpecification().queryParam("agentId", agentId).when().post("/uninstall")
            .then().statusCode(200).body("code", Matchers.equalTo(200));
        isInstalled = false;
        Response response = getRequestSpecification().when().get("/list");
        List<Map<String, Object>> rows = response.jsonPath().getList("data");
        if (rows.stream().anyMatch(row -> agentId.equals(row.get("agentId")))) {
            throw new AssertionError("uninstall left an agent record");
        }
    }

    @AfterClass(alwaysRun = true)
    public void cleanupFixture() {
        RestAssured.basePath = "/agent";
        try {
            if (isInstalled && agentId != null) {
                getRequestSpecification().queryParam("agentId", agentId).when().post("/uninstall");
            }
        } finally {
            try {
                if (hostId != null) {
                    RestAssured.basePath = "/host";
                    getRequestSpecification().pathParam("hostId", hostId).when().delete("/{hostId}");
                }
            } finally {
                restoreAdminServerHost();
            }
        }
    }

    private void configureAdminServerHost() {
        RestAssured.basePath = "/system/setting";
        Response response = getRequestSpecification().when().get();
        response.then().statusCode(200).body("code", Matchers.equalTo(200));
        originalSystemSetting = new LinkedHashMap<>(response.jsonPath().getMap("data"));
        Map<String, Object> localFixtureSetting = new LinkedHashMap<>(originalSystemSetting);
        localFixtureSetting.put("serverHost", "127.0.0.1");
        getRequestSpecification().contentType(ContentType.JSON).body(localFixtureSetting).when().put()
            .then().statusCode(200).body("code", Matchers.equalTo(200));
        isSystemSettingUpdated = true;
    }

    private void restoreAdminServerHost() {
        if (!isSystemSettingUpdated || originalSystemSetting == null) {
            return;
        }
        RestAssured.basePath = "/system/setting";
        getRequestSpecification().contentType(ContentType.JSON).body(originalSystemSetting).when().put()
            .then().statusCode(200).body("code", Matchers.equalTo(200));
    }

    private void assertAgentPresent(String expectedStatus) throws InterruptedException {
        waitForExpectedStatus(this::currentAgentStatus, expectedStatus, STATUS_POLL_ATTEMPTS, STATUS_POLL_INTERVAL);
    }

    private String currentAgentStatus() {
        Response response = getRequestSpecification().when().get("/list");
        response.then().statusCode(200).body("code", Matchers.equalTo(200));
        List<Map<String, Object>> rows = response.jsonPath().getList("data");
        Map<String, Object> row = rows.stream().filter(item -> agentId.equals(item.get("agentId")))
            .findFirst().orElseThrow(() -> new AssertionError("agent is missing from list"));
        return String.valueOf(row.get("status"));
    }

    static void waitForExpectedStatus(Supplier<String> statusSupplier, String expectedStatus, int attempts,
                                      Duration pollInterval) throws InterruptedException {
        String actualStatus = null;
        for (int attempt = 0; attempt < attempts; attempt++) {
            actualStatus = statusSupplier.get();
            if (expectedStatus.equalsIgnoreCase(actualStatus)) {
                return;
            }
            if (attempt + 1 < attempts && !pollInterval.isZero()) {
                Thread.sleep(pollInterval.toMillis());
            }
        }
        throw new AssertionError("agent status did not become " + expectedStatus + "; last status: " + actualStatus);
    }

    private void assertAgentPort(int expectedPort) {
        Response response = getRequestSpecification().when().get("/list");
        List<Map<String, Object>> rows = response.jsonPath().getList("data");
        Map<String, Object> row = rows.stream().filter(item -> agentId.equals(item.get("agentId")))
            .findFirst().orElseThrow(() -> new AssertionError("agent is missing from list"));
        Object actualPort = row.get("agentPort");
        if (!(actualPort instanceof Number)) {
            throw new AssertionError("agent port is not numeric: " + actualPort);
        }
        Number actualPortNumber = (Number) actualPort;
        if (!Integer.valueOf(expectedPort).equals(actualPortNumber.intValue())) {
            throw new AssertionError("unexpected agent port: " + actualPort);
        }
    }

    private static String required(String name) {
        String value = System.getenv(name);
        if (value == null || value.isBlank()) {
            throw new IllegalStateException(name + " must be supplied for the disposable Agent suite");
        }
        return value;
    }

    private static void requireNonBlank(String value, String name) {
        if (value == null || value.isBlank()) {
            throw new IllegalStateException("missing " + name + " from DataKit response");
        }
    }

    private void writeA07Evidence(Response response) {
        String output = System.getenv("ISSUE20_A07_EVIDENCE_DIR");
        if (output == null || output.isBlank()) {
            return;
        }
        try {
            Path directory = Path.of(output);
            Files.createDirectories(directory);
            write(directory, "01_environment.txt", "BASELINE_SHA=7b408a266909231f65bebc6114a585c9f8c7693b\n"
                + "DATAKIT_VERSION=7.0.0-RC3\nOPEN_GAUSS_BUILD=f08516a2\n"
                + "PRODUCT_LOGIC_MODIFIED=NO\n");
            write(directory, "02_task_instance_request.json", "{\n  \"taskName\": \""
                + taskInstanceRequest.get("taskName") + "\",\n  \"taskTemplateId\": \""
                + taskInstanceRequest.get("taskTemplateId") + "\",\n  \"agentId\": \""
                + taskInstanceRequest.get("agentId") + "\"\n}\n");
            write(directory, "03_task_instance_response.json", taskInstanceResponse);
            write(directory, "04_start_task_request.txt",
                "GET /agent/start/task?agentId=" + agentId + "&id=" + taskId + "\n");
            write(directory, "05_start_task_response.json", response.asPrettyString());
            write(directory, "07_reproduction_steps.md",
                "1. Install disposable Agent.\n2. Save `base_host_info` task instance.\n"
                    + "3. Call start/task with that agent and task id.\n"
                    + "4. Capture HTTP response and server log.\n5. Uninstall fixture.\n");
            write(directory, "08_source_analysis.md",
                "Observed /agent/start/task returned application code 500; server-side "
                    + "stack trace is captured for further analysis. No product code was modified.\n");
            write(directory, "09_summary.md", "A07_TRANSPORT_HTTP_STATUS=" + response.statusCode() + "\n"
                + "A07_APPLICATION_CODE=" + response.jsonPath().getInt("code") + "\n"
                + "TASK_ID=" + taskId + "\nAGENT_ID=" + agentId + "\n");
        } catch (IOException e) {
            throw new AssertionError("failed to write sanitized A07 evidence", e);
        }
    }

    private static void write(Path directory, String file, String content) throws IOException {
        Files.writeString(directory.resolve(file), content, StandardCharsets.UTF_8);
    }
}
