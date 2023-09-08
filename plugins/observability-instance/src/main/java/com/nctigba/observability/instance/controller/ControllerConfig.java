/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package com.nctigba.observability.instance.controller;

import java.beans.PropertyEditorSupport;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;

import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;

import cn.hutool.core.util.StrUtil;

public class ControllerConfig {
    @InitBinder
    public void initBinder(WebDataBinder webDataBinder) {
        webDataBinder.registerCustomEditor(Date.class, new PropertyEditorSupport() {
            @Override
            public void setAsText(String text) {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssX");
                ZonedDateTime zonedDateTime = ZonedDateTime.parse(text, formatter);
                ZonedDateTime systemZonedDateTime = zonedDateTime.withZoneSameInstant(ZoneId.systemDefault());
                setValue(Date.from(systemZonedDateTime.toInstant()));
            }

            @Override
            public String getAsText() {
                var value = getValue();
                if (value instanceof Date) {
                    Date date = (Date) value;
                    var localDateTime = date.toInstant().atZone(ZoneId.systemDefault());
                    var zUtcDateTime = localDateTime.withZoneSameInstant(ZoneId.of("UTC"));
                    var formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'");
                    return zUtcDateTime.format(formatter);
                }
                return StrUtil.EMPTY;
            }
        });
    }
}