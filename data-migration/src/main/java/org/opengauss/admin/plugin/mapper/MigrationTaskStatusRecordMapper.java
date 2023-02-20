package org.opengauss.admin.plugin.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.opengauss.admin.plugin.domain.MigrationTaskStatusRecord;

import java.util.List;

/**
* @author xielibo
* @date 2023/01/14 09:01
*/
@Mapper
public interface MigrationTaskStatusRecordMapper extends BaseMapper<MigrationTaskStatusRecord> {

    public List<MigrationTaskStatusRecord> selectByTaskId(@Param("taskId") Integer taskId);

}




