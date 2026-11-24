/*
 * Copyright (c) 2026 Huawei Technologies Co., Ltd.
 * openGauss-workbench is licensed under Mulan PSL v2.
 * You can use this software according to the terms and conditions of the Mulan PSL v2.
 * You may obtain a copy of Mulan PSL v2 at:
 *          http://license.coscl.org.cn/MulanPSL2
 * THIS SOFTWARE IS PROVIDED ON AN "AS IS" BASIS, WITHOUT WARRANTIES OF ANY KIND,
 * EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO NON-INFRINGEMENT,
 * MERCHANTABILITY OR FIT FOR A PARTICULAR PURPOSE.
 * See the Mulan PSL v2 for more details.
 */

package com.nctigba.alert.monitor.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.nctigba.alert.monitor.model.entity.NotifyTemplateDO;

import java.util.List;

/**
 * @author wuyuebin
 * @date 2023/5/31 10:16
 * @description
 */
public interface NotifyTemplateService extends IService<NotifyTemplateDO> {
    /**
     * page
     *
     * @param notifyTemplateName String
     * @param notifyTemplateType String
     * @param page Page
     * @return Page
     */
    Page getListPage(String notifyTemplateName, String notifyTemplateType, Page page);

    /**
     * save
     *
     * @param notifyTemplateDO entity
     */
    void saveTemplate(NotifyTemplateDO notifyTemplateDO);

    /**
     * delById
     *
     * @param id long
     */
    void delById(Long id);

    /**
     * getList
     *
     * @return list
     */
    List<NotifyTemplateDO> getList();
}
