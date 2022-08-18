package org.opengauss.admin.plugin.controller.ops;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.opengauss.admin.plugin.base.BaseController;
import org.opengauss.admin.common.core.domain.AjaxResult;
import org.opengauss.admin.plugin.service.ops.IOpsBackupService;
import org.opengauss.admin.plugin.vo.ops.BackupInputDto;
import org.opengauss.admin.plugin.vo.ops.BackupVO;
import org.opengauss.admin.plugin.vo.ops.RecoverInputDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * @author lhf
 * @date 2022/11/5 09:49
 **/
@RestController
@RequestMapping("/backup")
public class BackupController extends BaseController {
    @Autowired
    private IOpsBackupService backupService;

    @PostMapping("/backup")
    public AjaxResult backup(@RequestBody @Validated BackupInputDto backup) {
        backupService.backup(backup);
        return AjaxResult.success();
    }

    @GetMapping("/page")
    public AjaxResult pageBackup(@RequestParam(required = false, value = "clusterId") String clusterId) {
        Page<BackupVO> page = backupService.pageBackup(startPage(), clusterId);
        return AjaxResult.success(page);
    }

    @PostMapping("/recover/{id}")
    public AjaxResult recover(@PathVariable("id") String id, @RequestBody RecoverInputDto recover) {
        backupService.recover(id, recover);
        return AjaxResult.success();
    }

    @DeleteMapping("/del/{id}")
    public AjaxResult del(@PathVariable("id") String id) {
        backupService.del(id);
        return AjaxResult.success();
    }

}
