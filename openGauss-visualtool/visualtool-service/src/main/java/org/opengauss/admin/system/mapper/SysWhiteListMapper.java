package org.opengauss.admin.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import org.opengauss.admin.system.domain.SysWhiteList;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;


/**
 * White List Mapper
 *
 * @author xielibo
 */
@Mapper
public interface SysWhiteListMapper extends BaseMapper<SysWhiteList> {


    /**
     * Query the whitelist list by page
     *
     * @param page  page
     * @param whiteList SysWhiteList
     */
    public IPage<SysWhiteList> selectWhiteListPage(IPage<SysWhiteList> page, @Param("entity") SysWhiteList whiteList);

    /**
     * Query the whitelist list
     *
     * @param whiteList whiteList
     */
    public List<SysWhiteList> selectWhiteListList(SysWhiteList whiteList);


    public Integer countByIp(@Param("ip") String ip);

    public Integer countByTitle(SysWhiteList whiteList);

}
