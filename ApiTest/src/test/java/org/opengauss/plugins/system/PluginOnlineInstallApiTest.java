/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */

package org.opengauss.plugins.system;

import static org.opengauss.global.Constants.getRequestSpecification;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;

import org.hamcrest.Matchers;
import org.opengauss.global.AppConfigLoader;
import org.opengauss.global.Constants;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.WebSocket;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.time.Duration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletionStage;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLParameters;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

/**
 * P09 positive lifecycle probe for the frozen RC3 online-plugin protocol.
 *
 * <p>The fixture is selected only from the live unload list and uses official
 * RC3 URLs frozen in openGauss-visualtool.sql. The test always attempts to
 * remove only the selected fixture in {@link #cleanupOnlineFixture()}.</p>
 *
 * @since 2026/08/17
 */
public class PluginOnlineInstallApiTest {
    private static final Map<String, String> RC3_PLUGIN_URLS = Map.of(
        "alert-monitor", "https://opengauss.obs.cn-south-1.myhuaweicloud.com/latest/tools/Datakit/"
            + "visualtool-plugin/alert-monitor-7.0.0-RC3-repackage.jar",
        "webds-plugin", "https://opengauss.obs.cn-south-1.myhuaweicloud.com/latest/tools/Datakit/"
            + "visualtool-plugin/webds-plugin-7.0.0-RC3-repackage.jar",
        "observability-log-search", "https://opengauss.obs.cn-south-1.myhuaweicloud.com/latest/tools/Datakit/"
            + "visualtool-plugin/observability-log-search-7.0.0-RC3-repackage.jar",
        "observability-instance", "https://opengauss.obs.cn-south-1.myhuaweicloud.com/latest/tools/Datakit/"
            + "visualtool-plugin/observability-instance-7.0.0-RC3-repackage.jar",
        "data-migration", "https://opengauss.obs.cn-south-1.myhuaweicloud.com/latest/tools/Datakit/"
            + "visualtool-plugin/data-migration-7.0.0-RC3-repackage.jar",
        "base-ops", "https://opengauss.obs.cn-south-1.myhuaweicloud.com/latest/tools/Datakit/"
            + "visualtool-plugin/base-ops-7.0.0-RC3-repackage.jar",
        "observability-sql-diagnosis", "https://opengauss.obs.cn-south-1.myhuaweicloud.com/latest/tools/Datakit/"
            + "visualtool-plugin/observability-sql-diagnosis-7.0.0-RC3-repackage.jar",
        "compatibility-assessment", "https://opengauss.obs.cn-south-1.myhuaweicloud.com/latest/tools/Datakit/"
            + "visualtool-plugin/compatibility-assessment-7.0.0-RC3-repackage.jar",
        "container-management", "https://opengauss.obs.cn-south-1.myhuaweicloud.com/latest/tools/Datakit/"
            + "visualtool-plugin/container-management-plugin-7.0.0-RC3-repackage.jar");
    private static final Set<String> UNSAFE_DESTRUCTIVE_FIXTURE_IDS = Set.of("base-ops", "webds-plugin");
    private static final Duration POLL_INTERVAL = Duration.ofSeconds(2);
    private static final int POLL_ATTEMPTS = 45;

    private String pluginId;
    private String wsBusinessId;
    private WebSocket progressSocket;
    private boolean isFixtureInstalled;

    @BeforeClass
    public void selectDisposableOfficialFixture() {
        AppConfigLoader.loadConfig();
        Constants.loadToken();
        RestAssured.basePath = "/system/plugins";
        Response unload = getRequestSpecification().when().get("/unloadPluginsInfo");
        unload.then().statusCode(200).body("code", Matchers.equalTo(200))
            .body("data", Matchers.instanceOf(List.class));
        List<String> unloaded = unload.jsonPath().getList("data");
        Set<String> loadedIds = loadedPluginIds();
        pluginId = selectSafeFixture(unloaded, loadedIds);
    }

    @Test
    public void onlineInstallOfficialRc3FixtureAndRestoreInitialState() throws Exception {
        wsBusinessId = "issue20_online_" + Long.toUnsignedString(new SecureRandom().nextLong(), 36);
        progressSocket = openProgressSocket(wsBusinessId);

        Map<String, Object> payload = new HashMap<>();
        payload.put("pluginId", pluginId);
        payload.put("pluginUrl", RC3_PLUGIN_URLS.get(pluginId));
        payload.put("wsBusinessId", wsBusinessId);
        getRequestSpecification().contentType(ContentType.JSON).body(payload).when().post("/online_install")
            .then().statusCode(200).body("code", Matchers.equalTo(200));

        waitForLoaded(true);
        isFixtureInstalled = true;
        getRequestSpecification().pathParam("id", pluginId).when().post("/get/{id}")
            .then().statusCode(200).body("code", Matchers.equalTo(200)).body("data", Matchers.notNullValue());
    }

    @AfterClass(alwaysRun = true)
    public void cleanupOnlineFixture() throws InterruptedException {
        try {
            RestAssured.basePath = "/system/plugins";
            if (pluginId != null && (isFixtureInstalled || loadedPluginIds().contains(pluginId))) {
                getRequestSpecification().pathParam("id", pluginId).when().post("/uninstall/{id}")
                    .then().statusCode(200).body("code", Matchers.equalTo(200));
                waitForLoaded(false);
            }
        } finally {
            if (progressSocket != null) {
                progressSocket.sendClose(WebSocket.NORMAL_CLOSURE, "issue20 cleanup").join();
            }
        }
    }

    private Set<String> loadedPluginIds() {
        Response list = getRequestSpecification().when().get("/list");
        list.then().statusCode(200).body("code", Matchers.equalTo(200));
        List<Map<String, Object>> rows = list.jsonPath().getList("rows");
        Set<String> ids = new HashSet<>();
        if (rows != null) {
            for (Map<String, Object> row : rows) {
                Object id = row.get("pluginId");
                if (id != null) {
                    ids.add(String.valueOf(id));
                }
            }
        }
        return ids;
    }

    private void waitForLoaded(boolean isExpectedLoaded) throws InterruptedException {
        for (int attempt = 0; attempt < POLL_ATTEMPTS; attempt++) {
            if (loadedPluginIds().contains(pluginId) == isExpectedLoaded) {
                return;
            }
            Thread.sleep(POLL_INTERVAL.toMillis());
        }
        throw new AssertionError("plugin lifecycle state did not become loaded=" + isExpectedLoaded + ": " + pluginId);
    }

    private WebSocket openProgressSocket(String businessId) throws Exception {
        String baseUrl = AppConfigLoader.getAppConfig().getDatakit().getServerUrl();
        URI uri = URI.create(baseUrl.replaceFirst("^https", "wss").replaceFirst("^http", "ws")
            + "/websocket/base-ops/" + businessId);
        return createProgressHttpClient().newWebSocketBuilder().connectTimeout(Duration.ofSeconds(15))
            .buildAsync(uri, new WebSocket.Listener() {
                @Override
                public CompletionStage<?> onText(WebSocket webSocket, CharSequence data, boolean isLast) {
                    webSocket.request(1);
                    return null;
                }
            }).join();
    }

    static Map<String, String> officialRc3PluginUrls() {
        return RC3_PLUGIN_URLS;
    }

    static String selectSafeFixture(List<String> unloadedIds, Set<String> loadedIds) {
        return unloadedIds.stream()
            .filter(RC3_PLUGIN_URLS::containsKey)
            .filter(plugin -> !UNSAFE_DESTRUCTIVE_FIXTURE_IDS.contains(plugin))
            .filter(plugin -> !loadedIds.contains(plugin))
            .findFirst()
            .orElseThrow(() -> new AssertionError("no safe official RC3 plugin fixture is currently unloaded"));
    }

    static HttpClient createProgressHttpClient() throws Exception {
        // The disposable RC3 certificate has CN=opengauss and no IP SAN. Keep
        // hostname-verification relaxation on this client rather than the JVM.
        SSLParameters sslParameters = new SSLParameters();
        sslParameters.setEndpointIdentificationAlgorithm(null);
        return HttpClient.newBuilder().sslContext(trustAllSslContext()).sslParameters(sslParameters)
            .connectTimeout(Duration.ofSeconds(15)).build();
    }

    private static SSLContext trustAllSslContext() throws Exception {
        TrustManager[] trustAll = new TrustManager[]{new X509TrustManager() {
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
        SSLContext context = SSLContext.getInstance("TLS");
        context.init(null, trustAll, new SecureRandom());
        return context;
    }
}
