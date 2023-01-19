/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2012-2022. All rights reserved.
 */

package com.tools.monitor.common.core.controller;

import com.tools.monitor.entity.MonitorResult;
import com.tools.monitor.util.MonitorDateUtils;
import java.beans.PropertyEditorSupport;
import java.util.Date;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;

/**
 * BasicController
 *
 * @author liu
 * @since 2022-10-01
 */
public class BasicController {
    /**
     * initBinder
     *
     * @param binder binder
     */
    @InitBinder
    public void initBinder(WebDataBinder binder) {
        binder.registerCustomEditor(Date.class, new PropertyEditorSupport() {
            @Override
            public void setAsText(String text) {
                setValue(MonitorDateUtils.MonitorDate(text));
            }
        });
    }

    /**
     * success
     *
     * @return MonitorResult MonitorResult
     */
    public MonitorResult success() {
        return MonitorResult.success();
    }

    /**
     * error
     *
     * @return MonitorResult
     */
    public MonitorResult error() {
        return MonitorResult.error();
    }

    /**
     * success
     *
     * @param message message
     * @return MonitorResult MonitorResult
     */
    public MonitorResult success(String message) {
        return MonitorResult.success(message);
    }

    /**
     * error
     *
     * @param message message
     * @return MonitorResult MonitorResult
     */
    public MonitorResult error(String message) {
        return MonitorResult.error(message);
    }

    /**
     * toAjax
     *
     * @param rows rows
     * @return MonitorResult
     */
    protected MonitorResult toAjax(int rows) {
        return rows > 0 ? MonitorResult.success() : MonitorResult.error();
    }

    /**
     * MonitorResult
     *
     * @param isResult isResult
     * @return MonitorResult
     */
    protected MonitorResult toAjax(boolean isResult) {
        return isResult ? success() : error();
    }
}
