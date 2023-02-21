package org.opengauss.admin.plugin.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.opengauss.admin.plugin.domain.MigrationTask;

import java.util.List;
import java.util.Map;

/**
 * @author xielibo
 * @date 2023/01/14 09:01
 */
@Mapper
public interface MigrationTaskMapper extends BaseMapper<MigrationTask> {

    public IPage<MigrationTask> selectTaskPage(IPage<MigrationTask> page, @Param("mainTaskId") Integer mainTaskId);

    List<Map<String, Object>> countByMainTaskIdGroupByModelId(@Param("mainTaskId") Integer mainTaskId);

}





