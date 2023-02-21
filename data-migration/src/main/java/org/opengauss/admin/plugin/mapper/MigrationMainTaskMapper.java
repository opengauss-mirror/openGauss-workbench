package org.opengauss.admin.plugin.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.opengauss.admin.plugin.domain.MigrationMainTask;
import org.opengauss.admin.plugin.dto.MigrationMainTaskDto;

import java.util.List;


/**
 * Task Mapper
 *
 * @author xielibo
 */
@Mapper
public interface MigrationMainTaskMapper extends BaseMapper<MigrationMainTask> {


    /**
     * Query the task list by page
     *
     * @param page  page
     * @param task task
     */
    public IPage<MigrationMainTask> selectTaskPage(IPage<MigrationMainTask> page, @Param("entity") MigrationMainTaskDto task);

    /**
     * Query the task list
     *
     * @param task task
     */
    public List<MigrationMainTask> selectTaskList(MigrationMainTaskDto task);

    public List<String> selectCreateUsers();
}
