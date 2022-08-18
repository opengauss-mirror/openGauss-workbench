package org.opengauss.admin.plugin.mapper.ops;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.opengauss.admin.plugin.domain.entity.ops.OpsCheckEntity;
import org.apache.ibatis.annotations.Mapper;

/**
 * @author lhf
 * @date 2022/11/13 17:16
 **/
@Mapper
public interface OpsCheckMapper extends BaseMapper<OpsCheckEntity> {
}
