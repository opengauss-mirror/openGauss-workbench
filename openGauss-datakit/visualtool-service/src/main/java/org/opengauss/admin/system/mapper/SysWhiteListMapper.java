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
 * SysWhiteListMapper.java
 *
 * IDENTIFICATION
 * openGauss-visualtool/visualtool-service/src/main/java/org/opengauss/admin/system/mapper/SysWhiteListMapper.java
 *
 * -------------------------------------------------------------------------
 */


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
