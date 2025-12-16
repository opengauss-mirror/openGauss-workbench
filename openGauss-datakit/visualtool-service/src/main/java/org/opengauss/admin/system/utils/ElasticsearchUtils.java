/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package org.opengauss.admin.system.utils;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.elasticsearch.client.Request;
import org.elasticsearch.client.Response;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * ElasticsearchUtils
 *
 * @since 2025/11/10
 */
public class ElasticsearchUtils {
    private static final String HEALTH_STATUS_URL = "/_cluster/health";
    private static final String INDEX_URL = "/_cat/indices";
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    /**
     * Create an ElasticSearch REST client.
     *
     * @param ip the ElasticSearch ip
     * @param port the ElasticSearch port
     * @param username the ElasticSearch username (optional)
     * @param password the ElasticSearch password (optional)
     * @return the ElasticSearch REST client
     */
    public static RestClient createRestClient(String ip, int port, String username, String password) {
        if (ip == null || ip.isEmpty()) {
            throw new IllegalArgumentException("IP cannot be null or empty");
        }
        if (port <= 0) {
            throw new IllegalArgumentException("Port must be a positive integer");
        }

        RestClientBuilder builder = RestClient.builder(new HttpHost(ip, port, "http"));
        if (username != null && password != null) {
            CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
            credentialsProvider.setCredentials(AuthScope.ANY, new UsernamePasswordCredentials(username, password));

            builder.setHttpClientConfigCallback(httpClientBuilder ->
                    httpClientBuilder.setDefaultCredentialsProvider(credentialsProvider)
            );
        }
        return builder.build();
    }

    /**
     * Get the health status code of the ElasticSearch cluster.
     *
     * @param restClient the ElasticSearch REST client to test
     * @return the health status code of the ElasticSearch cluster
     * @throws IOException if an I/O error occurs while testing the connection
     */
    public static int healthStatusCode(RestClient restClient) throws IOException {
        if (restClient == null) {
            throw new IllegalArgumentException("RestClient cannot be null");
        }

        Request request = new Request("GET", HEALTH_STATUS_URL);
        Response response = restClient.performRequest(request);
        return response.getStatusLine().getStatusCode();
    }

    /**
     * Get the health status of the ElasticSearch cluster.
     *
     * @param restClient the ElasticSearch REST client to test
     * @return the health status of the ElasticSearch cluster
     * @throws IOException if an I/O error occurs while testing the connection
     */
    public static Map<String, Object> healthInfo(RestClient restClient) throws IOException {
        Request request = new Request("GET", HEALTH_STATUS_URL);
        Response response = restClient.performRequest(request);

        String responseBody = getResponseBody(response);
        return OBJECT_MAPPER.readValue(
                responseBody,
                new TypeReference<Map<String, Object>>() {}
        );
    }

    /**
     * Get the version number of the ElasticSearch cluster.
     *
     * @param restClient the ElasticSearch REST client to test
     * @return the version number of the ElasticSearch cluster
     * @throws IOException if an I/O error occurs while testing the connection
     */
    public static String getElasticsearchVersion(RestClient restClient) throws IOException {
        Request request = new Request("GET", "/");
        Response response = restClient.performRequest(request);

        String responseBody = getResponseBody(response);
        if (responseBody.contains("\"number\" : \"")) {
            int startIndex = responseBody.indexOf("\"number\" : \"") + "\"number\" : \"".length();
            int endIndex = responseBody.indexOf("\"", startIndex);
            return responseBody.substring(startIndex, endIndex);
        }

        throw new IOException("Failed to extract version number from response body");
    }

    /**
     * List all indexes in the ElasticSearch cluster.
     *
     * @param restClient the ElasticSearch REST client to test
     * @return the list of index names
     * @throws IOException if an I/O error occurs while testing the connection
     */
    public static List<String> listIndexes(RestClient restClient) throws IOException {
        Request request = new Request("GET", INDEX_URL);
        Response response = restClient.performRequest(request);

        String responseBody = getResponseBody(response);
        return Arrays.stream(responseBody.split("\n"))
                .map(line -> line.split("\\s+")[2])
                .collect(Collectors.toList());
    }

    /**
     * Close the ElasticSearch REST client.
     *
     * @param restClient the ElasticSearch REST client to close
     * @throws IOException if an I/O error occurs while closing the client
     */
    public static void closeClient(RestClient restClient) throws IOException {
        if (restClient == null) {
            return;
        }

        restClient.close();
    }

    /**
     * Parse the url string to URL object.
     *
     * @param urlStr the url string
     * @return the URL object
     */
    public static URL parseUrl(String urlStr) {
        return MilvusUtils.parseUrl(urlStr);
    }

    private static String getResponseBody(Response response) throws IOException {
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(response.getEntity().getContent(), StandardCharsets.UTF_8))) {
            return reader.lines().collect(Collectors.joining("\n"));
        }
    }
}
