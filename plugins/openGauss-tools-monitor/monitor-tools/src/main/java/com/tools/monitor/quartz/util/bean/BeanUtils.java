package com.tools.monitor.quartz.util.bean;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * BeanUtils
 *
 * @author liu
 * @since 2022-10-01
 */
public class BeanUtils extends org.springframework.beans.BeanUtils {
    private static final int BEAN_METHOD_PROP_INDEX = 3;

    private static final Pattern GET_PATTERN = Pattern.compile("get(\\p{javaUpperCase}\\w*)");

    private static final Pattern SET_PATTERN = Pattern.compile("set(\\p{javaUpperCase}\\w*)");

    /**
     * copyBeanProp
     *
     * @param dest
     * @param src
     */
    public static void copyBeanProp(Object dest, Object src) {
        try {
            copyProperties(src, dest);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * getSetterMethods
     *
     * @param obj
     * @return
     */
    public static List<Method> getSetterMethods(Object obj) {
        List<Method> setterMethods = new ArrayList<Method>();
        Method[] methods = obj.getClass().getMethods();
        for (Method method : methods) {
            Matcher m = SET_PATTERN.matcher(method.getName());
            if (m.matches() && (method.getParameterTypes().length == 1)) {
                setterMethods.add(method);
            }
        }
        return setterMethods;
    }

    /**
     * getGetterMethods
     *
     * @param obj
     * @return
     */
    public static List<Method> getGetterMethods(Object obj) {
        List<Method> getterMethods = new ArrayList<Method>();
        Method[] methods = obj.getClass().getMethods();
        for (Method method : methods) {
            Matcher m = GET_PATTERN.matcher(method.getName());
            if (m.matches() && (method.getParameterTypes().length == 0)) {
                getterMethods.add(method);
            }
        }
        return getterMethods;
    }

    /**
     * isMethodPropEquals
     *
     * @param m1
     * @param m2
     * @return
     */
    public static boolean isMethodPropEquals(String m1, String m2) {
        return m1.substring(BEAN_METHOD_PROP_INDEX).equals(m2.substring(BEAN_METHOD_PROP_INDEX));
    }
}
