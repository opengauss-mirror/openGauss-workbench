package org.opengauss.admin.web.controller.ops;

import com.baomidou.mybatisplus.core.metadata.IPage;
import org.opengauss.admin.common.core.controller.BaseController;
import org.opengauss.admin.common.core.domain.AjaxResult;
import org.opengauss.admin.common.core.domain.entity.ops.OpsHostTagEntity;
import org.opengauss.admin.common.core.domain.model.ops.host.tag.HostTagAddInputDto;
import org.opengauss.admin.common.core.domain.model.ops.host.tag.HostTagInputDto;
import org.opengauss.admin.common.core.domain.model.ops.host.tag.HostTagPageVO;
import org.opengauss.admin.common.core.page.TableDataInfo;
import org.opengauss.admin.system.service.ops.IOpsHostTagService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @author lhf
 * @date 2023/3/14 23:42
 **/
@RestController
@RequestMapping("/hostTag")
public class OpsHostTagController extends BaseController {
    @Autowired
    private IOpsHostTagService opsHostTagService;

    @PutMapping("/addTag")
    public AjaxResult addTag(@RequestBody HostTagInputDto hostTagInputDto){
        opsHostTagService.addTag(hostTagInputDto);
        return AjaxResult.success();
    }

    @GetMapping("/listAll")
    public AjaxResult listAll(){
        List<OpsHostTagEntity> list = opsHostTagService.list();
        return AjaxResult.success(list);
    }

    @GetMapping("/page")
    public TableDataInfo page(@RequestParam(value = "name",required = false) String name){
        IPage<HostTagPageVO> page  = opsHostTagService.page(startPage(),name);
        return getDataTable(page);
    }

    @PostMapping("/add")
    public AjaxResult add(@RequestBody HostTagAddInputDto addInputDto){
        opsHostTagService.add(addInputDto.getName());
        return AjaxResult.success();
    }

    @PutMapping("/update/{tagId}")
    public AjaxResult update(@PathVariable("tagId") String tagId, @RequestBody HostTagAddInputDto addInputDto){
        opsHostTagService.update(tagId, addInputDto.getName());
        return AjaxResult.success();
    }

    @DeleteMapping("/del/{tagId}")
    public AjaxResult del(@PathVariable("tagId") String tagId){
        opsHostTagService.removeById(tagId);
        return AjaxResult.success();
    }

}
