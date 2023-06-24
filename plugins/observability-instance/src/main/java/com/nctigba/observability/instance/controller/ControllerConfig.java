/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */
package com.nctigba.observability.instance.controller;

import org.springframework.beans.propertyeditors.CustomDateEditor;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;

import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ControllerConfig {
    @InitBinder
    public void initBinder(WebDataBinder webDataBinder) {
        webDataBinder.registerCustomEditor(Date.class,
                new CustomDateEditor(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'") {
                    private static final long serialVersionUID = 1L;

                    @Override
                    public Date parse(String text, ParsePosition pos) {
                        if (org.apache.commons.lang3.StringUtils.isBlank(text))
                            return null;
                        return super.parse(text, pos);
                    }
                }, true));
    }
}