
/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package org.opengauss.plugin.alertcenter.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import org.opengauss.plugin.alertcenter.dto.NotifyWayDto;
import org.opengauss.plugin.alertcenter.entity.NotifyWay;

import java.util.List;

/**
 * @author wuyuebin
 * @date 2023/5/25 15:52
 * @description
 */
public interface NotifyWayService extends IService<NotifyWay> {
    List<NotifyWay> getList(String notifyType);

    Page<NotifyWayDto> getListPage(String name, String notifyType, Page page);

    void saveNotifyWay(NotifyWay notifyWay);

    void delById(Long id);
}
