/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2012-2022. All rights reserved.
 *
 * openGauss is licensed under Mulan PSL v2.
 * You can use this software according to the terms and conditions of the Mulan PSL v2.
 * You may obtain a copy of Mulan PSL v2 at:
 *
 * http://license.coscl.org.cn/MulanPSL2
 *
 * THIS SOFTWARE IS PROVIDED ON AN "AS IS" BASIS, WITHOUT WARRANTIES OF ANY KIND,
 * EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO NON-INFRINGEMENT,
 * MERCHANTABILITY OR FITFOR A PARTICULAR PURPOSE.
 * See the Mulan PSL v2 for more details.
 * -------------------------------------------------------------------------
 *
 *
 * -------------------------------------------------------------------------
 */

package org.opengauss.tun.utils;

import org.opengauss.admin.common.exception.ServiceException;
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
 * @since 2023-12-20
 */
@Component
public final class MonitSpringUtils implements BeanFactoryPostProcessor, ApplicationContextAware {
    private static ConfigurableListableBeanFactory configurableListableBeanFactory;

    private static ApplicationContext monitorContext;

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory configurableListableBeanFactory) {
        MonitSpringUtils.configurableListableBeanFactory = configurableListableBeanFactory;
    }

    @Override
    public void setApplicationContext(ApplicationContext context) throws BeansException {
        MonitSpringUtils.monitorContext = context;
    }

    /**
     * 获取对象
     *
     * @param str str
     * @return Object 一个以所给名字注册的bean的实例
     * @throws BeansException BeansException
     */
    @SuppressWarnings("unchecked")
    public static <T> T getStr(String str) {
        try {
            return (T) configurableListableBeanFactory.getBean(str);
        } catch (BeansException exception) {
            throw new ServiceException(exception.getMessage());
        }
    }

    /**
     * 获取类型为requiredType的对象
     *
     * @param monitorClass monitorClass
     * @return t
     * @throws BeansException BeansException
     */
    public static <T> T getClass(Class<T> monitorClass) {
        try {
            return (T) configurableListableBeanFactory.getBean(monitorClass);
        } catch (BeansException exception) {
            throw new ServiceException(exception.getMessage());
        }
    }

    /**
     * 如果BeanFactory包含一个与所给名称匹配的bean定义，则返回true
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
     * @return Class注册对象的类型
     */
    public static Class<?> getMonitorType(String name) {
        return configurableListableBeanFactory.getType(name);
    }

    /**
     * 如果给定的bean名字在bean定义中有别名，则返回这些别名
     *
     * @param name name
     * @return String[]
     */
    public static String[] getMonitorAliases(String name) {
        return configurableListableBeanFactory.getAliases(name);
    }

    /**
     * 获取aop代理对象
     *
     * @param invoker invoker
     * @return t
     */
    @SuppressWarnings("unchecked")
    public static <T> T getAopProxy(T invoker) {
        return (T) AopContext.currentProxy();
    }

    /**
     * 获取当前的环境配置，无配置返回null
     *
     * @return 当前的环境配置
     */
    public static String[] getMonitorProfiles() {
        return monitorContext.getEnvironment().getActiveProfiles();
    }


    /**
     * 获取配置文件中的值
     *
     * @param str 配置文件的key
     * @return 当前的配置文件的值
     */
    public static String getMonitorProperty(String str) {
        return monitorContext.getEnvironment().getRequiredProperty(str);
    }
}
