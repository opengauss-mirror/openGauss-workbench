/*
 * Copyright (c) 2022-2022 Huawei Technologies Co.,Ltd.
 *
 * openGauss is licensed under Mulan PSL v2.
 * You can use this software according to the terms and conditions of the Mulan PSL v2.
 * You may obtain a copy of Mulan PSL v2 at:
 *
 * THIS SOFTWARE IS PROVIDED ON AN "AS IS" BASIS, WITHOUT WARRANTIES OF ANY KIND,
 * EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO NON-INFRINGEMENT,
 * MERCHANTABILITY OR FIT FOR A PARTICULAR PURPOSE.
 * See the Mulan PSL v2 for more details.
 */

package org.opengauss.admin.plugin.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.util.Strings;
import org.opengauss.admin.common.core.domain.AjaxResult;
import org.opengauss.admin.plugin.domain.TbMigrationTaskGlobalToolsParam;
import org.opengauss.admin.plugin.enums.MigrationErrorCode;
import org.opengauss.admin.plugin.service.TbMigrationTaskGlobalToolsParamService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import static org.opengauss.admin.plugin.enums.MigrationErrorCode.TOOLS_PARAMS_CAN_NOT_DELETE_ERROR;

/**
 * TaskToolsParamController
 *
 * @author: www
 * @date: 2023/11/28 15:04
 * @description: msg
 * @since: 1.1
 * @version: 1.1
 */
@RestController
@RequestMapping("/toolsParam")
@Slf4j
public class TaskToolsParamController {
    @Autowired
    TbMigrationTaskGlobalToolsParamService globalToolsParamService;

    /**
     * 根据houstid 查询配置信息
     *
     * @author: www
     * @date: 2023/11/28 15:04
     * @description: msg
     * @since: 1.1
     * @version: 1.1
     * @param hostId hostID
     * @return AjaxResult
     */
    @GetMapping(value = "/list/{hostId}")
    public AjaxResult getToolsGlobalParam(@PathVariable("hostId") String hostId) {
        LambdaQueryWrapper<TbMigrationTaskGlobalToolsParam> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TbMigrationTaskGlobalToolsParam::getPortalHostID, hostId);
        wrapper.orderByAsc(TbMigrationTaskGlobalToolsParam::getId);
        wrapper.eq(TbMigrationTaskGlobalToolsParam::getDeleteFlag,
                TbMigrationTaskGlobalToolsParam.DeleteFlagEnum.USED.getDeleteFlag());
        List<TbMigrationTaskGlobalToolsParam> globalToolsParams = globalToolsParamService.list(wrapper);
        globalToolsParams.forEach(globalToolsParam -> {
            if (globalToolsParam.getParamChangeValue() == null) {
                globalToolsParam.setParamChangeValue(globalToolsParam.getParamValue());
            }
        });
        return AjaxResult.success(globalToolsParams);
    }

    /**
     * modToolsGlobalParam
     *
     * @author: www
     * @date: 2023/11/28 15:05
     * @description: msg
     * @since: 1.1
     * @version: 1.1
     * @param globalToolsParams toolsParmas
     * @return AjaxResult
     */
    @PostMapping(value = "/modify")
    public AjaxResult modToolsGlobalParam(@RequestBody List<TbMigrationTaskGlobalToolsParam> globalToolsParams) {
        if (CollectionUtils.isEmpty(globalToolsParams)) {
            return AjaxResult.success();
        }
        List<Integer> paramIds =
                globalToolsParams.stream().map(TbMigrationTaskGlobalToolsParam::getId).collect(Collectors.toList());
        Map<Integer, String> toolsParamIdNewParamMap =
                globalToolsParams.stream().collect(Collectors.toMap(TbMigrationTaskGlobalToolsParam::getId,
                        TbMigrationTaskGlobalToolsParam::getParamChangeValue));
        List<TbMigrationTaskGlobalToolsParam> toolsParams = globalToolsParamService.listByIds(paramIds);
        for (TbMigrationTaskGlobalToolsParam toolsParam : toolsParams) {
            toolsParam.setParamChangeValue(toolsParamIdNewParamMap.get(toolsParam.getId()));
        }
        return AjaxResult.success(globalToolsParamService.updateBatchById(toolsParams));
    }

    /**
     * has param key
     *
     * @param paramKey param key
     * @param configId config id
     * @param portalHostID portal host id
     * @return AjaxResult
     */
    @GetMapping("/hasParamKey")
    public AjaxResult hasParamKey(
            @RequestParam String paramKey, @RequestParam Integer configId, @RequestParam String portalHostID) {
        return AjaxResult.success(globalToolsParamService.checkParamKeyExistence(paramKey, configId, portalHostID));
    }

    /**
     * 保存参数 根据configID 和 paramkey 判断是否同一个参数 是则修改状态 修改值
     *
     * @param globalToolsParam globalToolsParam
     * @return AjaxResult
     * @author: www
     * @date: 2023/11/24 15:02
     * @description: msg
     * @since: 1.1
     * @version: 1.1
     */
    @PostMapping(value = "/save")
    public AjaxResult saveToolsGlobalParam(@RequestBody TbMigrationTaskGlobalToolsParam globalToolsParam) {
        if (Objects.isNull(globalToolsParam)) {
            return AjaxResult.success();
        }
        if (globalToolsParamService.checkParamKeyExistence(
                globalToolsParam.getParamKey(), globalToolsParam.getConfigId(), globalToolsParam.getPortalHostID())) {
            return AjaxResult.error("A parameter with the same name exists.");
        }

        LambdaQueryWrapper<TbMigrationTaskGlobalToolsParam> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TbMigrationTaskGlobalToolsParam::getParamKey, globalToolsParam.getParamKey())
                .eq(TbMigrationTaskGlobalToolsParam::getConfigId, globalToolsParam.getConfigId())
                .eq(TbMigrationTaskGlobalToolsParam::getPortalHostID, globalToolsParam.getPortalHostID());
        TbMigrationTaskGlobalToolsParam toolsParam = globalToolsParamService.getOne(wrapper);
        if (toolsParam == null) {
            log.info("start save tools param :{}", globalToolsParam);
            globalToolsParam.setDeleteFlag(TbMigrationTaskGlobalToolsParam.DeleteFlagEnum.USED.getDeleteFlag());
            globalToolsParam.setNewParamFlag(
                    TbMigrationTaskGlobalToolsParam.NewParamFlagEnum.NEW_PARAM.getNewParamFlag());
            return AjaxResult.success(globalToolsParamService.saveOrUpdate(globalToolsParam));
        } else {
            toolsParam.setDeleteFlag(TbMigrationTaskGlobalToolsParam.DeleteFlagEnum.USED.getDeleteFlag());
            toolsParam.setParamValue(globalToolsParam.getParamValue());
            toolsParam.setParamChangeValue(globalToolsParam.getParamValue());
            toolsParam.setParamValueType(globalToolsParam.getParamValueType());
            toolsParam.setParamDesc(globalToolsParam.getParamDesc());
            return AjaxResult.success(globalToolsParamService.saveOrUpdate(toolsParam));
        }
    }


    /**
     * removeToolsGlobalParam
     *
     * @author: www
     * @date: 2023/11/28 15:05
     * @description: msg
     * @since: 1.1
     * @version: 1.1
     * @param toolsParmaId paramId
     * @return AjaxResult
     */
    @GetMapping(value = "/delete/{toolsParmaId}")
    public AjaxResult removeToolsGlobalParam(@PathVariable("toolsParmaId") String toolsParmaId) {
        if (Strings.isBlank(toolsParmaId)) {
            return AjaxResult.success();
        }
        log.info("start remove tools param :{}", toolsParmaId);
        TbMigrationTaskGlobalToolsParam toolsParam = globalToolsParamService.getById(toolsParmaId);
        if (toolsParam.getNewParamFlag() == null) {
            return AjaxResult.error(MigrationErrorCode.TOOLS_PARAMS_CAN_NOT_DELETE_ERROR.getCode(),
                    MigrationErrorCode.TOOLS_PARAMS_CAN_NOT_DELETE_ERROR.getMsg());
        }
        toolsParam.setDeleteFlag(TbMigrationTaskGlobalToolsParam.DeleteFlagEnum.DELETE.getDeleteFlag());
        return AjaxResult.success(globalToolsParamService.updateById(toolsParam));
    }
}

