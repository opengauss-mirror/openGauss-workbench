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
 *  AlertConfigServiceImplTest.java
 *
 *  IDENTIFICATION
 *  plugins/alert-monitor/src/test/java/com/nctigba/alert/monitor/service/impl/AlertConfigServiceImplTest.java
 *
 *  -------------------------------------------------------------------------
 */

package com.nctigba.alert.monitor.service.impl;

import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.nctigba.alert.monitor.model.entity.AlertConfigDO;
import com.nctigba.alert.monitor.mapper.AlertConfigMapper;
import org.apache.ibatis.builder.MapperBuilderAssistant;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * test the function of the AlertConfigServiceImpl
 *
 * @since 2023/7/12 23:24
 */
@RunWith(SpringRunner.class)
public class AlertConfigServiceImplTest {
    @InjectMocks
    @Spy
    private AlertConfigServiceImpl alertConfigService;
    @Mock
    private PrometheusServiceImpl prometheusService;

    @Mock
    private AlertConfigMapper baseMapper;


    @Before
    public void before() {
        MockitoAnnotations.initMocks(this);
        TableInfoHelper.initTableInfo(new MapperBuilderAssistant(new MybatisConfiguration(), ""),
            AlertConfigDO.class);
    }

    @Test
    public void testSaveAlertConf1() {
        AlertConfigDO alertConfigDO = new AlertConfigDO().setId(1L).setAlertIp("127.0.0.1").setAlertPort("80");
        List<AlertConfigDO> list = new ArrayList<>();
        list.add(alertConfigDO);
        when(baseMapper.selectList(any())).thenReturn(list);

        List<Long> ids = anyList();
        when(alertConfigService.removeBatchByIds(ids)).thenReturn(true);

        AlertConfigDO param = new AlertConfigDO().setAlertIp("127.0.0.1").setAlertPort("8080");
        doNothing().when(prometheusService).updatePrometheusConfig(param);

        alertConfigService.saveAlertConf(param);

        verify(alertConfigService, times(1)).list();
        verify(alertConfigService, times(1)).removeBatchByIds(ids);
        verify(alertConfigService, times(1)).saveOrUpdate(any());
        verify(prometheusService, times(1)).updatePrometheusConfig(param);
    }

    @Test
    public void testSaveAlertConf2() {
        List<AlertConfigDO> list = new ArrayList<>();
        when(baseMapper.selectList(any())).thenReturn(list);

        AlertConfigDO param = new AlertConfigDO().setAlertIp("127.0.0.1").setAlertPort("8080");
        doNothing().when(prometheusService).updatePrometheusConfig(param);

        alertConfigService.saveAlertConf(param);

        verify(alertConfigService, times(1)).list();
        verify(alertConfigService, times(1)).saveOrUpdate(any());
        verify(prometheusService, times(1)).updatePrometheusConfig(param);
    }
}
