/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2012-2022. All rights reserved.
 */

package com.tools.monitor.quartz.util;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;

import com.tools.monitor.quartz.domain.SysJob;
import com.tools.monitor.quartz.util.spring.MonitSpringUtils;
import com.tools.monitor.util.StringUtils;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import lombok.extern.slf4j.Slf4j;

/**
 * MonitorInvokeUtil
 *
 * @author liu
 * @since 2022-10-01
 */
@Slf4j
public class MonitorInvokeUtil {
    /**
     * invokeMethod
     *
     * @param sysJob sysJob
     */
    public static void invokeMethod(SysJob sysJob) {
        String invokeTarget = sysJob.getInvokeTarget();
        String beanName = getTargetName(invokeTarget);
        String methodName = getMonitorName(invokeTarget);
        List<Object[]> methodParams = getResulteParams(invokeTarget);
        try {
            if (!VerificationName(beanName)) {
                Object bean = MonitSpringUtils.getStr(beanName);
                invokeMethod(bean, methodName, methodParams);
            } else {
                Object bean = Class.forName(beanName).newInstance();
                invokeMethod(bean, methodName, methodParams);
            }
        } catch (IllegalAccessException | InstantiationException | ClassNotFoundException exception) {
            log.error("invokeMethod-->{}", exception.getMessage());
        }
    }


    private static void invokeMethod(Object object, String strName, List<Object[]> list) {
        try {
            if (CollectionUtil.isNotEmpty(list)) {
                Method monitorMethod = object.getClass().getDeclaredMethod(strName, getResultParamsType(list));
                monitorMethod.invoke(object, getResulteValue(list));
            } else {
                Method monitorMethod = object.getClass().getDeclaredMethod(strName);
                monitorMethod.invoke(object);
            }
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException exception) {
            log.error("invokeMethod-->{}", exception.getMessage());
        }
    }

    /**
     * isValidClassName
     *
     * @param str str
     * @return boolean
     */
    public static boolean VerificationName(String str) {
        if (StringUtils.countMatches(str, ".") > 1) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * getTargetName
     *
     * @param str str
     * @return String
     */
    public static String getTargetName(String str) {
        String name = StringUtils.substringBefore(str, "(");
        String resulte = StringUtils.substringBeforeLast(name, ".");
        if (StrUtil.isNotEmpty(resulte)) {
            return resulte;
        } else {
            return "";
        }
    }

    /**
     * getMethodName
     *
     * @param str str
     * @return String
     */
    public static String getMonitorName(String str) {
        String name = StringUtils.substringBefore(str, "(");
        String resulte = StringUtils.substringAfterLast(name, ".");
        if (StrUtil.isNotEmpty(resulte)) {
            return resulte;
        } else {
            return "";
        }
    }

    /**
     * getResulteParams
     *
     * @param target target
     * @return method
     */
    public static List<Object[]> getResulteParams(String target) {
        String resulte = StringUtils.substringBetween(target, "(", "$");
        if (StrUtil.isEmpty(resulte)) {
            return new ArrayList<>();
        }
        String[] resulterParams = resulte.split("#");
        List<Object[]> objects = new LinkedList<>();
        for (int i = 0; i < resulterParams.length; i++) {
            String finalStr = StringUtils.trimToEmpty(resulterParams[i]);
            if (StringUtils.startsWithAny(finalStr, "'", "\"")) {
                objects.add(new Object[]{StringUtils.substring(finalStr, 1, finalStr.length() - 1), String.class});
            } else if ("true".equalsIgnoreCase(finalStr) || "false".equalsIgnoreCase(finalStr)) {
                objects.add(new Object[]{Boolean.valueOf(finalStr), Boolean.class});
            } else if (StringUtils.endsWith(finalStr, "L")) {
                objects.add(new Object[]{Long.valueOf(StringUtils.substring(finalStr, 0, finalStr.length() - 1)), Long.class});
            } else if (StringUtils.endsWith(finalStr, "D")) {
                objects.add(new Object[]{Double.valueOf(StringUtils.substring(finalStr, 0, finalStr.length() - 1)), Double.class});
            } else {
                objects.add(new Object[]{Integer.valueOf(finalStr), Integer.class});
            }
        }
        return objects;
    }

    /**
     * getResultParamsType
     *
     * @param objects objects
     * @return Class
     */
    public static Class<?>[] getResultParamsType(List<Object[]> objects) {
        Class<?>[] resulte = new Class<?>[objects.size()];
        int num = 0;
        for (Object[] os : objects) {
            resulte[num] = (Class<?>) os[1];
            num++;
        }
        return resulte;
    }

    /**
     * getResulteValue
     *
     * @param objects objects
     * @return Object
     */
    public static Object[] getResulteValue(List<Object[]> objects) {
        Object[] resulte = new Object[objects.size()];
        int num = 0;
        for (Object[] os : objects) {
            resulte[num] = (Object) os[0];
            num++;
        }
        return resulte;
    }
}
