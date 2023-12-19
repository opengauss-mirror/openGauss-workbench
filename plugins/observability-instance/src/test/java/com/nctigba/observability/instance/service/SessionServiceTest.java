/*
 *  Copyright (c) GBA-NCTI-ISDC. 2022-2024.
 *
 *  openGauss DataKit is licensed under Mulan PSL v2.
 *  You can use this software according to the terms and conditions of the Mulan PSL v2.
 *  You may obtain a copy of Mulan PSL v2 at:
 *
 *  http://license.coscl.org.cn/MulanPSL2
 *
 *  THIS SOFTWARE IS PROVIDED ON AN "AS IS" BASIS, WITHOUT WARRANTIES OF ANY KIND,
 *  EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO NON-INFRINGEMENT,
 *  MERCHANTABILITY OR FITFOR A PARTICULAR PURPOSE.
 *  See the Mulan PSL v2 for more details.
 *  -------------------------------------------------------------------------
 *
 *  SessionServiceTest.java
 *
 *  IDENTIFICATION
 *  plugins/observability-instance/src/test/java/com/nctigba/observability/instance/service/SessionServiceTest.java
 *
 *  -------------------------------------------------------------------------
 */

package com.nctigba.observability.instance.service;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.nctigba.observability.instance.model.dto.session.DetailStatisticDTO;
import com.nctigba.observability.instance.mapper.SessionMapper;

/**
 * SessionServiceImplTest.java
 *
 * @since 2023年7月17日
 */
@ExtendWith(MockitoExtension.class)
class SessionServiceTest {
    @InjectMocks
    private SessionService sessionService;
    @Mock
    private SessionMapper sessionMapper;

    private static String getId() {
        return "12345";
    }

    @Test
    public void testDetailGeneral() {
        when(sessionMapper.generalMesList(anyString())).thenReturn(List.of(Map.of("1", 1)));
        when(sessionMapper.sessionIsWaiting(anyString())).thenReturn(1);
        sessionService.detailGeneral(getId(), getId());
    }

    @Test
    public void testDetailStatistic() {
        when(sessionMapper.statistic(anyString())).thenReturn(Collections.emptyList());
        // Act
        List<DetailStatisticDTO> result = sessionService.detailStatistic(getId(), getId());
        // Assert
        assertNotNull(result);
    }

    @Test
    public void testDetailWaiting() {
        sessionService.detailWaiting(getId(), getId());
    }

    @Test
    public void testDetailBlockTree() {
        sessionService.detailBlockTree(getId(), getId());
    }

    @Test
    public void testSimpleStatistic() {
        sessionService.simpleStatistic(getId());
    }

    @Test
    public void testLongTxc() {
        sessionService.longTxc(getId());
    }

    @Test
    public void testBlockAndLongTxc() {
        sessionService.blockAndLongTxc(getId());
    }

    @Test
    public void testDetail() {
        when(sessionMapper.generalMesList(anyString())).thenReturn(List.of(Map.of("1", 1)));
        when(sessionMapper.sessionIsWaiting(anyString())).thenReturn(1);
        sessionService.detail(getId(), getId());
    }
}