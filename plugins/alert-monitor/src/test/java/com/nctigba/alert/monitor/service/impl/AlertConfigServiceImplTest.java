/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package com.nctigba.alert.monitor.service.impl;

import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.nctigba.alert.monitor.entity.AlertConfig;
import com.nctigba.alert.monitor.mapper.AlertConfigMapper;
import com.nctigba.alert.monitor.service.PrometheusService;
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
    private PrometheusService prometheusService;

    @Mock
    private AlertConfigMapper baseMapper;


    @Before
    public void before() {
        MockitoAnnotations.initMocks(this);
        TableInfoHelper.initTableInfo(new MapperBuilderAssistant(new MybatisConfiguration(), ""),
            AlertConfig.class);
    }

    @Test
    public void testSaveAlertConf1() {
        AlertConfig alertConfig = new AlertConfig().setId(1L).setAlertIp("127.0.0.1").setAlertPort("80");
        List<AlertConfig> list = new ArrayList<>();
        list.add(alertConfig);
        when(baseMapper.selectList(any())).thenReturn(list);

        List<Long> ids = anyList();
        when(alertConfigService.removeBatchByIds(ids)).thenReturn(true);

        AlertConfig param = new AlertConfig().setAlertIp("127.0.0.1").setAlertPort("8080");
        doNothing().when(prometheusService).updateAlertConfig(param, list);

        alertConfigService.saveAlertConf(param);

        verify(alertConfigService, times(1)).list();
        verify(alertConfigService, times(1)).removeBatchByIds(ids);
        verify(alertConfigService, times(1)).saveOrUpdate(any());
        verify(prometheusService, times(1)).updateAlertConfig(param, list);
    }

    @Test
    public void testSaveAlertConf2() {
        List<AlertConfig> list = new ArrayList<>();
        when(baseMapper.selectList(any())).thenReturn(list);

        AlertConfig param = new AlertConfig().setAlertIp("127.0.0.1").setAlertPort("8080");
        doNothing().when(prometheusService).updateAlertConfig(param, list);

        alertConfigService.saveAlertConf(param);

        verify(alertConfigService, times(1)).list();
        verify(alertConfigService, times(1)).saveOrUpdate(any());
        verify(prometheusService, times(1)).updateAlertConfig(param, list);
    }
}
