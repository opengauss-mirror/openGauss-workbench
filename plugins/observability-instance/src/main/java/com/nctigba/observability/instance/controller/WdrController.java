/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package com.nctigba.observability.instance.controller;

import java.beans.PropertyEditorSupport;
import java.util.Date;

import javax.servlet.http.HttpServletResponse;

import org.opengauss.admin.common.core.domain.AjaxResult;
import org.opengauss.admin.common.utils.DateUtils;
import org.opengauss.admin.common.utils.ServletUtils;
import org.opengauss.admin.common.utils.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.nctigba.observability.instance.entity.OpsWdrEntity;
import com.nctigba.observability.instance.entity.OpsWdrEntity.WdrScopeEnum;
import com.nctigba.observability.instance.entity.OpsWdrEntity.WdrTypeEnum;
import com.nctigba.observability.instance.model.WdrGeneratorBody;
import com.nctigba.observability.instance.service.ClusterManager;
import com.nctigba.observability.instance.service.OpsWdrService;

/**
 * @author lhf
 * @date 2022/10/13 15:14
 **/
@RestController
@RequestMapping("/wdr")
public class WdrController {
    @Autowired
    private ClusterManager clusterManager;

    @InitBinder
    public void initBinder(WebDataBinder binder) {
        // Date format
        binder.registerCustomEditor(Date.class, new PropertyEditorSupport() {
            @Override
            public void setAsText(String text) {
                setValue(DateUtils.parseDate(text));
            }
        });
    }

    @Autowired
    private OpsWdrService wdrService;

    @GetMapping("/listSnapshot")
    public Page<?> listSnapshot(@RequestParam String clusterId, @RequestParam String hostId) {
        return wdrService.listSnapshot(startPage(), clusterManager.getNodeIdByCluster(clusterId, hostId));
    }

    @GetMapping("/createSnapshot")
    public AjaxResult createSnapshot(@RequestParam String clusterId, @RequestParam String hostId) {
        wdrService.createSnapshot(clusterId, hostId);
        return AjaxResult.success();
    }

    @GetMapping("/list")
    public Page<OpsWdrEntity> list(@RequestParam String clusterId,
            @RequestParam(required = false, name = "wdrScope") WdrScopeEnum wdrScope,
            @RequestParam(required = false, name = "wdrType") WdrTypeEnum wdrType,
            @RequestParam(required = false, name = "hostId") String hostId,
            @RequestParam(required = false, name = "start") @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") Date start,
            @RequestParam(required = false, name = "end") @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") Date end) {
        return wdrService.listWdr(startPage(), clusterId, wdrScope, wdrType, hostId, start, end);
    }

    @DeleteMapping("/del/{id}")
    public AjaxResult del(@PathVariable("id") String id) {
        wdrService.del(id);
        return AjaxResult.success();
    }

    @PostMapping("/generate")
    public AjaxResult generate(@RequestBody @Validated WdrGeneratorBody wdrGeneratorBody) {
        wdrService.generate(wdrGeneratorBody);
        return AjaxResult.success();
    }

    @GetMapping("/downloadWdr")
    public void downloadWdr(@RequestParam String wdrId, HttpServletResponse response) {
        wdrService.downloadWdr(wdrId, response);
    }

    @SuppressWarnings("rawtypes")
    protected Page startPage() {
        Page page = new Page();
        Integer pageNum = ServletUtils.getParameterToInt("pageNum");
        Integer pageSize = ServletUtils.getParameterToInt("pageSize");
        if (StringUtils.isNotNull(pageNum) && StringUtils.isNotNull(pageSize)) {
            page.setCurrent(pageNum);
            page.setSize(pageSize);
            page.setOptimizeCountSql(false);
            page.setMaxLimit(500L);
        }
        return page;
    }
}
