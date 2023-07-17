/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package com.nctigba.alert.monitor.service.impl;

import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.nctigba.alert.monitor.entity.AlertTemplateRuleItem;
import org.apache.ibatis.builder.MapperBuilderAssistant;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.springframework.test.context.junit4.SpringRunner;

import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

/**
 * test the function of the AlertTemplateRuleItemServiceImpl
 *
 * @since 2023/7/13 15:03
 */
@RunWith(SpringRunner.class)
public class AlertTemplateRuleItemServiceImplTest {
    @InjectMocks
    @Spy
    private AlertTemplateRuleItemServiceImpl alertTemplateRuleItemService;

    @Before
    public void before() {
        MockitoAnnotations.initMocks(this);
        TableInfoHelper.initTableInfo(new MapperBuilderAssistant(new MybatisConfiguration(), ""),
            AlertTemplateRuleItem.class);
    }

    @Test
    public void testSaveOrUpdateList() {
        alertTemplateRuleItemService.saveOrUpdateList(anyList());
        verify(alertTemplateRuleItemService, times(1)).saveOrUpdateBatch(anyList());
    }
}
