/*
 * Copyright (c) 2022 Huawei Technologies Co.,Ltd.
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
 * BaseController.java
 *
 * IDENTIFICATION
 * openGauss-visualtool/visualtool-common/src/main/java/org/opengauss/admin/common/core/controller/BaseController.java
 *
 * -------------------------------------------------------------------------
 */


package org.opengauss.admin.common.core.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.metadata.OrderItem;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.opengauss.admin.common.constant.SqlConstants;
import org.opengauss.admin.common.core.domain.AjaxResult;
import org.opengauss.admin.common.core.domain.model.LoginUser;
import org.opengauss.admin.common.core.page.PageDomain;
import org.opengauss.admin.common.core.page.TableDataInfo;
import org.opengauss.admin.common.core.page.TableSupport;
import org.opengauss.admin.common.enums.ResponseCode;
import org.opengauss.admin.common.utils.DateUtils;
import org.opengauss.admin.common.utils.SecurityUtils;
import org.opengauss.admin.common.utils.StringUtils;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;

import java.beans.PropertyEditorSupport;
import java.util.Date;

/**
 * base controller
 *
 * @author xielibo
 */
public class BaseController {

    /**
     * Automatically convert the date format string passed from the front desk to Date type
     */
    @InitBinder
    public void initBinder(WebDataBinder binder) {
        binder.registerCustomEditor(Date.class, new PropertyEditorSupport() {
            @Override
            public void setAsText(String text) {
                setValue(DateUtils.parseDate(text));
            }
        });
    }

    /**
     * Set request pagination params
     */
    protected Page startPage() {
        Page page = new Page<>();
        page.setSize(15);
        PageDomain pageDomain = TableSupport.buildPageRequest();
        Integer pageNum = pageDomain.getPageNum();
        Integer pageSize = pageDomain.getPageSize();
        String orderBy = pageDomain.getOrderBy();
        String orderColumn = pageDomain.getOrderByColumn();
        String isAsc = pageDomain.getIsAsc();

        if (StringUtils.isNotNull(pageNum) && StringUtils.isNotNull(pageSize)) {
            page.setCurrent(pageNum);
            page.setSize(pageSize);
            page.setOptimizeCountSql(false);
            page.setMaxLimit(500L);
        }

        if (StringUtils.isNotBlank(orderColumn)) {
            if (SqlConstants.ASC.equalsIgnoreCase(isAsc)) {
                page.addOrder(OrderItem.asc(orderColumn));
            } else {
                page.addOrder(OrderItem.desc(orderColumn));
            }
        }

        return page;
    }

    /**
     * Set request pagination params
     */
    protected Page startPage(Integer pageNum, Integer size, String orderColumn, String order) {
        Page page = new Page();
        page.setCurrent(pageNum);
        page.setSize(size);
        page.setOptimizeCountSql(false);
        page.setMaxLimit(500L);

        if (StringUtils.isNotBlank(orderColumn)) {
            if (SqlConstants.ASC.equalsIgnoreCase(order)) {
                page.addOrder(OrderItem.asc(orderColumn));
            } else {
                page.addOrder(OrderItem.desc(orderColumn));
            }
        }
        return page;
    }

    /**
     * Set response pagination params
     *
     * @param page
     */
    protected TableDataInfo getDataTable(IPage<?> page) {
        TableDataInfo rspData = new TableDataInfo();
        rspData.setCode(ResponseCode.SUCCESS.code());
        rspData.setMsg(ResponseCode.SUCCESS.msg());
        rspData.setRows(page.getRecords());
        rspData.setTotal(page.getTotal());
        return rspData;
    }

    /**
     * response result
     */
    protected AjaxResult toAjax(int rows) {
        return rows > 0 ? AjaxResult.success() : AjaxResult.error();
    }

    /**
     * response result
     */
    protected AjaxResult toAjax(boolean result) {
        return result ? success() : error();
    }

    /**
     * response success result
     */
    public AjaxResult success() {
        return AjaxResult.success();
    }

    /**
     * response error result
     */
    public AjaxResult error() {
        return AjaxResult.error();
    }

    /**
     * response success result
     */
    public AjaxResult success(String message) {
        return AjaxResult.success(message);
    }

    /**
     * response error result
     */
    public AjaxResult error(String message) {
        return AjaxResult.error(message);
    }


    /**
     * Get the currently logged in user
     */
    public LoginUser getLoginUser() {
        return SecurityUtils.getLoginUser();
    }

    /**
     * Get the currently logged in userId
     */
    public Integer getUserId() {
        return getLoginUser().getUser().getUserId();
    }

    /**
     * Get the currently logged in username
     */
    public String getUsername() {
        return getLoginUser().getUsername();
    }
}
