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
 * SysWhiteListController.java
 *
 * IDENTIFICATION
 * openGauss-visualtool/visualtool-api/src/main/java/org/opengauss/admin/web/controller/SysWhiteListController.java
 *
 * -------------------------------------------------------------------------
 */


package org.opengauss.admin.web.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.github.pagehelper.util.StringUtil;
import org.opengauss.admin.common.annotation.Log;
import org.opengauss.admin.common.core.controller.BaseController;
import org.opengauss.admin.common.core.domain.AjaxResult;
import org.opengauss.admin.common.core.page.TableDataInfo;
import org.opengauss.admin.common.enums.BusinessType;
import org.opengauss.admin.common.enums.ResponseCode;
import org.opengauss.admin.system.domain.SysWhiteList;
import org.opengauss.admin.system.service.ISysWhiteListService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * whiteList Controller
 *
 * @author xielibo
 * @date 2022-08-10
 */
@RestController
@RequestMapping("/sys/whiteList")
public class SysWhiteListController extends BaseController {
    @Autowired
    private ISysWhiteListService iSysWhiteListService;

    /**
     * page list
     */
    @PreAuthorize("@ss.hasPermi('system:whiteList:list')")
    @GetMapping("/list")
    public TableDataInfo list(SysWhiteList whiteList) {
        IPage<SysWhiteList> list = iSysWhiteListService.selectList(startPage(), whiteList);
        return getDataTable(list);
    }

    /**
     * list
     */
    @PreAuthorize("@ss.hasPermi('system:whiteList:list')")
    @GetMapping("/list/all")
    public AjaxResult listAll(SysWhiteList whiteList) {
        List<SysWhiteList> list = iSysWhiteListService.selectListAll(whiteList);
        return AjaxResult.success(list);
    }

    /**
     * getById
     */
    @PreAuthorize("@ss.hasPermi('system:whiteList:query')")
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") Integer id) {
        return AjaxResult.success(iSysWhiteListService.getById(id));
    }

    /**
     * save
     */
    @Log(title = "whitelist", businessType = BusinessType.INSERT)
    @PreAuthorize("@ss.hasPermi('system:whiteList:add')")
    @PostMapping
    public AjaxResult add(@RequestBody SysWhiteList whiteList) {
        whiteList.setCreateTime(new Date());
        return processWhiteList(whiteList, true);
    }

    /**
     * update
     */
    @Log(title = "whitelist", businessType = BusinessType.UPDATE)
    @PreAuthorize("@ss.hasPermi('system:whiteList:edit')")
    @PutMapping
    public AjaxResult edit(@RequestBody SysWhiteList whiteList) {
        return processWhiteList(whiteList, false);
    }

    private AjaxResult processWhiteList(SysWhiteList whiteList, boolean isAdd) {
        if (whiteList.getTitle().length() > 100) {
            return AjaxResult.error(ResponseCode.WHITELIST_TITLE_MAX_LENGTH_ERROR.code());
        }
        if (iSysWhiteListService.checkTitleExists(whiteList)) {
            return AjaxResult.error(ResponseCode.WHITELIST_TITLE_EXISTS_ERROR.code());
        }
        if (StringUtil.isNotEmpty(whiteList.getIpList()) && whiteList.getIpList().length() > 200) {
            return AjaxResult.error(ResponseCode.WHITELIST_IPS_MAX_LENGTH_ERROR.code());
        }
        if (whiteList.hasDuplicateIp()) {
            return AjaxResult.error(ResponseCode.WHITELIST_HAS_DUPLICATE_IP_ERROR.code());
        }
        List<String> existsIps = iSysWhiteListService.checkIpsExistsInWhiteList(whiteList);
        if (!existsIps.isEmpty()) {
            return AjaxResult.error(ResponseCode.WHITELIST_IPS_EXISTS_ERROR.code(),
                    ResponseCode.WHITELIST_IPS_EXISTS_ERROR.msg() + ". Ip addresses: " + String.join(",", existsIps));
        }

        return isAdd ? toAjax(iSysWhiteListService.save(whiteList))
                : toAjax(iSysWhiteListService.updateById(whiteList));
    }

    /**
     * delete
     */
    @Log(title = "whitelist", businessType = BusinessType.DELETE)
    @PreAuthorize("@ss.hasPermi('system:whiteList:remove')")
    @DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Integer[] ids) {
        return toAjax(iSysWhiteListService.removeBatchByIds(Arrays.asList(ids)));
    }
}
