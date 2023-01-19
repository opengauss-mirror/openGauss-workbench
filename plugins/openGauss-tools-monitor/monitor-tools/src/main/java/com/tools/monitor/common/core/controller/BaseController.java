package com.tools.monitor.common.core.controller;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.tools.monitor.common.contant.HttpStatus;
import com.tools.monitor.common.core.page.PageDomain;
import com.tools.monitor.common.core.page.TableDataInfo;
import com.tools.monitor.common.core.page.TableSupport;
import com.tools.monitor.entity.AjaxResult;
import com.tools.monitor.util.DateUtils;
import com.tools.monitor.util.PageUtils;
import com.tools.monitor.util.SqlUtil;
import com.tools.monitor.util.StringUtils;
import java.beans.PropertyEditorSupport;
import java.util.Date;
import java.util.List;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;

/**
 * BaseController
 *
 * @author liu
 * @since 2022-10-01
 */
public class BaseController {
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
                setValue(DateUtils.parseDate(text));
            }
        });
    }

    /**
     * startPage
     */
    protected void startPage() {
        PageUtils.startPage();
    }

    /**
     * startOrderBy
     */
    protected void startOrderBy() {
        PageDomain pageDomain = TableSupport.buildPageRequest();
        if (StringUtils.isNotEmpty(pageDomain.getOrderBy())) {
            String orderBy = SqlUtil.escapeOrderBySql(pageDomain.getOrderBy());
            PageHelper.orderBy(orderBy);
        }
    }

    /**
     * clearPage
     */
    protected void clearPage() {
        PageUtils.clearPage();
    }

    /**
     * getDataTable
     *
     * @param list list
     * @return TableDataInfo
     */
    @SuppressWarnings({"rawtypes", "unchecked"})
    protected TableDataInfo getDataTable(List<?> list) {
        TableDataInfo rspData = new TableDataInfo();
        rspData.setCode(HttpStatus.SUCCESS);
        rspData.setMsg("Query was successful");
        rspData.setRows(list);
        rspData.setTotal(new PageInfo(list).getTotal());
        return rspData;
    }

    /**
     * success
     *
     * @return AjaxResult
     */
    public AjaxResult success() {
        return AjaxResult.success();
    }

    /**
     * error
     *
     * @return AjaxResult
     */
    public AjaxResult error() {
        return AjaxResult.error();
    }

    /**
     * success
     *
     * @param message message
     * @return AjaxResult
     */
    public AjaxResult success(String message) {
        return AjaxResult.success(message);
    }

    /**
     * error
     *
     * @param message message
     * @return AjaxResult
     */
    public AjaxResult error(String message) {
        return AjaxResult.error(message);
    }

    /**
     * toAjax
     *
     * @param rows rows
     * @return AjaxResult
     */
    protected AjaxResult toAjax(int rows) {
        return rows > 0 ? AjaxResult.success() : AjaxResult.error();
    }

    /**
     * toAjax
     *
     * @param result result
     * @return AjaxResult
     */
    protected AjaxResult toAjax(boolean result) {
        return result ? success() : error();
    }

    /**
     * redirect
     *
     * @param url url
     * @return String
     */
    public String redirect(String url) {
        return StringUtils.format("redirect:{}", url);
    }
}
