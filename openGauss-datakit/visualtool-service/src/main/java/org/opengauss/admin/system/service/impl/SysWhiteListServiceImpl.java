/*
 * Copyright (c) 2022 Huawei Technologies Co.,Ltd.
 *
 * openGauss is licensed under Mulan PSL v2.
 * You can use this software according to the terms and conditions of the Mulan PSL v2.
 * You may obtain a copy of Mulan PSL v2 at:
 *
 * http://license.coscl.org.cn/MulanPSL2
 *
 * THIS SOFTWARE IS PROVIDED ON AN "AS IS" BASIS, WITHOUT WARRANTIES OF ANY KIND,
 * EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO NON-INFRINGEMENT,
 * MERCHANTABILITY OR FITFOR A PARTICULAR PURPOSE.
 * See the Mulan PSL v2 for more details.
 * -------------------------------------------------------------------------
 *
 * SysWhiteListServiceImpl.java
 *
 * IDENTIFICATION
 * openGauss-visualtool/visualtool-service/src/main/java/org/opengauss/admin/system/service/impl/SysWhiteListServiceImpl.java
 *
 * -------------------------------------------------------------------------
 */


package org.opengauss.admin.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.opengauss.admin.system.domain.SysWhiteList;
import org.opengauss.admin.system.mapper.SysWhiteListMapper;
import org.opengauss.admin.system.service.ISysWhiteListService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

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
    public boolean checkSingleIpExistsInWhiteList(String ip) {
        Integer count = sysWhiteListMapper.countByIp(ip);
        return count > 0;
    }

    @Override
    public List<String> checkIpsExistsInWhiteList(SysWhiteList whiteList) {
        List<SysWhiteList> sysWhiteLists = (whiteList.getId() != null)
                ? list(new LambdaQueryWrapper<SysWhiteList>().ne(SysWhiteList::getId, whiteList.getId()))
                : list();

        Set<String> whiteListIps = new HashSet<>();
        for (SysWhiteList sysWhiteList : sysWhiteLists) {
            whiteListIps.addAll(Arrays.asList(sysWhiteList.getIpList().split(",")));
        }

        return Arrays.stream(whiteList.getIpList().split(","))
                .filter(whiteListIps::contains)
                .collect(Collectors.toList());
    }

    /**
     * Check whether the title exists
     */
    public boolean checkTitleExists(SysWhiteList whiteList) {
        Integer count = sysWhiteListMapper.countByTitle(whiteList);
        return count > 0;
    }

}
