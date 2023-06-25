/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package org.opengauss.plugin.alertcenter.constant;

import java.io.File;

/**
 * @author wuyuebin
 * @date 2023/4/27 17:49
 * @description
 */
public class CommonConstants {
    public static final String PROMETHEUS_YML = "/prometheus.yml";
    public static final Integer IS_DELETE = 1;
    public static final Integer IS_NOT_DELETE = 0;
    public static final Integer UNREAD_STATUS = 0;
    public static final Integer READ_STATUS = 1;
    public static final Integer FIRING_STATUS = 0;
    public static final Integer RECOVER_STATUS = 1;
    public static final String SERIOUS = "serious";
    public static final String WARN = "warn";
    public static final String INFO = "info";
    public static final String DELIMITER = ",";
    public static final Integer IS_NOT_REPEAT = 0;
    public static final Integer IS_REPEAT = 1;
    public static final Integer IS_NOT_SILENCE = 0;
    public static final Integer IS_SILENCE = 1;
    public static final String EMAIL = "email";
    public static final String WE_COM = "WeCom";
    public static final String DING_TALK = "DingTalk";
    public static final Integer ENABLE = 1;
    public static final Integer DISABLE = 0;
    //dev use "/"
    public static final String SLASH = File.separator;
    public static final String LINE_SEPARATOR = System.lineSeparator();
}
