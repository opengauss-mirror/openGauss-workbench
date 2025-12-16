/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package org.opengauss.admin.system.utils;

import io.milvus.v2.client.ConnectConfig;
import io.milvus.v2.client.MilvusClientV2;
import io.milvus.v2.service.collection.CollectionInfo;

import org.opengauss.admin.common.exception.ops.OpsException;
import org.opengauss.admin.common.utils.StringUtils;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

/**
 * MilvusUtils
 *
 * @since 2025/11/10
 */
public class MilvusUtils {
    /**
     * Create a Milvus client V2.
     *
     * @param ip the Milvus ip
     * @param port the Milvus port
     * @param database the Milvus database name (optional)
     * @param username the Milvus username (optional)
     * @param password the Milvus password (optional)
     * @return the Milvus client V2
     */
    public static MilvusClientV2 createMilvusClientV2(
            String ip, int port, String database, String username, String password
    ) {
        if (ip == null || ip.isEmpty()) {
            throw new IllegalArgumentException("IP cannot be null or empty");
        }
        if (port <= 0) {
            throw new IllegalArgumentException("Port must be a positive integer");
        }

        String uri = String.format(Locale.ROOT, "http://%s:%d", ip, port);
        ConnectConfig connectConfig = ConnectConfig.builder().uri(uri).build();

        if (database != null && !database.isEmpty()) {
            connectConfig.setDbName(database);
        }
        if (username != null && password != null) {
            connectConfig.setUsername(username);
            connectConfig.setPassword(password);
        }

        return new MilvusClientV2(connectConfig);
    }

    /**
     * Get the version number of the Milvus cluster.
     *
     * @param milvusClientV2 the Milvus client V2
     * @return the version number of the Milvus cluster
     */
    public static String getMilvusVersion(MilvusClientV2 milvusClientV2) {
        if (milvusClientV2 == null) {
            throw new IllegalArgumentException("MilvusClientV2 cannot be null");
        }

        return milvusClientV2.getServerVersion().replace("v", "");
    }

    /**
     * List all databases in the Milvus cluster
     *
     * @param client the Milvus client V2
     * @return the list of database names
     */
    public static List<String> listDatabases(MilvusClientV2 client) {
        if (client == null) {
            throw new IllegalArgumentException("MilvusClientV2 cannot be null");
        }

        return client.listDatabases().getDatabaseNames();
    }

    /**
     * List all collections in the Milvus cluster.
     *
     * @param client the Milvus client V2
     * @return the list of collection names
     */
    public static List<String> listCollections(MilvusClientV2 client) {
        if (client == null) {
            throw new IllegalArgumentException("MilvusClientV2 cannot be null");
        }

        List<CollectionInfo> collectionInfos = client.listCollections().getCollectionInfos();
        return collectionInfos.stream()
                .map(CollectionInfo::getCollectionName)
                .collect(Collectors.toList());
    }

    /**
     * Close the Milvus client V2.
     *
     * @param milvusClientV2 the Milvus client V2
     */
    public static void closeMilvusClientV2(MilvusClientV2 milvusClientV2) {
        if (milvusClientV2 == null) {
            throw new IllegalArgumentException("MilvusClientV2 cannot be null");
        }

        milvusClientV2.close();
    }

    /**
     * Parse the url string to URL object.
     *
     * @param urlStr the url string
     * @return the URL object
     */
    public static URL parseUrl(String urlStr) {
        if (StringUtils.isEmpty(urlStr)) {
            throw new OpsException("Url cannot be empty");
        }

        try {
            URL url = new URL(urlStr);
            if ("https".equals(url.getProtocol())) {
                throw new OpsException("Only support http protocol, not https");
            }
            int port = url.getPort();
            if (port == -1) {
                throw new OpsException("Port cannot be empty");
            }
            if (port < 0 || port > 65535) {
                throw new OpsException("Invalid port number: " + port);
            }
            return url;
        } catch (MalformedURLException e) {
            throw new OpsException("Invalid url: " + urlStr);
        }
    }
}
