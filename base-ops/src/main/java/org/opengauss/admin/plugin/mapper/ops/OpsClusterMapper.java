package org.opengauss.admin.plugin.mapper.ops;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.opengauss.admin.plugin.domain.entity.ops.OpsClusterEntity;
import org.apache.ibatis.annotations.Mapper;

/**
 * @author lhf
 * @date 2022/8/12 09:04
 **/
@Mapper
public interface OpsClusterMapper extends BaseMapper<OpsClusterEntity> {
}
