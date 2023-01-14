package org.opengauss.admin.web.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.opengauss.admin.common.annotation.Log;
import org.opengauss.admin.common.core.controller.BaseController;
import org.opengauss.admin.common.core.domain.AjaxResult;
import org.opengauss.admin.common.core.dto.SysTaskDto;
import org.opengauss.admin.common.core.page.TableDataInfo;
import org.opengauss.admin.common.enums.BusinessType;
import org.opengauss.admin.system.domain.SysTask;
import org.opengauss.admin.system.service.ISysTaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;

/**
 * Task Controller
 *
 * @author xielibo
 * @date 2023-01-13
 */
@RestController
@RequestMapping("/sys/task")
@Api(tags = "Task")
public class SysTaskController extends BaseController {
    @Autowired
    private ISysTaskService sysTaskService;

    /**
     * page list
     */
    @ApiOperation(value = "page list", notes = "page list")
    @PreAuthorize("@ss.hasPermi('system:task:list')")
    @GetMapping("/list")
    public TableDataInfo list(SysTaskDto task) {
        IPage<SysTask> list = sysTaskService.selectList(startPage(), task);
        return getDataTable(list);
    }

    /**
     * list
     */
    @ApiOperation(value = "list", notes = "list")
    @PreAuthorize("@ss.hasPermi('system:task:list')")
    @GetMapping("/list/all")
    public AjaxResult listAll(SysTaskDto task) {
        List<SysTask> list = sysTaskService.selectListAll(task);
        return AjaxResult.success(list);
    }

    /**
     * getById
     */
    @ApiOperation(value = "getById", notes = "getById")
    @ApiImplicitParams({
    })
    @PreAuthorize("@ss.hasPermi('system:task:query')")
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") Integer id) {
        return AjaxResult.success(sysTaskService.getById(id));
    }

    /**
     * delete
     */
    @Log(title = "task", businessType = BusinessType.DELETE)
    @ApiOperation(value = "delete", notes = "delete")
    @PreAuthorize("@ss.hasPermi('system:task:remove')")
    @DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Integer[] ids) {
        return toAjax(sysTaskService.removeBatchByIds(Arrays.asList(ids)));
    }
}
