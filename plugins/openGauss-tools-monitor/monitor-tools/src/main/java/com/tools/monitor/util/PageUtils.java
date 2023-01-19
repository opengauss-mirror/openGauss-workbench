package com.tools.monitor.util;

import com.tools.monitor.common.core.page.PageDomain;
import com.tools.monitor.common.core.page.TableSupport;
import com.github.pagehelper.PageHelper;

/**
 * PageUtils
 *
 * @author liu
 * @since 2022-10-01
 */
public class PageUtils extends PageHelper {
    public static void startPage() {
        PageDomain pageDomain = TableSupport.buildPageRequest();
        Integer pageNum = pageDomain.getPageNum();
        Integer pageSize = pageDomain.getPageSize();
        String orderBy = SqlUtil.escapeOrderBySql(pageDomain.getOrderBy());
        Boolean reasonable = pageDomain.getReasonable();
        PageHelper.startPage(pageNum, pageSize, orderBy).setReasonable(reasonable);
    }

    public static void clearPage() {
        PageHelper.clearPage();
    }
}
