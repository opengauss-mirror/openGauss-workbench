package org.opengauss.admin.plugin.mapper.ops;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.opengauss.admin.plugin.domain.entity.ops.OpsClusterNodeEntity;
import org.apache.ibatis.annotations.Mapper;

/**
 * @author lhf
 * @date 2022/8/18 09:14
 **/
@Mapper
public interface OpsClusterNodeMapper extends BaseMapper<OpsClusterNodeEntity> {
}
