package org.opengauss.admin.web.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import org.opengauss.admin.common.annotation.Log;
import org.opengauss.admin.common.core.controller.BaseController;
import org.opengauss.admin.common.core.domain.AjaxResult;
import org.opengauss.admin.common.core.page.TableDataInfo;
import org.opengauss.admin.common.enums.BusinessType;
import org.opengauss.admin.system.domain.SysWhiteList;
import org.opengauss.admin.system.service.ISysWhiteListService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
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
@Api(tags = "whiteList")
public class SysWhiteListController extends BaseController {
    @Autowired
    private ISysWhiteListService iSysWhiteListService;

    /**
     * page list
     */
    @ApiOperation(value = "page list", notes = "page list")
    @PreAuthorize("@ss.hasPermi('system:whiteList:list')")
    @GetMapping("/list")
    public TableDataInfo list(SysWhiteList whiteList) {
        IPage<SysWhiteList> list = iSysWhiteListService.selectList(startPage(), whiteList);
        return getDataTable(list);
    }

    /**
     * list
     */
    @ApiOperation(value = "list", notes = "list")
    @PreAuthorize("@ss.hasPermi('system:whiteList:list')")
    @GetMapping("/list/all")
    public AjaxResult listAll(SysWhiteList whiteList) {
        List<SysWhiteList> list = iSysWhiteListService.selectListAll(whiteList);
        return AjaxResult.success(list);
    }

    /**
     * getById
     */
    @ApiOperation(value = "getById", notes = "getById")
    @ApiImplicitParams({
    })
    @PreAuthorize("@ss.hasPermi('system:whiteList:query')")
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") Integer id) {
        return AjaxResult.success(iSysWhiteListService.getById(id));
    }

    /**
     * save
     */
    @Log(title = "whitelist", businessType = BusinessType.INSERT)
    @ApiOperation(value = "save", notes = "save")
    @PreAuthorize("@ss.hasPermi('system:whiteList:add')")
    @PostMapping
    public AjaxResult add(@RequestBody SysWhiteList whiteList) {
        whiteList.setCreateTime(new Date());
        return toAjax(iSysWhiteListService.save(whiteList));
    }

    /**
     * update
     */
    @Log(title = "whitelist", businessType = BusinessType.UPDATE)
    @ApiOperation(value = "update", notes = "update")
    @PreAuthorize("@ss.hasPermi('system:whiteList:edit')")
    @PutMapping
    public AjaxResult edit(@RequestBody SysWhiteList whiteList) {
        return toAjax(iSysWhiteListService.updateById(whiteList));
    }

    /**
     * delete
     */
    @Log(title = "whitelist", businessType = BusinessType.DELETE)
    @ApiOperation(value = "delete", notes = "delete")
    @PreAuthorize("@ss.hasPermi('system:whiteList:remove')")
    @DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Integer[] ids) {
        return toAjax(iSysWhiteListService.removeBatchByIds(Arrays.asList(ids)));
    }
}
