package org.opengauss.admin.system.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import org.opengauss.admin.common.core.dto.SysTaskDto;
import org.opengauss.admin.system.domain.SysTask;

import java.util.List;

/**
 * Task Service Interface
 *
 * @author xielibo
 */
public interface ISysTaskService extends IService<SysTask> {

    IPage<SysTask> selectList(IPage<SysTask> page, SysTaskDto task);

    List<SysTask> selectListAll(SysTaskDto task);
}
