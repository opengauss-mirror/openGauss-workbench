package org.opengauss.admin.system.mapper.ops;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.opengauss.admin.common.core.domain.entity.ops.OpsHostTagEntity;
import org.opengauss.admin.common.core.domain.model.ops.host.tag.HostTagPageVO;

/**
 * @author lhf
 * @date 2023/3/14 23:37
 **/
@Mapper
public interface OpsHostTagMapper extends BaseMapper<OpsHostTagEntity> {
    IPage<HostTagPageVO> page(Page page, @Param("name") String name);
}
