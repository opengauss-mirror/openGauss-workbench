/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package com.nctigba.observability.instance.listener;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.opengauss.admin.common.core.vo.MenuVo;
import org.opengauss.admin.system.plugin.facade.MenuFacade;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.event.ContextClosedEvent;

import com.gitee.starblues.spring.MainApplicationContext;
import com.gitee.starblues.spring.SpringBeanFactory;

/**
 * PluginListenerTest.java
 *
 * @since 2023年7月17日
 */
@ExtendWith(MockitoExtension.class)
class PluginListenerTest {
    @InjectMocks
    private PluginListener pluginListener;
    @Mock
    private ApplicationReadyEvent readyEvent;
    @Mock
    private ConfigurableApplicationContext applicationContext;
    @Mock
    private MainApplicationContext mainApplicationContext;
    @Mock
    private SpringBeanFactory beanFactory;
    @Mock
    private MenuFacade menuFacade;
    @Mock
    private ContextClosedEvent closedEvent;

    @SuppressWarnings("unchecked")
    @Test
    void test() {
        when(readyEvent.getApplicationContext()).thenReturn(applicationContext);
        when(applicationContext.getBean(any(Class.class))).thenReturn(mainApplicationContext);
        when(mainApplicationContext.getSpringBeanFactory()).thenReturn(beanFactory);
        when(beanFactory.getBean(MenuFacade.class)).thenReturn(menuFacade);
        var vo = new MenuVo();
        vo.setMenuId(1);
        when(menuFacade.savePluginMenu(anyString(), anyString(), anyString(), anyInt(), anyString())).thenReturn(vo);
        pluginListener.onApplicationEvent(readyEvent);
        when(closedEvent.getApplicationContext()).thenReturn(applicationContext);
        pluginListener.onApplicationEvent(closedEvent);
    }
}