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
 *  AlertTemplateRuleServiceImplTest.java
 *
 *  IDENTIFICATION
 *  plugins/alert-monitor/src/test/java/com/nctigba/alert/monitor/service/impl/AlertTemplateRuleServiceImplTest.java
 *
 *  -------------------------------------------------------------------------
 */

package com.nctigba.alert.monitor.service.impl;

import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.nctigba.alert.monitor.constant.CommonConstants;
import com.nctigba.alert.monitor.model.entity.AlertTemplateRuleDO;
import com.nctigba.alert.monitor.model.entity.AlertTemplateRuleItemDO;
import com.nctigba.alert.monitor.mapper.AlertTemplateRuleItemMapper;
import com.nctigba.alert.monitor.mapper.AlertTemplateRuleMapper;
import com.nctigba.alert.monitor.service.AlertTemplateRuleItemService;
import org.apache.ibatis.builder.MapperBuilderAssistant;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.opengauss.admin.common.exception.ServiceException;
import org.springframework.test.context.junit4.SpringRunner;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * test the function of the AlertTemplateRuleServiceImpl
 *
 * @since 2023/7/13 16:40
 */
@RunWith(SpringRunner.class)
public class AlertTemplateRuleServiceImplTest {
    @InjectMocks
    @Spy
    private AlertTemplateRuleServiceImpl alertTemplateRuleService;
    @Mock
    private AlertTemplateRuleItemMapper alertTemplateRuleItemMapper;
    @Mock
    private AlertTemplateRuleItemService templateRuleItemService;
    @Mock
    private AlertTemplateRuleMapper baseMapper;
    @Mock
    private PrometheusServiceImpl prometheusService;

    @Before
    public void before() {
        MockitoAnnotations.initMocks(this);
        TableInfoHelper.initTableInfo(new MapperBuilderAssistant(new MybatisConfiguration(), ""),
            AlertTemplateRuleDO.class);
    }

    @Test(expected = ServiceException.class)
    public void testGetTemplateRuleThrowException() {
        AlertTemplateRuleDO alertTemplateRuleDO = null;
        when(baseMapper.selectById(anyLong())).thenReturn(alertTemplateRuleDO);
        alertTemplateRuleService.getTemplateRule(anyLong());
        verify(baseMapper, times(1)).selectById(anyLong());
    }

    @Test
    public void testGetTemplateRule() {
        AlertTemplateRuleDO alertTemplateRuleDO = new AlertTemplateRuleDO().setTemplateId(1L).setId(1L).setRuleId(1L);
        when(baseMapper.selectById(anyLong())).thenReturn(alertTemplateRuleDO);

        List<AlertTemplateRuleItemDO> alertTemplateRuleItemDOS = new ArrayList<>();
        AlertTemplateRuleItemDO ruleItem = new AlertTemplateRuleItemDO().setId(1L).setTemplateRuleId(1L);
        alertTemplateRuleItemDOS.add(ruleItem);
        when(alertTemplateRuleItemMapper.selectList(any())).thenReturn(alertTemplateRuleItemDOS);

        AlertTemplateRuleDO templateRule = alertTemplateRuleService.getTemplateRule(anyLong());

        verify(baseMapper, times(1)).selectById(anyLong());
        verify(alertTemplateRuleItemMapper, times(1)).selectList(any());
        assertEquals(alertTemplateRuleDO.getId(), templateRule.getId());
    }

    @Test
    public void testSaveIndexTemplateRule() {
        AlertTemplateRuleItemDO ruleItem = new AlertTemplateRuleItemDO().setId(1L).setTemplateRuleId(1L)
            .setAction("normal").setOperate(">=").setLimitValue(new BigDecimal(50)).setRuleMark("A").setRuleExpName("cpu")
            .setUnit("%");
        AlertTemplateRuleItemDO ruleItem2 = new AlertTemplateRuleItemDO().setTemplateRuleId(1L).setAction(
            "normal").setOperate(">=").setLimitValue(new BigDecimal(50)).setRuleMark("B").setRuleExpName("cpu").setUnit("");
        AlertTemplateRuleItemDO ruleItem3 = new AlertTemplateRuleItemDO().setId(3L).setTemplateRuleId(1L).setAction(
            "normal").setOperate(">=").setLimitValue(new BigDecimal(50)).setRuleMark("C").setRuleExpName("cpu").setUnit("");
        List<AlertTemplateRuleItemDO> templateRuleItemList = new ArrayList<>();
        templateRuleItemList.add(ruleItem);
        templateRuleItemList.add(ruleItem2);
        templateRuleItemList.add(ruleItem3);
        AlertTemplateRuleDO alertTemplateRuleDO = new AlertTemplateRuleDO().setRuleType(CommonConstants.INDEX_RULE)
            .setTemplateId(1L);
        alertTemplateRuleDO.setAlertRuleItemList(templateRuleItemList);

        when(baseMapper.insert(alertTemplateRuleDO)).thenReturn(1);
        when(templateRuleItemService.saveOrUpdateBatch(templateRuleItemList)).thenReturn(true);
        doNothing().when(prometheusService).updateRuleByTemplateRule(alertTemplateRuleDO);

        AlertTemplateRuleDO alertTemplateRuleDO0 = alertTemplateRuleService.saveTemplateRule(alertTemplateRuleDO);

        verify(baseMapper, times(1)).insert(alertTemplateRuleDO);
        verify(templateRuleItemService, times(1)).saveOrUpdateBatch(templateRuleItemList);
        verify(prometheusService, times(1)).updateRuleByTemplateRule(any());
        assertEquals(3, alertTemplateRuleDO0.getAlertRuleItemList().size());
    }

    @Test
    public void testSaveIndexTemplateRuleWithoutTemplateId() {
        AlertTemplateRuleItemDO ruleItem = new AlertTemplateRuleItemDO().setId(1L).setTemplateRuleId(1L)
            .setAction("normal").setOperate(">=").setLimitValue(new BigDecimal(50)).setRuleMark("A").setRuleExpName("cpu")
            .setUnit("%");
        AlertTemplateRuleItemDO ruleItem2 = new AlertTemplateRuleItemDO().setTemplateRuleId(1L).setAction(
            "normal").setOperate(">=").setLimitValue(new BigDecimal(50)).setRuleMark("B").setRuleExpName("cpu").setUnit("");
        AlertTemplateRuleItemDO ruleItem3 = new AlertTemplateRuleItemDO().setId(3L).setTemplateRuleId(1L).setAction(
            "normal").setOperate(">=").setLimitValue(new BigDecimal(50)).setRuleMark("C").setRuleExpName("cpu").setUnit("");
        List<AlertTemplateRuleItemDO> templateRuleItemList = new ArrayList<>();
        templateRuleItemList.add(ruleItem);
        templateRuleItemList.add(ruleItem2);
        templateRuleItemList.add(ruleItem3);
        AlertTemplateRuleDO alertTemplateRuleDO = new AlertTemplateRuleDO().setRuleType(CommonConstants.INDEX_RULE)
            .setId(1L);
        alertTemplateRuleDO.setAlertRuleItemList(templateRuleItemList);

        when(baseMapper.selectById(alertTemplateRuleDO.getId())).thenReturn(alertTemplateRuleDO);
        when(baseMapper.updateById(alertTemplateRuleDO)).thenReturn(1);
        when(templateRuleItemService.saveOrUpdateBatch(templateRuleItemList)).thenReturn(true);

        AlertTemplateRuleDO alertTemplateRuleDO0 = alertTemplateRuleService.saveTemplateRule(alertTemplateRuleDO);

        verify(baseMapper, times(1)).selectById(alertTemplateRuleDO.getId());
        verify(baseMapper, times(1)).updateById(alertTemplateRuleDO);
        verify(templateRuleItemService, times(1)).saveOrUpdateBatch(templateRuleItemList);
        assertEquals(3, alertTemplateRuleDO0.getAlertRuleItemList().size());
    }

    @Test
    public void testSaveLogTemplateRule() {
        AlertTemplateRuleItemDO ruleItem = new AlertTemplateRuleItemDO().setId(1L).setTemplateRuleId(1L);
        AlertTemplateRuleItemDO ruleItem2 = new AlertTemplateRuleItemDO().setTemplateRuleId(1L).setAction("normal");
        AlertTemplateRuleItemDO ruleItem3 =
            new AlertTemplateRuleItemDO().setId(3L).setTemplateRuleId(1L).setRuleMark("C");
        List<AlertTemplateRuleItemDO> templateRuleItemList = new ArrayList<>();
        templateRuleItemList.add(ruleItem);
        templateRuleItemList.add(ruleItem2);
        templateRuleItemList.add(ruleItem3);
        AlertTemplateRuleDO alertTemplateRuleDO = new AlertTemplateRuleDO().setRuleType(CommonConstants.LOG_RULE)
            .setTemplateId(1L);
        alertTemplateRuleDO.setAlertRuleItemList(templateRuleItemList);

        when(baseMapper.insert(alertTemplateRuleDO)).thenReturn(1);
        when(templateRuleItemService.saveOrUpdateBatch(templateRuleItemList)).thenReturn(true);

        AlertTemplateRuleDO alertTemplateRuleDO0 = alertTemplateRuleService.saveTemplateRule(alertTemplateRuleDO);

        verify(baseMapper, times(1)).insert(alertTemplateRuleDO);
        verify(templateRuleItemService, times(1)).saveOrUpdateBatch(templateRuleItemList);
        assertEquals(3, alertTemplateRuleDO0.getAlertRuleItemList().size());
    }

    @Test
    public void testGetListByTemplateId() {
        List<AlertTemplateRuleDO> alertRules = new ArrayList<>();
        AlertTemplateRuleDO alertTemplateRuleDO = new AlertTemplateRuleDO().setId(1L);
        alertRules.add(alertTemplateRuleDO);
        when(baseMapper.selectList(any())).thenReturn(alertRules);

        List<AlertTemplateRuleItemDO> alertRuleItems = new ArrayList<>();
        AlertTemplateRuleItemDO ruleItem1 = new AlertTemplateRuleItemDO().setId(1L).setTemplateRuleId(1L);
        AlertTemplateRuleItemDO ruleItem2 = new AlertTemplateRuleItemDO().setId(2L).setTemplateRuleId(1L);
        AlertTemplateRuleItemDO ruleItem3 = new AlertTemplateRuleItemDO().setId(3L).setTemplateRuleId(1L);
        AlertTemplateRuleItemDO ruleItem4 = new AlertTemplateRuleItemDO().setId(4L).setTemplateRuleId(1L);
        alertRuleItems.add(ruleItem1);
        alertRuleItems.add(ruleItem2);
        alertRuleItems.add(ruleItem3);
        alertRuleItems.add(ruleItem4);
        when(alertTemplateRuleItemMapper.selectList(any())).thenReturn(alertRuleItems);

        List<AlertTemplateRuleDO> list = alertTemplateRuleService.getListByTemplateId(anyLong());

        verify(baseMapper, times(1)).selectList(any());
        verify(alertTemplateRuleItemMapper, times(1)).selectList(any());
        assertEquals(1, list.size());
    }

    @Test
    public void testEnableIndexTemplateRule() {
        AlertTemplateRuleDO alertTemplateRuleDO =
            new AlertTemplateRuleDO().setId(1L).setRuleType(CommonConstants.INDEX_RULE);
        when(baseMapper.selectById(anyLong())).thenReturn(alertTemplateRuleDO);
        when(baseMapper.updateById(alertTemplateRuleDO)).thenReturn(1);
        doNothing().when(prometheusService).updateRuleByTemplateRule(alertTemplateRuleDO);

        alertTemplateRuleService.enableTemplateRule(anyLong());

        verify(baseMapper, times(1)).selectById(anyLong());
        verify(baseMapper, times(1)).updateById(any());
        verify(prometheusService, times(1)).updateRuleByTemplateRule(any());
    }

    @Test
    public void testEnableLogTemplateRule() {
        AlertTemplateRuleDO alertTemplateRuleDO =
            new AlertTemplateRuleDO().setId(1L).setRuleType(CommonConstants.LOG_RULE);
        when(baseMapper.selectById(anyLong())).thenReturn(alertTemplateRuleDO);
        when(baseMapper.updateById(alertTemplateRuleDO)).thenReturn(1);

        alertTemplateRuleService.enableTemplateRule(anyLong());

        verify(baseMapper, times(1)).selectById(anyLong());
        verify(baseMapper, times(1)).updateById(any());
    }

    @Test
    public void testDisableIndexTemplateRule() {
        AlertTemplateRuleDO alertTemplateRuleDO =
            new AlertTemplateRuleDO().setId(1L).setRuleType(CommonConstants.INDEX_RULE);
        when(baseMapper.selectById(anyLong())).thenReturn(alertTemplateRuleDO);
        when(baseMapper.updateById(alertTemplateRuleDO)).thenReturn(1);
        doNothing().when(prometheusService).updateRuleByTemplateRule(alertTemplateRuleDO);

        alertTemplateRuleService.disableTemplateRule(anyLong());

        verify(baseMapper, times(1)).selectById(anyLong());
        verify(baseMapper, times(1)).updateById(any());
        verify(prometheusService, times(1)).removeRuleByTemplateRule(any());
    }

    @Test
    public void testDisableLogTemplateRule() {
        AlertTemplateRuleDO alertTemplateRuleDO =
            new AlertTemplateRuleDO().setId(1L).setRuleType(CommonConstants.LOG_RULE);
        when(baseMapper.selectById(anyLong())).thenReturn(alertTemplateRuleDO);
        when(baseMapper.updateById(alertTemplateRuleDO)).thenReturn(1);

        alertTemplateRuleService.disableTemplateRule(anyLong());

        verify(baseMapper, times(1)).selectById(anyLong());
        verify(baseMapper, times(1)).updateById(any());
    }
}
