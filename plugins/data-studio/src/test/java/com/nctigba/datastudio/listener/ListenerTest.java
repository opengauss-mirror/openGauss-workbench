/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package com.nctigba.datastudio.listener;

import com.gitee.starblues.spring.MainApplicationContext;
import com.gitee.starblues.spring.SpringBeanFactory;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.opengauss.admin.system.plugin.facade.MenuFacade;
import org.springframework.boot.context.event.ApplicationEnvironmentPreparedEvent;
import org.springframework.boot.context.event.ApplicationPreparedEvent;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.context.event.ContextRefreshedEvent;

import static org.mockito.Mockito.when;

/**
 * ListenerTest
 *
 * @since 2023-07-14
 */
@Slf4j
@RunWith(MockitoJUnitRunner.class)
public class ListenerTest {
    @InjectMocks
    private PluginListener listener;

    @Mock
    private ApplicationEnvironmentPreparedEvent environmentPreparedEvent;

    @Mock
    private ApplicationPreparedEvent preparedEvent;

    @Mock
    private ContextRefreshedEvent refreshedEvent;

    @Mock
    private ApplicationReadyEvent readyEvent;

    @Mock
    private ContextClosedEvent closedEvent;

    @Mock
    private ApplicationEvent event;

    @Mock
    private ConfigurableApplicationContext applicationContext;

    @Mock
    private MainApplicationContext context;

    @Mock
    private SpringBeanFactory factory;

    @Mock
    private MenuFacade menuFacade;

    @Test
    public void testEvent() {
        listener.onApplicationEvent(environmentPreparedEvent);
    }

    @Test
    public void testEvent1() {
        listener.onApplicationEvent(preparedEvent);
    }

    @Test
    public void testEvent2() {
        listener.onApplicationEvent(refreshedEvent);
    }

    @Test
    public void testEvent3() {
        when(readyEvent.getApplicationContext()).thenReturn(applicationContext);
        when(applicationContext.getBean(MainApplicationContext.class)).thenReturn(context);
        when(context.getSpringBeanFactory()).thenReturn(factory);
        when(factory.getBean(MenuFacade.class)).thenReturn(menuFacade);
        listener.onApplicationEvent(readyEvent);
    }

    @Test
    public void testEvent4() {
        when(closedEvent.getApplicationContext()).thenReturn(applicationContext);
        when(applicationContext.getBean(MainApplicationContext.class)).thenReturn(context);
        when(context.getSpringBeanFactory()).thenReturn(factory);
        when(factory.getBean(MenuFacade.class)).thenReturn(menuFacade);
        listener.onApplicationEvent(closedEvent);
    }

    @Test
    public void testEvent5() {
        listener.onApplicationEvent(event);
    }
}
