package org.opengauss.admin.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.opengauss.admin.common.core.domain.entity.SysSettingEntity;

/**
 * System Setting Mapper
 *
 * @author wangyl
 */
@Mapper
public interface SysSettingMapper extends BaseMapper<SysSettingEntity> {
}
