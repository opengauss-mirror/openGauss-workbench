/*
 * Copyright (c) 2026 Huawei Technologies Co., Ltd.
 * openGauss-workbench is licensed under Mulan PSL v2.
 * You can use this software according to the terms and conditions of the Mulan PSL v2.
 * You may obtain a copy of Mulan PSL v2 at:
 *          http://license.coscl.org.cn/MulanPSL2
 * THIS SOFTWARE IS PROVIDED ON AN "AS IS" BASIS, WITHOUT WARRANTIES OF ANY KIND,
 * EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO NON-INFRINGEMENT,
 * MERCHANTABILITY OR FIT FOR A PARTICULAR PURPOSE.
 * See the Mulan PSL v2 for more details.
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