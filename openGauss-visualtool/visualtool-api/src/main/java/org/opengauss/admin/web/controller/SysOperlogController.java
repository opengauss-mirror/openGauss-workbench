package org.opengauss.admin.web.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import org.opengauss.admin.common.core.controller.BaseController;
import org.opengauss.admin.common.core.domain.AjaxResult;
import org.opengauss.admin.common.core.page.TableDataInfo;
import org.opengauss.admin.system.domain.SysOperLog;
import org.opengauss.admin.system.service.ISysOperLogService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * OperLog Controller
 *
 * @author xielibo
 */
@RestController
@RequestMapping("/system/operlog")
@Api(tags = "operLog")
public class SysOperlogController extends BaseController {
    @Autowired
    private ISysOperLogService operLogService;

    @GetMapping("/list")
    @ApiOperation(value = "log list", notes = "log list")
    @ApiImplicitParams({
    })
    public TableDataInfo list(SysOperLog operLog) {
        IPage<SysOperLog> list = operLogService.selectOperLogList(startPage(), operLog);
        return getDataTable(list);
    }

    @ApiOperation(value = "delete log", notes = "delete log")
    @ApiImplicitParams({
    })
    @DeleteMapping("/{operIds}")
    public AjaxResult remove(@PathVariable String[] operIds) {
        return toAjax(operLogService.deleteOperLogByIds(operIds));
    }

    @ApiOperation(value = "clean", notes = "clean")
    @ApiImplicitParams({
    })
    @DeleteMapping("/clean")
    public AjaxResult clean() {
        operLogService.cleanOperLog();
        return AjaxResult.success();
    }
}
