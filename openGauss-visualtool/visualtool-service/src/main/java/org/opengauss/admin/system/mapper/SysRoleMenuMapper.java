package org.opengauss.admin.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.opengauss.admin.system.domain.SysRoleMenu;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * Role Menu Relation Mapper
 *
 * @author xielibo
 */
@Mapper
public interface SysRoleMenuMapper extends BaseMapper<SysRoleMenu> {
    /**
     * Add role menu association information in batches
     *
     * @param roleMenuList roleMenuList
     */
    public int batchRoleMenu(List<SysRoleMenu> roleMenuList);
}
