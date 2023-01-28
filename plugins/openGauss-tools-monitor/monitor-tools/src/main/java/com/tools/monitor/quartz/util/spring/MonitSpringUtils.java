/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2012-2022. All rights reserved.
 */

package com.tools.monitor.quartz.util.spring;

import com.tools.monitor.exception.ParamsException;
import com.tools.monitor.util.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.aop.framework.AopContext;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

/**
 * MonitSpringUtils
 *
 * @author liu
 * @since 2022-10-01
 */
@Slf4j
@Component
public final class MonitSpringUtils implements BeanFactoryPostProcessor, ApplicationContextAware {
    /**
     * Spring application context
     */
    private static ConfigurableListableBeanFactory configurableListableBeanFactory;

    private static ApplicationContext monitorContext;

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory configurableListableBeanFactory) throws BeansException {
        MonitSpringUtils.configurableListableBeanFactory = configurableListableBeanFactory;
    }

    @Override
    public void setApplicationContext(ApplicationContext context) throws BeansException {
        MonitSpringUtils.monitorContext = context;
    }

    /**
     * getStr
     *
     * @param str str
     * @return Object
     * @throws BeansException BeansException
     */
    @SuppressWarnings("unchecked")
    public static <T> T getStr(String str) {
        try {
            return (T) configurableListableBeanFactory.getBean(str);
        } catch (BeansException exception) {
            throw new ParamsException(exception.getMessage());
        }
    }

    /**
     * getClass
     *
     * @param monitorClass monitorClass
     * @return t
     * @throws BeansException BeansException
     */
    public static <T> T getClass(Class<T> monitorClass) {
        try {
            return (T) configurableListableBeanFactory.getBean(monitorClass);
        } catch (BeansException exception) {
            throw new ParamsException(exception.getMessage());
        }
    }

    /**
     * monitorBean
     *
     * @param str str
     * @return boolean
     */
    public static boolean monitorBean(String str) {
        return configurableListableBeanFactory.containsBean(str);
    }

    /**
     * isSingleton
     *
     * @param str str
     * @return boolean
     */
    public static boolean isMonitorSingleton(String str) {
        return configurableListableBeanFactory.isSingleton(str);
    }

    /**
     * getType
     *
     * @param name name
     * @return Class
     */
    public static Class<?> getMonitorType(String name) {
        return configurableListableBeanFactory.getType(name);
    }

    /**
     * getMonitorAliases
     *
     * @param name name
     * @return String[]
     */
    public static String[] getMonitorAliases(String name) {
        return configurableListableBeanFactory.getAliases(name);
    }

    /**
     * getAopProxy
     *
     * @param invoker invoker
     * @return t
     */
    @SuppressWarnings("unchecked")
    public static <T> T getAopProxy(T invoker) {
        return (T) AopContext.currentProxy();
    }

    /**
     * getMonitorProfiles
     *
     * @return String
     */
    public static String[] getMonitorProfiles() {
        return monitorContext.getEnvironment().getActiveProfiles();
    }

    /**
     * getMonitorProfile
     *
     * @return String
     */
    public static String getMonitorProfile() {
        final String[] activeProfiles = getMonitorProfiles();
        return StringUtils.isHaveSomething(activeProfiles) ? activeProfiles[0] : "";
    }

    /**
     * getMonitorProperty
     *
     * @param str str
     * @return String
     */
    public static String getMonitorProperty(String str) {
        return monitorContext.getEnvironment().getRequiredProperty(str);
    }
}
