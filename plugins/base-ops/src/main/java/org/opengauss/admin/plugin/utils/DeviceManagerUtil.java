/*
 * Copyright (c) 2022-2022 Huawei Technologies Co.,Ltd.
 *
 * openGauss is licensed under Mulan PSL v2.
 * You can use this software according to the terms and conditions of the Mulan PSL v2.
 * You may obtain a copy of Mulan PSL v2 at:
 *
 *           http://license.coscl.org.cn/MulanPSL2
 *
 * THIS SOFTWARE IS PROVIDED ON AN "AS IS" BASIS, WITHOUT WARRANTIES OF ANY KIND,
 * EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO NON-INFRINGEMENT,
 * MERCHANTABILITY OR FIT FOR A PARTICULAR PURPOSE.
 * See the Mulan PSL v2 for more details.
 */

package org.opengauss.admin.plugin.utils;

import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpStatus;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.util.EntityUtils;
import org.opengauss.admin.common.utils.StringUtils;
import org.opengauss.admin.plugin.domain.entity.ops.OpsDeviceManagerEntity;
import org.opengauss.admin.plugin.domain.model.ops.DisasterQueryResult;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;

/**
 * device manager api tools class
 *
 * @author wbd
 * @since 2024/1/26 11:14
 **/
@Slf4j
public class DeviceManagerUtil {
    private static final RequestConfig REQUEST_CONFIG = RequestConfig.custom()
        .setConnectTimeout(3000)
        .setConnectionRequestTimeout(3000)
        .setSocketTimeout(60000)
        .build();

    private static final String SESSION_URL = "https://%s:%s/deviceManager/rest/datakit/sessions";

    private static final String SWITCH_PAIR_URL = "https://%s:%s/deviceManager/rest/%s/REPLICATIONPAIR/switch";

    private static final String QUERY_ACTIVATE_CANCEL_PAIR_URL =
        "https://%s:%s/deviceManager/rest/%s/REPLICATIONPAIR/%s";

    private static final String SYNC_PAIR_URL = "https://%s:%s/deviceManager/rest/%s/REPLICATIONPAIR/sync";

    private static final String SPLIT_PAIR_URL = "https://%s:%s/deviceManager/rest/%s/REPLICATIONPAIR/split";

    private static final String GET_METHOD = "GET";

    private static final String POST_METHOD = "POST";

    private static final String PUT_METHOD = "PUT";

    private static final int LUN_ONLY_READ = 2;

    private static final int LUN_READ_WRITE = 3;

    @Getter
    enum SyncSpeed {
        LOW(1), MEDIUM(2), HIGH(3), VERY_HIGH(4);

        private final int speed;
        SyncSpeed(int speed) {
            this.speed = speed;
        }
    }

    /**
     * authenticate by username and password
     *
     * @param deviceManagerEntity deviceManagerEntity
     * @return Map
     */
    public static Map<String, String> authenticate(OpsDeviceManagerEntity deviceManagerEntity) {
        log.error("device manager password is {}", deviceManagerEntity.getPassword());
        // 组装数据
        JSONObject paramJson = new JSONObject();
        paramJson.put("username", deviceManagerEntity.getUserName());
        // 注意密码在上层调用解密好
        paramJson.put("password", deviceManagerEntity.getPassword());
        // 0：本地用户，1：LDAP用户，8：RADIUS用户
        paramJson.put("scope", "0");
        log.info("======start authenticate======");
        JSONObject resultJson = send(
            String.format(SESSION_URL, deviceManagerEntity.getHostIp(), deviceManagerEntity.getPort()), paramJson, null,
            POST_METHOD);
        log.info("resultJson is {}", resultJson);
        Map<String, String> result = new HashMap<>();
        // 如果error中的code不为0,则不取参数，因为此时也取不到
        if (!resultJson.isEmpty() && resultJson.getJSONObject("error").getInteger("code") != 0) {
            log.error("authenticate failed, message is {}, suggestion is {}",
                resultJson.getJSONObject("error").getString("description"),
                resultJson.getJSONObject("error").getString("suggestion"));
            return result;
        }
        // 请求正常时，解析取出所需内容
        if (!resultJson.isEmpty()) {
            result.put("cookie", resultJson.getString("cookie"));
            result.put("iBaseToken", resultJson.getJSONObject("data").getString("iBaseToken"));
            result.put("deviceId", resultJson.getJSONObject("data").getString("deviceid"));
        }
        log.info("======end authenticate======");
        return result;
    }

    /**
     * query replication pair status
     *
     * @param deviceManagerEntity deviceManagerEntity
     * @param authenticateMap authenticateMap
     * @return Object
     */
    public static DisasterQueryResult queryPairInfo(OpsDeviceManagerEntity deviceManagerEntity,
                                                    Map<String, String> authenticateMap) {
        // 组装header数据
        Map<String, String> headers = new HashMap<>();
        headers.put("iBaseToken", authenticateMap.get("iBaseToken"));
        headers.put("Cookie", authenticateMap.get("cookie"));
        log.info("======start queryPairInfo======");
        JSONObject resultJson = send(
            String.format(QUERY_ACTIVATE_CANCEL_PAIR_URL, deviceManagerEntity.getHostIp(),
                deviceManagerEntity.getPort(), authenticateMap.get("deviceId"), deviceManagerEntity.getPairId()),
                null, headers, GET_METHOD);
        log.info("resultJson is {}", resultJson);
        DisasterQueryResult queryResult = new DisasterQueryResult();
        if (!resultJson.isEmpty() && resultJson.getJSONObject("error").getInteger("code") != 0) {
            log.error("queryPairInfo failed, message is {}, suggestion is {}",
                resultJson.getJSONObject("error").getString("description"),
                resultJson.getJSONObject("error").getString("suggestion"));
            return queryResult;
        }
        // 获取同步状态和进度
        if (!resultJson.isEmpty()) {
            queryResult.setRunningStatus(resultJson.getJSONObject("data").getString("RUNNINGSTATUS"));
            // REPLICATIONPROGRESS是条件返回，所以有可能为空
            if (resultJson.getJSONObject("data").containsKey("REPLICATIONPROGRESS")) {
                queryResult.setReplicationProgress(resultJson.getJSONObject("data").getString("REPLICATIONPROGRESS"));
            } else {
                queryResult.setReplicationProgress("");
            }
            queryResult.setReplicationProgress(resultJson.getJSONObject("data").getString("REPLICATIONPROGRESS"));
        }
        log.info("======end queryPairInfo======, status is {}", queryResult.getRunningStatus());
        return queryResult;
    }

    /**
     * switch master to slave
     *
     * @param deviceManagerEntity deviceManagerEntity
     * @param authenticateMap authenticateMap
     * @return boolean
     */
    public static boolean switchMasterToSlave(OpsDeviceManagerEntity deviceManagerEntity,
                                              Map<String, String> authenticateMap) {
        log.info("======start switchMasterToSlave======");
        // 组装数据
        JSONObject paramJson = new JSONObject();
        paramJson.put("ID", deviceManagerEntity.getPairId());
        return sendRequest(deviceManagerEntity, SWITCH_PAIR_URL, authenticateMap, paramJson);
    }

    /**
     * sync replication pair
     *
     * @param deviceManagerEntity deviceManagerEntity
     * @param authenticateMap authenticateMap
     * @return boolean
     */
    public static boolean syncPair(OpsDeviceManagerEntity deviceManagerEntity,
                                   Map<String, String> authenticateMap) {
        log.info("======start syncPair======");
        // 组装数据
        JSONObject paramJson = new JSONObject();
        paramJson.put("ID", deviceManagerEntity.getPairId());
        return sendRequest(deviceManagerEntity, SYNC_PAIR_URL, authenticateMap, paramJson);
    }

    /**
     * modify replication params
     *
     * @param deviceManagerEntity deviceManagerEntity
     * @param authenticateMap authenticateMap
     * @return boolean
     */
    public static boolean modifyReplicationParams(OpsDeviceManagerEntity deviceManagerEntity,
                                                  Map<String, String> authenticateMap) {
        log.info("======modify Replication Params======");
        JSONObject paramJson = new JSONObject();
        paramJson.put("ID", deviceManagerEntity.getPairId());
        paramJson.put("SPEED", SyncSpeed.HIGH.getSpeed());
        // 组装header数据
        Map<String, String> headers = new HashMap<>();
        headers.put("iBaseToken", authenticateMap.get("iBaseToken"));
        headers.put("Cookie", authenticateMap.get("cookie"));
        JSONObject resultJson = send(String.format(QUERY_ACTIVATE_CANCEL_PAIR_URL, deviceManagerEntity.getHostIp(),
                deviceManagerEntity.getPort(), authenticateMap.get("deviceId"), deviceManagerEntity.getPairId()),
                paramJson, headers, PUT_METHOD);
        log.info("resultJson is {}", resultJson);
        // 解析取出所需内容
        if (!resultJson.isEmpty() && resultJson.getJSONObject("error").getInteger("code") != 0) {
            log.error("slaveResourceProtect failed, message is {}, suggestion is {}",
                    resultJson.getJSONObject("error").getString("description"),
                    resultJson.getJSONObject("error").getString("suggestion"));
            return false;
        }
        return true;
    }

    /**
     * activate salve resource protect
     *
     * @param deviceManagerEntity deviceManagerEntity
     * @param deviceId deviceId
     * @param iBaseToken iBaseToken
     * @param cookie cookie
     * @return boolean
     */
    public static boolean activateSlaveResourceProtect(OpsDeviceManagerEntity deviceManagerEntity, String deviceId,
        String iBaseToken, String cookie) {
        log.info("======start activateSlaveResourceProtect======");
        return slaveResourceProtect(deviceManagerEntity, deviceId, iBaseToken, cookie, LUN_ONLY_READ);
    }

    /**
     * cancel salve resource protect
     *
     * @param deviceManagerEntity deviceManagerEntity
     * @param deviceId deviceId
     * @param iBaseToken iBaseToken
     * @param cookie cookie
     * @return boolean
     */
    public static boolean cancelSlaveResourceProtect(OpsDeviceManagerEntity deviceManagerEntity, String deviceId,
        String iBaseToken, String cookie) {
        log.info("======start cancelSlaveResourceProtect======");
        return slaveResourceProtect(deviceManagerEntity, deviceId, iBaseToken, cookie, LUN_READ_WRITE);
    }

    private static boolean slaveResourceProtect(OpsDeviceManagerEntity deviceManagerEntity, String deviceId,
        String iBaseToken, String cookie, int flag) {
        // 组装数据
        JSONObject paramJson = new JSONObject();
        paramJson.put("SECRESACCESS", flag);
        // 组装header数据
        Map<String, String> headers = new HashMap<>();
        headers.put("iBaseToken", iBaseToken);
        headers.put("Cookie", cookie);
        JSONObject resultJson = send(String.format(QUERY_ACTIVATE_CANCEL_PAIR_URL, deviceManagerEntity.getHostIp(),
            deviceManagerEntity.getPort(), deviceId, deviceManagerEntity.getPairId()), paramJson, headers, PUT_METHOD);
        log.info("resultJson is {}", resultJson);
        // 解析取出所需内容
        if (!resultJson.isEmpty() && resultJson.getJSONObject("error").getInteger("code") != 0) {
            log.error("slaveResourceProtect failed, message is {}, suggestion is {}",
                resultJson.getJSONObject("error").getString("description"),
                resultJson.getJSONObject("error").getString("suggestion"));
            return false;
        }
        return true;
    }

    /**
     * split replication pair
     *
     * @param deviceManagerEntity deviceManagerEntity
     * @param authenticateMap authenticateMap
     * @return boolean
     */
    public static boolean splitPair(OpsDeviceManagerEntity deviceManagerEntity,
                                    Map<String, String> authenticateMap) {
        log.info("======start splitPair======");
        // 组装数据
        JSONObject paramJson = new JSONObject();
        paramJson.put("ID", deviceManagerEntity.getPairId());
        return sendRequest(deviceManagerEntity, SPLIT_PAIR_URL, authenticateMap, paramJson);
    }

    private static boolean sendRequest(OpsDeviceManagerEntity deviceManagerEntity, String url,
                                       Map<String, String> authenticateMap, JSONObject paramJson) {
        // 组装header数据
        Map<String, String> headers = new HashMap<>();
        headers.put("iBaseToken", authenticateMap.get("iBaseToken"));
        headers.put("Cookie", authenticateMap.get("cookie"));
        JSONObject resultJson = send(
            String.format(url, deviceManagerEntity.getHostIp(), deviceManagerEntity.getPort(), authenticateMap.get(
                    "deviceId")), paramJson, headers, PUT_METHOD);
        log.info("resultJson is {}", resultJson);
        // 解析取出所需内容
        if (!resultJson.isEmpty() && resultJson.getJSONObject("error").getInteger("code") != 0) {
            log.error("sendRequest failed, message is {}, suggestion is {}",
                resultJson.getJSONObject("error").getString("description"),
                resultJson.getJSONObject("error").getString("suggestion"));
            return false;
        }
        return true;
    }

    private static JSONObject send(String url, JSONObject param, Map<String, String> headers, String method) {
        JSONObject result = new JSONObject();
        if (StringUtils.isEmpty(url) || StringUtils.isEmpty(method)) {
            log.error("url or method is empty, url:{}, method:{}", url, method);
            return result;
        }
        CloseableHttpClient httpClient;
        CloseableHttpResponse response = null;
        HttpRequestBase httpRequestBase;
        try {
            StringEntity entity = null;
            if (param != null) {
                entity = new StringEntity(param.toString(), "UTF-8");
                entity.setContentEncoding("UTF-8");
                entity.setContentType("application/json");
            }
            if ("GET".equals(method)) {
                httpRequestBase = new HttpGet(url);
            } else if ("PUT".equals(method)) {
                httpRequestBase = new HttpPut(url);
                ((HttpPut) httpRequestBase).setEntity(entity);
            } else {
                httpRequestBase = new HttpPost(url);
                ((HttpPost) httpRequestBase).setEntity(entity);
            }
            if (headers != null) {
                headers.forEach((key, value) -> httpRequestBase.addHeader(key, value));
            }
            httpRequestBase.setConfig(REQUEST_CONFIG);
            httpClient = createHttpClient().get();
            response = httpClient.execute(httpRequestBase);
            if (response != null) {
                log.info("status code is {}", response.getStatusLine().getStatusCode());
                if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                    result = JSONObject.parseObject(EntityUtils.toString(response.getEntity(), "UTF-8"));
                    result.put("cookie", response.getFirstHeader("Set-Cookie").getValue());
                } else {
                    log.error("response code is {},msg is {}", response.getStatusLine().getStatusCode(),
                        EntityUtils.toString(response.getEntity(), "UTF-8"));
                }
            }
        } catch (IOException | JSONException | NoSuchElementException e) {
            log.error("execute failed.", e);
        }
        return result;
    }

    private static Optional<CloseableHttpClient> createHttpClient() {
        CloseableHttpClient httpClient;
        try {
            SSLContext sc = SSLContext.getInstance("TLSv1.2");
            sc.init(null, new TrustManager[] {new TrustAnyTrustManager()}, new java.security.SecureRandom());
            SSLConnectionSocketFactory socketFactory = new SSLConnectionSocketFactory(sc,
                NoopHostnameVerifier.INSTANCE);
            Registry<ConnectionSocketFactory> socketFactoryRegistry = RegistryBuilder.<ConnectionSocketFactory>create()
                .register("http", PlainConnectionSocketFactory.INSTANCE)
                .register("https", socketFactory)
                .build();
            PoolingHttpClientConnectionManager connectionManager = new PoolingHttpClientConnectionManager(
                socketFactoryRegistry);
            httpClient = HttpClients.custom().setConnectionManager(connectionManager).build();
            return Optional.of(httpClient);
        } catch (GeneralSecurityException e) {
            log.error("createHttpClient failed.");
        }
        return Optional.empty();
    }

    private static class TrustAnyTrustManager implements X509TrustManager {
        @Override
        public void checkClientTrusted(X509Certificate[] chain, String authType) {
        }

        @Override
        public void checkServerTrusted(X509Certificate[] chain, String authType) {
        }

        @Override
        public X509Certificate[] getAcceptedIssuers() {
            return new X509Certificate[] {};
        }
    }
}
