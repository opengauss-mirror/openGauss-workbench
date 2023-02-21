package org.opengauss.admin.plugin.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.opengauss.admin.plugin.domain.MigrationTaskHostRef;

import java.util.List;

/**
* @author xielibo
* @date 2023/01/14 09:01
*/
@Mapper
public interface MigrationTaskHostRefMapper extends BaseMapper<MigrationTaskHostRef> {


    List<MigrationTaskHostRef> selectByMainTaskId(@Param("mainTaskId") Integer mainTaskId);
}




