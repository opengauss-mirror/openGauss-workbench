/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package org.opengauss.plugin.alertcenter.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.opengauss.admin.common.core.page.TableDataInfo;
import org.opengauss.admin.common.enums.ResponseCode;
import org.opengauss.admin.common.utils.ServletUtils;
import org.opengauss.admin.common.utils.StringUtils;

/**
 * @author wuyuebin
 * @date 2023/5/29 09:14
 * @description
 */
public class BaseController {
    protected Page startPage() {
        Page page = new Page();
        Integer pageNum = ServletUtils.getParameterToInt("pageNum");
        Integer pageSize = ServletUtils.getParameterToInt("pageSize");
        if (StringUtils.isNotNull(pageNum) && StringUtils.isNotNull(pageSize)) {
            page.setCurrent((long) pageNum);
            page.setSize((long) pageSize);
            page.setOptimizeCountSql(false);
            page.setMaxLimit(500L);
        }
        return page;
    }

    protected TableDataInfo getDataTable(IPage<?> page) {
        TableDataInfo rspData = new TableDataInfo();
        rspData.setCode(ResponseCode.SUCCESS.code());
        rspData.setMsg(ResponseCode.SUCCESS.msg());
        rspData.setRows(page.getRecords());
        rspData.setTotal(page.getTotal());
        return rspData;
    }
}
