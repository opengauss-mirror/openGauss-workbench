package org.opengauss.admin.plugin.controller;

import org.opengauss.admin.common.core.domain.AjaxResult;
import org.opengauss.admin.plugin.base.BaseController;
import org.opengauss.admin.plugin.service.MigrationTaskInitGlobalParamService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author xielibo
 * @date 2023/01/14 09:01
 **/
@RestController
@RequestMapping("/param")
public class TaskParamController extends BaseController {

    @Autowired
    private MigrationTaskInitGlobalParamService taskInitGlobalParamService;

    /**
     * getById
     */
    @GetMapping(value = "/default")
    public AjaxResult getDefaultGlobalParam() {
        return AjaxResult.success(taskInitGlobalParamService.list());
    }


}
