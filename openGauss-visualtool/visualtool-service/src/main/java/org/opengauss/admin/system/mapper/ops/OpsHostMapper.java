package org.opengauss.admin.system.mapper.ops;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.opengauss.admin.common.core.domain.entity.ops.OpsHostEntity;
import org.opengauss.admin.common.core.domain.model.ops.host.OpsHostVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * @author lhf
 * @date 2022/8/8 14:09
 **/
@Mapper
public interface OpsHostMapper extends BaseMapper<OpsHostEntity> {
    IPage<OpsHostVO> pageHost(Page page, @Param("name") String name);
}
