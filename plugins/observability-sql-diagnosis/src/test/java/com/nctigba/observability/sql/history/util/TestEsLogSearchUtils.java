/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package com.nctigba.observability.sql.history.util;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.SortOptions;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.indices.ElasticsearchIndicesClient;
import co.elastic.clients.elasticsearch.indices.GetIndexRequest;
import co.elastic.clients.elasticsearch.indices.GetIndexResponse;
import co.elastic.clients.elasticsearch.indices.IndexState;
import co.elastic.clients.util.ObjectBuilder;
import com.nctigba.common.web.exception.HisDiagnosisException;
import com.nctigba.observability.sql.config.ElasticsearchProvider;
import com.nctigba.observability.sql.model.history.query.EsSearchQuery;
import com.nctigba.observability.sql.util.EsLogSearchUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * TestEsLogSearchUtils
 *
 * @author luomeng
 * @since 2023/7/13
 */
@RunWith(MockitoJUnitRunner.class)
public class TestEsLogSearchUtils {
    @Mock
    private ElasticsearchProvider clientProvider;

    @InjectMocks
    private EsLogSearchUtils util;

    @Test
    public void testQueryLogInfo_noData() {
        try {
            ElasticsearchClient client = mock(ElasticsearchClient.class);
            when(clientProvider.client()).thenReturn(client);
            SearchResponse<HashMap> searchResponse = mock(SearchResponse.class);
            when(client.search(
                    (Function<SearchRequest.Builder, ObjectBuilder<SearchRequest>>) any(),
                    Mockito.eq(HashMap.class))).thenReturn(searchResponse);
            EsSearchQuery queryParam = new EsSearchQuery();
            queryParam.setClusterId(new ArrayList<>());
            queryParam.setLogLevel(new ArrayList<>());
            queryParam.setLogType(new ArrayList<>());
            queryParam.setNodeId(new ArrayList<>());
            queryParam.setEndDate(new Date());
            queryParam.setStartDate(new Date());
            queryParam.setSorts(new ArrayList<>());
            queryParam.setId("");
            Optional<SearchResponse<HashMap>> response = util.queryLogInfo(queryParam);
            assertNotNull(response);
        } catch (IOException e) {
            throw new HisDiagnosisException("connect fail");
        }
    }

    @Test
    public void testIndexList() {
        try {
            ElasticsearchClient client = mock(ElasticsearchClient.class);
            when(clientProvider.client()).thenReturn(client);
            ElasticsearchIndicesClient indicesClient = mock(ElasticsearchIndicesClient.class);
            when(client.indices()).thenReturn(indicesClient);
            GetIndexResponse getIndexResponse = mock(GetIndexResponse.class);
            when(indicesClient.get(
                    (Function<GetIndexRequest.Builder, ObjectBuilder<GetIndexRequest>>) any())).thenReturn(
                    getIndexResponse);
            Map<String, IndexState> map = mock(Map.class);
            map.put("1", mock(IndexState.class));
            when(getIndexResponse.result()).thenReturn(map);
            String indexName = "";
            Optional<Set<String>> result = util.indexList(indexName);
            assertNotNull(result);
        } catch (IOException e) {
            throw new HisDiagnosisException("connect fail");
        }
    }

    @Test
    public void testGetIndexName_noData() {
        EsSearchQuery queryParam = new EsSearchQuery();
        queryParam.setClusterId(new ArrayList<>());
        queryParam.setLogLevel(new ArrayList<>());
        queryParam.setLogType(new ArrayList<>());
        queryParam.setNodeId(new ArrayList<>());
        queryParam.setEndDate(new Date());
        queryParam.setStartDate(new Date());
        queryParam.setSorts(new ArrayList<>());
        queryParam.setId("");
        String result = util.getIndexName(queryParam);
        assertNotNull(result);
    }

    @Test
    public void testGetIndexName_hasData() {
        EsSearchQuery queryParam = new EsSearchQuery();
        queryParam.setClusterId(new ArrayList<>());
        queryParam.setLogLevel(new ArrayList<>());
        List<String> logType = new ArrayList<>();
        logType.add("opengauss-error-log");
        queryParam.setLogType(logType);
        List<String> nodes = new ArrayList<>();
        nodes.add("37e8a893-0b7e-49b2-a0b4-e6fdf7dc4345");
        queryParam.setNodeId(nodes);
        queryParam.setEndDate(new Date());
        queryParam.setStartDate(new Date());
        queryParam.setSorts(new ArrayList<>());
        queryParam.setId("");
        String result = util.getIndexName(queryParam);
        assertNotNull(result);
    }

    @Test
    public void testQuery() {
        EsSearchQuery queryParam = new EsSearchQuery();
        queryParam.setClusterId(new ArrayList<>());
        queryParam.setLogLevel(new ArrayList<>());
        queryParam.setLogType(new ArrayList<>());
        queryParam.setNodeId(new ArrayList<>());
        queryParam.setEndDate(new Date());
        queryParam.setStartDate(new Date());
        queryParam.setSorts(new ArrayList<>());
        queryParam.setId("");
        Function<Query.Builder, ObjectBuilder<Query>> result = util.query(queryParam);
        assertNotNull(result);
    }

    @Test
    public void testQuery_startNull() {
        EsSearchQuery queryParam = new EsSearchQuery();
        queryParam.setClusterId(new ArrayList<>());
        queryParam.setLogLevel(new ArrayList<>());
        queryParam.setLogType(new ArrayList<>());
        queryParam.setNodeId(new ArrayList<>());
        queryParam.setEndDate(new Date());
        queryParam.setSorts(new ArrayList<>());
        queryParam.setId("");
        Function<Query.Builder, ObjectBuilder<Query>> result = util.query(queryParam);
        assertNotNull(result);
    }

    @Test
    public void testQuery_endNull() {
        EsSearchQuery queryParam = new EsSearchQuery();
        queryParam.setClusterId(new ArrayList<>());
        queryParam.setLogLevel(new ArrayList<>());
        queryParam.setLogType(new ArrayList<>());
        queryParam.setNodeId(new ArrayList<>());
        queryParam.setStartDate(new Date());
        queryParam.setSorts(new ArrayList<>());
        queryParam.setId("");
        Function<Query.Builder, ObjectBuilder<Query>> result = util.query(queryParam);
        assertNotNull(result);
    }

    @Test
    public void testSort() {
        EsSearchQuery queryParam = new EsSearchQuery();
        queryParam.setClusterId(new ArrayList<>());
        queryParam.setLogLevel(new ArrayList<>());
        queryParam.setLogType(new ArrayList<>());
        queryParam.setNodeId(new ArrayList<>());
        queryParam.setEndDate(new Date());
        queryParam.setStartDate(new Date());
        queryParam.setSorts(new ArrayList<>());
        queryParam.setId("");
        Function<SortOptions.Builder, ObjectBuilder<SortOptions>> result = util.sort(queryParam);
        assertNotNull(result);
    }

    @Test
    public void testIndexsSort() {
        EsSearchQuery queryParam = new EsSearchQuery();
        queryParam.setClusterId(new ArrayList<>());
        queryParam.setLogLevel(new ArrayList<>());
        queryParam.setLogType(new ArrayList<>());
        queryParam.setNodeId(new ArrayList<>());
        queryParam.setEndDate(new Date());
        queryParam.setStartDate(new Date());
        queryParam.setSorts(new ArrayList<>());
        queryParam.setId("");
        Function<SortOptions.Builder, ObjectBuilder<SortOptions>> result = util.indexsSort(queryParam);
        assertNotNull(result);
    }

    @Test
    public void testLogSort() {
        EsSearchQuery queryParam = new EsSearchQuery();
        queryParam.setClusterId(new ArrayList<>());
        queryParam.setLogLevel(new ArrayList<>());
        queryParam.setLogType(new ArrayList<>());
        queryParam.setNodeId(new ArrayList<>());
        queryParam.setEndDate(new Date());
        queryParam.setStartDate(new Date());
        queryParam.setSorts(new ArrayList<>());
        queryParam.setId("");
        Function<SortOptions.Builder, ObjectBuilder<SortOptions>> result = util.logSort(queryParam);
        assertNotNull(result);
    }

    @Test
    public void testSort_NotNull() {
        EsSearchQuery queryParam = new EsSearchQuery();
        queryParam.setClusterId(new ArrayList<>());
        queryParam.setLogLevel(new ArrayList<>());
        queryParam.setLogType(new ArrayList<>());
        queryParam.setNodeId(new ArrayList<>());
        queryParam.setEndDate(new Date());
        queryParam.setStartDate(new Date());
        queryParam.setSorts(new ArrayList<>());
        queryParam.setId("");
        queryParam.setOrder("DESC");
        Function<SortOptions.Builder, ObjectBuilder<SortOptions>> result = util.sort(queryParam);
        assertNotNull(result);
    }

    @Test
    public void testIndexsSort_NotNull() {
        EsSearchQuery queryParam = new EsSearchQuery();
        queryParam.setClusterId(new ArrayList<>());
        queryParam.setLogLevel(new ArrayList<>());
        queryParam.setLogType(new ArrayList<>());
        queryParam.setNodeId(new ArrayList<>());
        queryParam.setEndDate(new Date());
        queryParam.setStartDate(new Date());
        queryParam.setSorts(new ArrayList<>());
        queryParam.setId("");
        queryParam.setOrder("DESC");
        Function<SortOptions.Builder, ObjectBuilder<SortOptions>> result = util.indexsSort(queryParam);
        assertNotNull(result);
    }

    @Test
    public void testLogSort_NotNull() {
        EsSearchQuery queryParam = new EsSearchQuery();
        queryParam.setClusterId(new ArrayList<>());
        queryParam.setLogLevel(new ArrayList<>());
        queryParam.setLogType(new ArrayList<>());
        queryParam.setNodeId(new ArrayList<>());
        queryParam.setEndDate(new Date());
        queryParam.setStartDate(new Date());
        queryParam.setSorts(new ArrayList<>());
        queryParam.setId("");
        queryParam.setOrder("DESC");
        Function<SortOptions.Builder, ObjectBuilder<SortOptions>> result = util.logSort(queryParam);
        assertNotNull(result);
    }
}
