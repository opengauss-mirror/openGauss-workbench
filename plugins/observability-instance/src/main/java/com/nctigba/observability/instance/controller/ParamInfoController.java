/*
 *  Copyright (c) GBA-NCTI-ISDC. 2022-2024.
 *
 *  openGauss DataKit is licensed under Mulan PSL v2.
 *  You can use this software according to the terms and conditions of the Mulan PSL v2.
 *  You may obtain a copy of Mulan PSL v2 at:
 *
 *  http://license.coscl.org.cn/MulanPSL2
 *
 *  THIS SOFTWARE IS PROVIDED ON AN "AS IS" BASIS, WITHOUT WARRANTIES OF ANY KIND,
 *  EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO NON-INFRINGEMENT,
 *  MERCHANTABILITY OR FITFOR A PARTICULAR PURPOSE.
 *  See the Mulan PSL v2 for more details.
 *  -------------------------------------------------------------------------
 *
 *  ParamInfoController.java
 *
 *  IDENTIFICATION
 *  plugins/observability-instance/src/main/java/com/nctigba/observability/instance/controller/ParamInfoController.java
 *
 *  -------------------------------------------------------------------------
 */

package com.nctigba.observability.instance.controller;

import java.util.List;

import com.nctigba.observability.instance.service.impl.ParamInfoServiceImpl;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.nctigba.observability.instance.model.dto.param.ParamInfoDTO;
import com.nctigba.observability.instance.exception.InstanceException;
import com.nctigba.observability.instance.model.query.ParamQuery;
import com.nctigba.observability.instance.util.MessageSourceUtils;

import lombok.RequiredArgsConstructor;

/**
 * ParamInfo
 *
 * @author luomeng@ncti-gba.cn
 * @since 2023/01/30 15:00
 */
@RestController
@RequestMapping("/observability/v1/param")
@RequiredArgsConstructor
public class ParamInfoController {
    private final ParamInfoServiceImpl paramInfoServiceImpl;

    @GetMapping(value = "/paramInfo")
    public List<ParamInfoDTO> paramInfo(ParamQuery paramQuery) {
        MessageSourceUtils.reset();
        if ("".equals(paramQuery.getNodeId()) || paramQuery.getNodeId() == null) {
            throw new InstanceException(MessageSourceUtils.get("node.tip"));
        }
        return paramInfoServiceImpl.getParamInfo(paramQuery);
    }
}