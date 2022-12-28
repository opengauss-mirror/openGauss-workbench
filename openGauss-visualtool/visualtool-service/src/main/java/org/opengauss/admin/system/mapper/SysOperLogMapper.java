package org.opengauss.admin.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.opengauss.admin.system.domain.SysOperLog;
import org.apache.ibatis.annotations.Mapper;

/**
 * OperLog Mapper
 *
 * @author xielibo
 */
@Mapper
public interface SysOperLogMapper extends BaseMapper<SysOperLog> {
}
