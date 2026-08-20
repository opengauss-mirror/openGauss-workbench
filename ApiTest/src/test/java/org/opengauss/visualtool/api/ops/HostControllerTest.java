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
import org.opengauss.exception.ApiTestException;
import org.opengauss.global.AppConfigLoader;
import org.opengauss.global.Constants;
import org.opengauss.utils.EncryptionUtils;
import org.opengauss.utils.ExcelUtils;
import org.opengauss.utils.FileUtils;
import org.opengauss.visualtool.api.model.ops.Host;
import org.opengauss.visualtool.api.model.ops.SSHBody;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.WebSocket;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.time.Duration;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletionException;
import java.util.concurrent.TimeUnit;

import static org.opengauss.global.Constants.getRequestSpecification;

/**
 * HostController test
 *
 * @since 2024/10/30
 */
public class HostControllerTest {
    private static final Logger logger = LogManager.getLogger(HostControllerTest.class);

    private static final String BASE_PATH = "/host";
    private static final String CURRENT_LOCALE = "zh-CN";
    private static final String HOST_NAME_PREFIX = "host-";
    private static final String WEBSOCKET_TYPE = "ops";
    private static final long WEBSOCKET_CONNECT_TIMEOUT_SECONDS = 10L;
    private static final long WEBSOCKET_CLOSE_TIMEOUT_SECONDS = 5L;
    private static final int IMPORT_POLL_MAX_ATTEMPTS = 30;
    private static final int IMPORT_ERROR_POLL_MAX_ATTEMPTS = 60;
    private static final long IMPORT_POLL_INTERVAL_MILLIS = 1000L;
    private static final String EXCEL_CONTENT_TYPE =
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet;charset=utf-8";
    private static final String TEMPLATE_FILE_PATH = "src/test/resources/temp/模板.xlsx";
    private static final SecureRandom RANDOM = new SecureRandom();
    private static final String DISABLE_HOSTNAME_VERIFICATION_PROPERTY =
            "jdk.internal.httpclient.disableHostnameVerification";

    static {
        // Scoped to this ApiTest JVM only: local self-signed integration-test certificates may
        // not match the configured server address, so disable JDK HttpClient hostname verification.
        System.setProperty(DISABLE_HOSTNAME_VERIFICATION_PROPERTY, "true");
    }

    private Host host;
    private String hostName;
    private String hostIp;
    private String hostRemark;
    private String uuid;
    private final File xlsxFile = new File(TEMPLATE_FILE_PATH);

    private boolean isDeletedByTest;
    private boolean isImportedHostDeleted;

    /**
     * Initializes the test environment once per class: loads the RSA public key
     * (needed to encrypt host credentials) and pins the base path so every request
     * in this class targets /host deterministically.
     */
    @BeforeClass
    public void setUp() {
        initializeEnvironment();
        generateHostBody();
        logger.info("HostControllerTest start.");
    }

    private void initializeEnvironment() {
        EncryptionUtils.getEncryptionKey();
        RestAssured.basePath = BASE_PATH;
    }

    private void generateHostBody() {
        hostName = HOST_NAME_PREFIX + UUID.randomUUID().toString().replace("-", "");
        hostIp = generateLoopbackIp();

        host = Host.builder()
                .name(hostName)
                .privateIp(hostIp)
                .publicIp(hostIp)
                .port(AppConfigLoader.getAppConfig().getOpsHost().getPort())
                .username("root")
                .password(EncryptionUtils.encrypt(AppConfigLoader.getAppConfig().getOpsHost().getPassword()))
                .isRemember(true)
                .build();
    }

    private String generateLoopbackIp() {
        return "127."
                + (RANDOM.nextInt(254) + 1) + "."
                + (RANDOM.nextInt(254) + 1) + "."
                + (RANDOM.nextInt(254) + 1);
    }

    @Test(priority = 1)
    public void pingTest() {
        getRequestSpecification()
                .contentType(ContentType.JSON)
                .body(host)
                .when()
                .post("/ping")
                .then()
                .statusCode(200)
                .body("code", Matchers.equalTo(200));
    }

    @Test(dependsOnMethods = "pingTest")
    public void addTest() {
        add();
    }

    private void add() {
        getRequestSpecification()
                .contentType(ContentType.JSON)
                .body(host)
                .when()
                .post()
                .then()
                .statusCode(200)
                .body("code", Matchers.equalTo(200));
    }

    @Test(dependsOnMethods = "addTest")
    public void pageTest() {
        page();
    }

    private void page() {
        Response response = getRequestSpecification()
                .params(Constants.PAGE_PARAMS)
                .param("name", hostIp)
                .when()
                .get("/page");

        response.then()
                .statusCode(200)
                .body("code", Matchers.equalTo(200))
                .body("rows.name", Matchers.hasItem(hostName));

        host.setHostId(response.jsonPath().getString("rows.find { it.name == '" + hostName + "' }.hostId"));
    }

    @Test(dependsOnMethods = "pageTest")
    public void pingHostIdTest() {
        getRequestSpecification()
                .pathParam("hostId", host.getHostId())
                .when()
                .get("/ping/{hostId}")
                .then()
                .statusCode(200)
                .body("code", Matchers.equalTo(200));
    }

    @Test(dependsOnMethods = "pingHostIdTest")
    public void editTest() {
        hostRemark = hostName + " host remark";
        host.setRemark(hostRemark);

        getRequestSpecification()
                .contentType(ContentType.JSON)
                .body(host)
                .pathParam("hostId", host.getHostId())
                .when()
                .put("/{hostId}")
                .then()
                .statusCode(200)
                .body("code", Matchers.equalTo(200));
    }

    @Test(dependsOnMethods = "editTest")
    public void listAllTest() {
        getRequestSpecification()
                .when()
                .get("/listAll")
                .then()
                .statusCode(200)
                .body("code", Matchers.equalTo(200))
                .body("data.remark", Matchers.hasItem(hostRemark));
    }

    @Test(dependsOnMethods = "listAllTest")
    public void sshTest() {
        String businessId = newBusinessId("ssh");
        WebSocket webSocket = openWebSocket(businessId);
        try {
            SSHBody sshBody = buildSshBody(businessId);

            getRequestSpecification()
                    .contentType(ContentType.JSON)
                    .body(sshBody)
                    .when()
                    .post("/ssh")
                    .then()
                    .statusCode(200)
                    .body("code", Matchers.equalTo(200));
        } finally {
            closeWebSocket(webSocket);
        }
    }

    @Test(dependsOnMethods = "sshTest")
    public void sshHostIdTest() {
        String businessId = newBusinessId("sshHostId");
        WebSocket webSocket = openWebSocket(businessId);
        try {
            SSHBody sshBody = buildSshBody(businessId);

            getRequestSpecification()
                    .contentType(ContentType.JSON)
                    .pathParam("hostId", host.getHostId())
                    .body(sshBody)
                    .when()
                    .post("/ssh/{hostId}")
                    .then()
                    .statusCode(200)
                    .body("code", Matchers.equalTo(200));
        } finally {
            closeWebSocket(webSocket);
        }
    }

    @Test(dependsOnMethods = "sshHostIdTest")
    public void monitorTest() {
        String businessId = newBusinessId("monitor");
        WebSocket webSocket = openWebSocket(businessId);
        try {
            getRequestSpecification()
                    .contentType(ContentType.JSON)
                    .queryParam("businessId", businessId)
                    .body(Collections.singletonList(host.getHostId()))
                    .when()
                    .post("/monitor")
                    .then()
                    .statusCode(200)
                    .body("code", Matchers.equalTo(200))
                    .body("data.res", Matchers.equalTo(true));
        } finally {
            closeWebSocket(webSocket);
        }
    }

    @Test(dependsOnMethods = "monitorTest")
    public void deleteTest() {
        delete();
        isDeletedByTest = true;
    }

    private void delete() {
        getRequestSpecification()
                .pathParam("hostId", host.getHostId())
                .when()
                .delete("/{hostId}")
                .then()
                .statusCode(200)
                .body("code", Matchers.equalTo(200));
    }

    private SSHBody buildSshBody(String businessId) {
        return SSHBody.builder()
                .ip(host.getPublicIp())
                .sshPort(host.getPort())
                .sshUsername(host.getUsername())
                .sshPassword(host.getPassword())
                .businessId(businessId)
                .build();
    }

    private String newBusinessId(String purpose) {
        return purpose + "-" + UUID.randomUUID().toString().replace("-", "");
    }

    private WebSocket openWebSocket(String businessId) {
        String baseUri = RestAssured.baseURI;
        if (baseUri == null || baseUri.isEmpty()) {
            throw new ApiTestException(
                    "RestAssured.baseURI is not configured; call AppConfigLoader.loadConfig() first.");
        }
        URI base = URI.create(baseUri);
        URI uri = URI.create(toWebSocketScheme(base.getScheme()) + "://" + base.getAuthority()
                + normalizePathPrefix(base.getPath()) + "/websocket/" + WEBSOCKET_TYPE + "/" + businessId);
        HttpClient client = HttpClient.newBuilder()
                .sslContext(buildTrustAllSslContext())
                .connectTimeout(Duration.ofSeconds(WEBSOCKET_CONNECT_TIMEOUT_SECONDS))
                .build();
        try {
            return client.newWebSocketBuilder()
                    .connectTimeout(Duration.ofSeconds(WEBSOCKET_CONNECT_TIMEOUT_SECONDS))
                    .buildAsync(uri, new WebSocket.Listener() {
                    })
                    .join();
        } catch (CompletionException e) {
            throw new ApiTestException("Failed to open websocket session for businessId " + businessId, e);
        }
    }

    private String toWebSocketScheme(String scheme) {
        if ("https".equalsIgnoreCase(scheme)) {
            return "wss";
        }
        if ("http".equalsIgnoreCase(scheme)) {
            return "ws";
        }
        return scheme;
    }

    private String normalizePathPrefix(String path) {
        if (path == null || path.isEmpty() || "/".equals(path)) {
            return "";
        }
        String normalized = path.endsWith("/") ? path.substring(0, path.length() - 1) : path;
        return normalized.startsWith("/") ? normalized : "/" + normalized;
    }

    private void closeWebSocket(WebSocket webSocket) {
        if (webSocket == null) {
            return;
        }
        try {
            webSocket.sendClose(WebSocket.NORMAL_CLOSURE, "done")
                    .orTimeout(WEBSOCKET_CLOSE_TIMEOUT_SECONDS, TimeUnit.SECONDS)
                    .join();
        } catch (CompletionException e) {
            webSocket.abort();
            logger.warn("Failed to close websocket session: {}", e.getMessage());
        }
    }

    private SSLContext buildTrustAllSslContext() {
        try {
            TrustManager[] trustAllCerts = new TrustManager[] {new X509TrustManager() {
                @Override
                public void checkClientTrusted(X509Certificate[] chain, String authType) {
                }

                @Override
                public void checkServerTrusted(X509Certificate[] chain, String authType) {
                }

                @Override
                public X509Certificate[] getAcceptedIssuers() {
                    return new X509Certificate[0];
                }
            }};
            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null, trustAllCerts, new SecureRandom());
            return sslContext;
        } catch (GeneralSecurityException e) {
            logger.error("Failed to build trust-all SSL context.", e);
            throw new ApiTestException("Failed to build trust-all SSL context.", e);
        }
    }

    @Test(dependsOnMethods = "deleteTest")
    public void downloadTemplateTest() {
        downloadTemplate();
    }

    private void downloadTemplate() {
        if (xlsxFile.exists()) {
            FileUtils.deleteFile(xlsxFile.getPath());
        }

        Response response = getRequestSpecification()
                .pathParam("currentLocale", CURRENT_LOCALE)
                .when()
                .get("/downloadTemplate/{currentLocale}");

        response.then()
                .statusCode(200)
                .contentType(EXCEL_CONTENT_TYPE)
                .header("Content-Length", Matchers.not(Matchers.equalTo("0")));

        try (InputStream inputStream = response.asInputStream()) {
            FileUtils.createParentDirectoryIfNotExists(xlsxFile.getPath());
            FileUtils.writeToFile(inputStream, xlsxFile.getPath());
        } catch (IOException e) {
            throw new ApiTestException("Write 模板.xlsx failed. ", e);
        }
    }

    @Test(dependsOnMethods = "downloadTemplateTest")
    public void uploadTest() {
        List<Object> hostRecord = getHostRecord();
        ExcelUtils.addRecordToExcel(xlsxFile.getPath(), hostRecord);

        upload();
    }

    private List<Object> getHostRecord() {
        return Arrays.asList(
                1,
                hostName,
                hostIp,
                hostIp,
                AppConfigLoader.getAppConfig().getOpsHost().getPort(),
                "root",
                AppConfigLoader.getAppConfig().getOpsHost().getPassword(),
                "是"
        );
    }

    private void upload() {
        Response response = getRequestSpecification()
                .multiPart("file", xlsxFile)
                .when()
                .post("/upload");

        response.then()
                .statusCode(200)
                .body("code", Matchers.equalTo(200))
                .body("msg", Matchers.notNullValue());

        uuid = response.jsonPath().getString("msg");
    }

    @Test(dependsOnMethods = "uploadTest")
    public void invokeFileTest() {
        invokeFile(0);
    }

    private void invokeFile(int isInvoke) {
        getRequestSpecification()
                .param("uuid", uuid)
                .param("isInvoke", isInvoke)
                .param("currentLocale", CURRENT_LOCALE)
                .when()
                .post("/invokeFile")
                .then()
                .statusCode(200)
                .body("code", Matchers.equalTo(200));
    }

    @Test(dependsOnMethods = "invokeFileTest")
    public void getImportPlanByUuidTest() {
        waitForImportResult(uuid, false);

        invokeFile(1);
        page();
        delete();
        isImportedHostDeleted = true;
    }

    private ImportProgress waitForImportResult(String uuid, boolean shouldExpectError) {
        int maxAttempts = shouldExpectError ? IMPORT_ERROR_POLL_MAX_ATTEMPTS : IMPORT_POLL_MAX_ATTEMPTS;
        ImportProgress lastProgress = null;
        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            Response response = getImportPlanByUuid(uuid);
            lastProgress = ImportProgress.from(response);
            if (lastProgress.isEnd()) {
                if (shouldExpectError && lastProgress.errorSum() == 1) {
                    return lastProgress;
                }
                if (!shouldExpectError && lastProgress.successSum() == 1) {
                    return lastProgress;
                }
                if (shouldExpectError && lastProgress.successSum() == 1) {
                    throw new ApiTestException("An unexpected host import success occurred. uuid=" + uuid
                            + ", " + lastProgress);
                }
                if (!shouldExpectError && lastProgress.errorSum() == 1) {
                    throw new ApiTestException("Failed to import host. uuid=" + uuid + ", " + lastProgress);
                }
            }
            if (attempt == maxAttempts) {
                throw new ApiTestException("Importing host timed out. uuid=" + uuid + ", last=" + lastProgress);
            }
            sleepForImportPoll();
        }
        throw new ApiTestException(
                "Host import did not reach a terminal state. uuid=" + uuid + ", last=" + lastProgress);
    }

    private void sleepForImportPoll() {
        try {
            TimeUnit.MILLISECONDS.sleep(IMPORT_POLL_INTERVAL_MILLIS);
        } catch (InterruptedException e) {
            throw new ApiTestException("Interrupted while waiting for host import.", e);
        }
    }

    private Response getImportPlanByUuid(String uuid) {
        Response response = getRequestSpecification()
                .param("uuid", uuid)
                .when()
                .get("/get_import_plan");

        response.then()
                .statusCode(200)
                .body("code", Matchers.equalTo(200));

        return response;
    }

    @Test(dependsOnMethods = "getImportPlanByUuidTest")
    public void downloadErrorExcelTest() {
        downloadTemplate();

        List<Object> hostRecord = getHostRecord();
        hostRecord.set(6, "");
        ExcelUtils.addRecordToExcel(xlsxFile.getPath(), hostRecord);

        upload();
        invokeFile(0);

        waitForImportResult(uuid, true);

        Response response = getRequestSpecification()
                .pathParam("uuid", uuid)
                .when()
                .get("/downloadErrorExcel/{uuid}");

        response.then()
                .statusCode(200)
                .contentType(EXCEL_CONTENT_TYPE);

        try (InputStream inputStream = response.asInputStream()) {
            byte[] buffer = new byte[256];
            int read = inputStream.read(buffer);
            if (read <= 0) {
                throw new ApiTestException("Downloaded error Excel is empty. uuid=" + uuid);
            }
        } catch (IOException e) {
            throw new ApiTestException("Read error Excel failed. uuid=" + uuid, e);
        }

        invokeFile(1);
    }

    @Test(priority = 1)
    public void listSupportOsNameTest() {
        getRequestSpecification()
                .when()
                .get("/listSupportOsName")
                .then()
                .statusCode(200)
                .body("code", Matchers.equalTo(200));
    }

    /**
     * Adds an ops host for use as a fixture by other test classes. This instance is
     * created manually (not managed by TestNG), so it must initialize its own
     * environment before building and persisting the host body.
     *
     * @return the persisted ops host with its hostId populated
     */
    public Host addHost() {
        initializeEnvironment();
        generateHostBody();
        add();
        page();
        return host;
    }

    /**
     * Deletes the ops host previously created by {@link #addHost()}.
     */
    public void deleteHost() {
        RestAssured.basePath = BASE_PATH;
        delete();
    }

    /**
     * Best-effort cleanup so a failed chain never leaves the unique test host or the
     * temporary Excel file behind. Environment-owned hosts are untouched because only
     * the collision-resistant host name created by this test is targeted.
     */
    @AfterClass(alwaysRun = true)
    public void cleanUp() {
        cleanupHost();
        cleanupTempFile();
    }

    private void cleanupHost() {
        if (isDeletedByTest && isImportedHostDeleted) {
            return;
        }
        if (hostName == null || hostName.isEmpty()) {
            return;
        }
        try {
            RestAssured.basePath = BASE_PATH;
            Response response = getRequestSpecification()
                    .params(Constants.PAGE_PARAMS)
                    .param("name", hostIp)
                    .when()
                    .get("/page");
            List<String> hostIds = response.jsonPath()
                    .getList("rows.findAll { it.name == '" + hostName + "' }.hostId");
            if (hostIds == null || hostIds.isEmpty()) {
                return;
            }
            for (String hostId : hostIds) {
                deleteHostQuietly(hostId);
            }
        } catch (Exception | AssertionError e) {
            logger.warn("Failed to clean up host {}: {}", hostName, e.getMessage());
        }
    }

    private void deleteHostQuietly(String hostId) {
        try {
            RestAssured.basePath = BASE_PATH;
            getRequestSpecification()
                    .pathParam("hostId", hostId)
                    .when()
                    .delete("/{hostId}");
        } catch (Exception | AssertionError e) {
            logger.warn("Failed to clean up host {}: {}", hostId, e.getMessage());
        }
    }

    private void cleanupTempFile() {
        if (xlsxFile.exists()) {
            FileUtils.deleteFile(xlsxFile.getPath());
        }
    }

    /**
     * Snapshot of the async host import progress, also used to render actionable
     * failure context in poll timeouts.
     */
    private record ImportProgress(boolean isEnd, int successSum, int errorSum) {
        static ImportProgress from(Response response) {
            return new ImportProgress(
                    response.jsonPath().getBoolean("data.end"),
                    response.jsonPath().getInt("data.successSum"),
                    response.jsonPath().getInt("data.errorSum"));
        }
    }
}
