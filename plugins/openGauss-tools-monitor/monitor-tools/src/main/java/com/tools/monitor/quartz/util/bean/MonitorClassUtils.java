/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2012-2022. All rights reserved.
 */

package com.tools.monitor.quartz.util.bean;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;

/**
 * MonitorClassUtils
 *
 * @author liu
 * @since 2022-10-01
 */
@Slf4j
public class MonitorClassUtils extends org.springframework.beans.BeanUtils {
    /**
     * attributeCopy。
     *
     * @param tar tar
     * @param source  source
     */
    public static void attributeCopy(Object tar, Object source) {
        try {
            copyProperties(source, tar);
        } catch (BeansException exception) {
            log.error("attributeCopy-->{}", exception.getMessage());
        }
    }
}
