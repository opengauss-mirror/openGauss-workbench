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
 * ISysWhiteListService.java
 *
 * IDENTIFICATION
 * openGauss-visualtool/visualtool-service/src/main/java/org/opengauss/admin/system/service/ISysWhiteListService.java
 *
 * -------------------------------------------------------------------------
 */


package org.opengauss.admin.system.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import org.opengauss.admin.system.domain.SysWhiteList;

import java.util.List;

/**
 * White List Service Interface
 *
 * @author xielibo
 */
public interface ISysWhiteListService extends IService<SysWhiteList> {
    IPage<SysWhiteList> selectList(IPage<SysWhiteList> page, SysWhiteList whiteList);

    List<SysWhiteList> selectListAll(SysWhiteList whiteList);

    /**
     * check single ip exists in whiteList
     *
     * @param ip ip
     * @return is single ip exists
     */
    boolean checkSingleIpExistsInWhiteList(String ip);

    /**
     * check ips exists in white list
     *
     * @param whiteList whitelist
     * @return exists ip list
     */
    List<String> checkIpsExistsInWhiteList(SysWhiteList whiteList);

    public boolean checkTitleExists(SysWhiteList whiteList);
}
