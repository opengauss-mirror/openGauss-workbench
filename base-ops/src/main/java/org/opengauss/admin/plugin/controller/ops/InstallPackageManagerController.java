package org.opengauss.admin.plugin.controller.ops;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.extern.slf4j.Slf4j;
import org.opengauss.admin.common.core.domain.AjaxResult;
import org.opengauss.admin.common.core.page.TableDataInfo;
import org.opengauss.admin.plugin.base.BaseController;
import org.opengauss.admin.plugin.domain.entity.ops.OpsPackageManagerEntity;
import org.opengauss.admin.plugin.enums.ops.OpenGaussVersionEnum;
import org.opengauss.admin.plugin.service.ops.IOpsPackageManagerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;
import java.util.Objects;

/**
 * @author lhf
 * @date 2022/12/11 16:04
 **/
@Slf4j
@RestController
@RequestMapping("/installPackageManager")
public class InstallPackageManagerController extends BaseController {

    @Autowired
    private IOpsPackageManagerService opsPackageManagerService;

    @PostMapping("/save")
    public AjaxResult save(@RequestBody OpsPackageManagerEntity packageManager){
        packageManager.setCreateTime(new Date());
        opsPackageManagerService.save(packageManager.populatePackageUrl());
        return AjaxResult.success();
    }

    @DeleteMapping("/{id}")
    public AjaxResult del(@PathVariable("id") String id){
        opsPackageManagerService.removeById(id);
        return AjaxResult.success();
    }

    @PutMapping("/update/{id}")
    public AjaxResult update(@PathVariable("id") String id,@RequestBody OpsPackageManagerEntity packageManager){
        packageManager.setPackageId(id);
        packageManager.setUpdateTime(new Date());
        opsPackageManagerService.updateById(packageManager.populatePackageUrl());
        return AjaxResult.success();
    }

    @GetMapping("/detail/{id}")
    public AjaxResult detail(@PathVariable("id") String id){
        OpsPackageManagerEntity byId = opsPackageManagerService.getById(id);
        return AjaxResult.success(byId);
    }

    @GetMapping("/page")
    public TableDataInfo page(@RequestParam(value = "name",required = false) String name,@RequestParam(value = "packageVersion",required = false) OpenGaussVersionEnum packageVersion){
        LambdaQueryWrapper<OpsPackageManagerEntity> queryWrapper = Wrappers.lambdaQuery(OpsPackageManagerEntity.class)
                .eq(Objects.nonNull(packageVersion),OpsPackageManagerEntity::getPackageVersion, packageVersion);

        if (StrUtil.isNotEmpty(name)){
            queryWrapper.or().eq(OpsPackageManagerEntity::getOs, name).or()
                    .eq(OpsPackageManagerEntity::getCpuArch, name).or()
                    .eq(OpsPackageManagerEntity::getPackageVersionNum, name);
        }

        IPage<OpsPackageManagerEntity> page = opsPackageManagerService.page(startPage(),queryWrapper);
        return getDataTable(page);
    }

    @GetMapping("/list")
    public AjaxResult list(@RequestParam(value = "os",required = false) String os,
                           @RequestParam(value = "cpuArch",required =false) String cpuArch,
                           @RequestParam(value = "packageVersion",required = false) OpenGaussVersionEnum packageVersion,
                           @RequestParam(value = "packageVersionNum",required = false) String packageVersionNum){

        LambdaQueryWrapper<OpsPackageManagerEntity> queryWrapper = Wrappers.lambdaQuery(OpsPackageManagerEntity.class)
                .eq(StrUtil.isNotEmpty(os), OpsPackageManagerEntity::getOs, os)
                .eq(StrUtil.isNotEmpty(cpuArch), OpsPackageManagerEntity::getCpuArch, cpuArch)
                .eq(Objects.nonNull(packageVersion), OpsPackageManagerEntity::getPackageVersion, Objects.nonNull(packageVersion)?packageVersion.name():null)
                .eq(StrUtil.isNotEmpty(packageVersionNum), OpsPackageManagerEntity::getPackageVersionNum, packageVersionNum)
                .orderByDesc(OpsPackageManagerEntity::getCreateTime);

        List<OpsPackageManagerEntity> list = opsPackageManagerService.list(queryWrapper);
        return AjaxResult.success(list);
    }

}
