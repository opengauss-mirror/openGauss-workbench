package org.opengauss.admin.system.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.opengauss.admin.system.domain.SysWhiteList;
import org.opengauss.admin.system.mapper.SysWhiteListMapper;
import org.opengauss.admin.system.service.ISysWhiteListService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * White List Service
 *
 * @author xielibo
 */
@Service
public class SysWhiteListServiceImpl extends ServiceImpl<SysWhiteListMapper, SysWhiteList> implements ISysWhiteListService {

    @Autowired
    private SysWhiteListMapper sysWhiteListMapper;

    /**
     * Query the whitelist list by page
     *
     * @param whiteList SysWhiteList
     * @return SysWhiteList
     */
    @Override
    public IPage<SysWhiteList> selectList(IPage<SysWhiteList> page, SysWhiteList whiteList) {
        return sysWhiteListMapper.selectWhiteListPage(page, whiteList);
    }

    /**
     * Query all whitelists
     *
     * @param whiteList SysWhiteList
     * @return SysWhiteList
     */
    @Override
    public List<SysWhiteList> selectListAll(SysWhiteList whiteList) {
        return sysWhiteListMapper.selectWhiteListList(whiteList);
    }

    /**
     * Check whether the ip exists in the whitelist
     */
    @Override
    public boolean checkIpExistsInWhiteList(String ip) {
        Integer count = sysWhiteListMapper.countByIp(ip);
        return count > 0;
    }

    /**
     * Check whether the title exists
     */
    public boolean checkTitleExists(SysWhiteList whiteList) {
        Integer count = sysWhiteListMapper.countByTitle(whiteList);
        return count > 0;
    }

}
