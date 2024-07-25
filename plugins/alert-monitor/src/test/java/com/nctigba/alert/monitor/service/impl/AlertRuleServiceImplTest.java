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
 *  AlertRuleServiceImplTest.java
 *
 *  IDENTIFICATION
 *  plugins/alert-monitor/src/test/java/com/nctigba/alert/monitor/service/impl/AlertRuleServiceImplTest.java
 *
 *  -------------------------------------------------------------------------
 */

package com.nctigba.alert.monitor.service.impl;

import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.nctigba.alert.monitor.mapper.AlertRuleItemExpSrcMapper;
import com.nctigba.alert.monitor.mapper.AlertRuleItemMapper;
import com.nctigba.alert.monitor.mapper.AlertRuleItemSrcMapper;
import com.nctigba.alert.monitor.mapper.AlertRuleMapper;
import com.nctigba.alert.monitor.model.dto.AlertRuleParamDTO;
import com.nctigba.alert.monitor.model.entity.AlertRuleDO;
import com.nctigba.alert.monitor.model.entity.AlertRuleItemDO;
import com.nctigba.alert.monitor.model.entity.AlertRuleItemExpSrcDO;
import com.nctigba.alert.monitor.model.entity.AlertRuleItemSrcDO;
import com.nctigba.alert.monitor.model.query.RuleQuery;
import com.nctigba.alert.monitor.service.AlertRuleItemService;
import com.nctigba.alert.monitor.util.MessageSourceUtils;
import org.apache.ibatis.builder.MapperBuilderAssistant;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.opengauss.admin.common.exception.ServiceException;
import org.springframework.test.context.junit4.SpringRunner;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * test the function of the AlertRuleServiceImpl
 *
 * @since 2023/7/13 09:21
 */
@RunWith(SpringRunner.class)
public class AlertRuleServiceImplTest {
    @InjectMocks
    @Spy
    private AlertRuleServiceImpl alertRuleService;
    @Mock
    private AlertRuleItemMapper alertRuleItemMapper;
    @Mock
    private AlertRuleMapper baseMapper;
    @Mock
    private AlertRuleItemSrcMapper ruleItemSrcMapper;
    @Mock
    private AlertRuleItemExpSrcMapper ruleItemExpSrcMapper;
    @Mock
    private AlertRuleItemService ruleItemService;


    @Before
    public void before() {
        MockitoAnnotations.initMocks(this);
        TableInfoHelper.initTableInfo(new MapperBuilderAssistant(new MybatisConfiguration(), ""),
            AlertRuleDO.class);
        TableInfoHelper.initTableInfo(new MapperBuilderAssistant(new MybatisConfiguration(), ""),
            AlertRuleItemDO.class);
    }

    @Test
    public void testGetRulePageNull() {
        Page page = new Page(1, 10);
        Page<AlertRuleDO> alertRulePage = new Page<>(1, 10);
        when(baseMapper.selectPage(eq(page), any())).thenReturn(alertRulePage);

        RuleQuery ruleQuery = new RuleQuery();
        Page<AlertRuleDO> ruleDtoPage = alertRuleService.getRulePage(ruleQuery, page);
        verify(baseMapper, times(1)).selectPage(eq(page), any());
        assertEquals(0L, ruleDtoPage.getTotal());
    }

    @Test
    public void testGetRulePage() {
        try (MockedStatic<MessageSourceUtils> mockedStatic = mockStatic(MessageSourceUtils.class)) {
            Page page = new Page(1, 10);
            AlertRuleDO alertRuleDO = new AlertRuleDO().setId(1L);
            List<AlertRuleDO> alertRuleDOList = new ArrayList<>();
            alertRuleDOList.add(alertRuleDO);
            Page<AlertRuleDO> alertRulePage = new Page<>(1, 10);
            alertRulePage.setRecords(alertRuleDOList).setTotal(1);
            when(baseMapper.selectPage(eq(page), any())).thenReturn(alertRulePage);

            AlertRuleItemDO ruleItem = new AlertRuleItemDO().setId(1L).setRuleId(1L).setAction("normal")
                .setOperate(">=").setLimitValue(new BigDecimal(50)).setRuleMark("A").setRuleExpName("cpu").setUnit("%");
            List<AlertRuleItemDO> alertRuleItemDOS = new ArrayList<>();
            alertRuleItemDOS.add(ruleItem);
            when(alertRuleItemMapper.selectList(any())).thenReturn(alertRuleItemDOS);

            RuleQuery ruleQuery = new RuleQuery();
            Page<AlertRuleDO> ruleDtoPage = alertRuleService.getRulePage(ruleQuery, page);

            verify(baseMapper, times(1)).selectPage(eq(page), any());
            verify(alertRuleItemMapper, times(1)).selectList(any());
            assertEquals(1L, ruleDtoPage.getTotal());
        }
    }

    @Test(expected = ServiceException.class)
    public void testGetRuleByIdThrowException() {
        AlertRuleDO alertRuleDO = null;
        when(baseMapper.selectById(anyLong())).thenReturn(alertRuleDO);
        alertRuleService.getRuleById(anyLong());
    }

    @Test
    public void testGetRuleById() {
        AlertRuleDO alertRuleDO = new AlertRuleDO().setId(1L).setRuleName("ruleName");
        when(baseMapper.selectById(anyLong())).thenReturn(alertRuleDO);
        List<AlertRuleItemDO> alertRuleItemDOS = new ArrayList<>();
        AlertRuleItemDO alertRuleItemDO = new AlertRuleItemDO().setId(1L).setRuleId(1L);
        alertRuleItemDOS.add(alertRuleItemDO);
        when(alertRuleItemMapper.selectList(any())).thenReturn(alertRuleItemDOS);

        AlertRuleDO result = alertRuleService.getRuleById(anyLong());

        verify(baseMapper, times(1)).selectById(anyLong());
        verify(alertRuleItemMapper, times(1)).selectList(any());
        assertEquals(alertRuleDO.getId(), result.getId());
    }

    @Test
    public void testGetRuleList() {
        List<AlertRuleDO> alertRuleDOS = new ArrayList<>();
        AlertRuleDO alertRuleDO = new AlertRuleDO().setId(1L);
        alertRuleDOS.add(alertRuleDO);
        when(baseMapper.selectList(any())).thenReturn(alertRuleDOS);

        AlertRuleItemDO ruleItem1 = new AlertRuleItemDO().setId(1L).setRuleId(1L).setAction("normal")
            .setOperate(">=").setLimitValue(new BigDecimal(50)).setRuleMark("A").setRuleExpName("cpu").setUnit("%");
        List<AlertRuleItemDO> alertRuleItemDOS = new ArrayList<>();
        alertRuleItemDOS.add(ruleItem1);
        when(alertRuleItemMapper.selectList(any())).thenReturn(alertRuleItemDOS);

        List<AlertRuleDO> ruleList = alertRuleService.getRuleList(Arrays.asList("index", "log"));

        verify(baseMapper, times(1)).selectList(any());
        verify(alertRuleItemMapper, times(1)).selectList(any());
        assertEquals(alertRuleDOS.size(), ruleList.size());
    }

    @Test
    public void testGetRuleItemSrcList() {
        List<AlertRuleItemSrcDO> list = new ArrayList<>();
        when(ruleItemSrcMapper.selectList(any())).thenReturn(list);
        List<AlertRuleItemSrcDO> ruleItemSrcList = alertRuleService.getRuleItemSrcList();
        verify(ruleItemSrcMapper, times(1)).selectList(any());
        assertEquals(list, ruleItemSrcList);
    }

    @Test
    public void testGetRuleItemExpSrcListByRuleItemSrcId() {
        List<AlertRuleItemExpSrcDO> list = new ArrayList<>();
        when(ruleItemExpSrcMapper.selectList(any())).thenReturn(list);
        List<AlertRuleItemExpSrcDO> result = alertRuleService.getRuleItemExpSrcListByRuleItemSrcId(anyLong());
        verify(ruleItemExpSrcMapper, times(1)).selectList(any());
        assertEquals(list, result);
    }

    @Test
    public void testSaveRuleWithoutDel() {
        when(baseMapper.insert(any())).thenReturn(1);
        List<AlertRuleItemDO> delItemList = new ArrayList<>();
        when(ruleItemService.list(any())).thenReturn(delItemList);
        when(ruleItemService.saveOrUpdateBatch(anyList())).thenReturn(true);

        AlertRuleItemDO ruleItem = new AlertRuleItemDO();
        List<AlertRuleItemDO> ruleItemList = new ArrayList<>();
        ruleItemList.add(ruleItem);
        AlertRuleParamDTO alertRule = new AlertRuleParamDTO().setAlertRuleItemList(ruleItemList);
        alertRuleService.saveRule(alertRule);

        verify(baseMapper, times(1)).insert(any());
        verify(ruleItemService, times(1)).list(any());
        verify(ruleItemService, times(1)).saveOrUpdateBatch(anyList());
    }

    @Test
    public void testSaveRuleWithDel() {
        AlertRuleItemDO alertRuleItemDO = new AlertRuleItemDO();
        List<AlertRuleItemDO> delItemList = new ArrayList<>();
        delItemList.add(alertRuleItemDO);
        when(ruleItemService.list(any())).thenReturn(delItemList);
        when(ruleItemService.update(any())).thenReturn(true);
        when(ruleItemService.saveOrUpdateBatch(anyList())).thenReturn(true);

        AlertRuleItemDO ruleItem = new AlertRuleItemDO();
        List<AlertRuleItemDO> ruleItemList = new ArrayList<>();
        ruleItemList.add(ruleItem);
        AlertRuleParamDTO alertRule = new AlertRuleParamDTO().setAlertRuleItemList(ruleItemList).setId(1L);
        alertRuleService.saveRule(alertRule);

        verify(ruleItemService, times(1)).list(any());
        verify(ruleItemService, times(1)).update(any());
        verify(ruleItemService, times(1)).saveOrUpdateBatch(anyList());
    }

    @Test
    public void testDelRuleById() {
        when(baseMapper.update(any(), any())).thenReturn(1);
        when(ruleItemService.update(any())).thenReturn(true);
        alertRuleService.delRuleById(anyLong());
        verify(baseMapper, times(1)).update(any(), any());
        verify(ruleItemService, times(1)).update(any());
    }
}
