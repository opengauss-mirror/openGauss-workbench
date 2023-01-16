package org.opengauss.admin.system.mapper.ops;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.opengauss.admin.common.core.domain.entity.ops.OpsJdbcDbClusterEntity;

/**
 * @author lhf
 * @date 2023/1/13 11:06
 **/
@Mapper
public interface OpsJdbcDbClusterMapper extends BaseMapper<OpsJdbcDbClusterEntity> {
}
