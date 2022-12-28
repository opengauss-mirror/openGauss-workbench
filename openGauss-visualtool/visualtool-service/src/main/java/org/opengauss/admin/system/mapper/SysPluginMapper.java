package org.opengauss.admin.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import org.opengauss.admin.system.domain.SysPlugin;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;


/**
 * SysPluginMapper
 *
 * @author xielibo
 */
@Mapper
public interface SysPluginMapper extends BaseMapper<SysPlugin> {

    /**
     * selectSysPluginListPage
     *
     */
    public IPage<SysPlugin> selectSysPluginListPage(IPage<SysPlugin> page, @Param("entity") SysPlugin sysPlugin);
}
