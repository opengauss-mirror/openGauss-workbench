package org.opengauss.admin.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.opengauss.admin.common.core.dto.SysTaskDto;
import org.opengauss.admin.system.domain.SysTask;

import java.util.List;


/**
 * Task Mapper
 *
 * @author xielibo
 */
@Mapper
public interface SysTaskMapper extends BaseMapper<SysTask> {


    /**
     * Query the task list by page
     *
     * @param page  page
     * @param task task
     */
    public IPage<SysTask> selectTaskPage(IPage<SysTask> page, @Param("entity") SysTaskDto task);

    /**
     * Query the task list
     *
     * @param task task
     */
    public List<SysTask> selectTaskList(SysTaskDto task);
}
