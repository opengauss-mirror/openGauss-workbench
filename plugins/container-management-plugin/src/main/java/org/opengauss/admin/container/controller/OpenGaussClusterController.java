/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2024-2024. All rights reserved.
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
 *  OpenGaussClusterController.java
 *
 *  IDENTIFICATION
 *  plugins/container-management-plugin/src/main/java/org/opengauss/admin/container/controller
 * /OpenGaussClusterController.java
 *
 *  -------------------------------------------------------------------------
 */

package org.opengauss.admin.container.controller;

import lombok.extern.slf4j.Slf4j;
import org.opengauss.admin.common.core.domain.AjaxResult;
import org.opengauss.admin.common.exception.CustomException;
import org.opengauss.admin.common.utils.SecurityUtils;
import org.opengauss.admin.container.beans.OpenGaussClusterDetail;
import org.opengauss.admin.container.beans.OpenGaussClusterDto;
import org.opengauss.admin.container.beans.OpenGaussClusterSwitchoverDto;
import org.opengauss.admin.container.beans.OpenGaussPodVo;
import org.opengauss.admin.container.constant.CommonConstant;
import org.opengauss.admin.container.exception.MarsRuntimeException;
import org.opengauss.admin.container.service.OpenGaussClusterLifeCycleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * OpenGaussClusterController
 *
 * @since 2024-6-26 16:39
 **/
@Slf4j
@RestController
@RequestMapping("/opengauss/cluster")
public class OpenGaussClusterController {
    @Autowired
    OpenGaussClusterLifeCycleService openGaussClusterLifeCycleService;

    /**
     * 查询用户名
     *
     * @return 用户名
     */
    public String getLoginUserName() {
        try {
            return SecurityUtils.getUsername();
        } catch (CustomException exp) {
            log.error("Exception in obtaining username:", exp);
        }
        return "";
    }

    /**
     * 创建（部署）ogc集群
     *
     * @param openGaussClusterDto ogc模型
     * @return 部署结果
     */
    @PostMapping("/deploy")
    public AjaxResult deployOgc(@RequestBody OpenGaussClusterDto openGaussClusterDto) {
        if (openGaussClusterDto != null) {
            openGaussClusterDto.setOperator(getLoginUserName());
        }
        String res = openGaussClusterLifeCycleService.deploy(openGaussClusterDto);
        if (CommonConstant.RETURN_CODE_SUCCESS.equals(res)) {
            return AjaxResult.success();
        } else {
            return AjaxResult.error(res);
        }
    }

    /**
     * 删除（释放）ogc集群
     *
     * @param id ogc集群id
     * @return 删除结果
     */
    @DeleteMapping("/release/{id}")
    public AjaxResult releaseOgc(@PathVariable("id") Integer id) {
        String res = openGaussClusterLifeCycleService.release(id);
        if (CommonConstant.RETURN_CODE_SUCCESS.equals(res)) {
            return AjaxResult.success();
        } else {
            return AjaxResult.error(res);
        }
    }

    /**
     * 分页查询ogc集群列表
     *
     * @param k8sId    k8s集群id
     * @param ogcName  ogc名称
     * @param pageNum  页码
     * @param pageSize 页大小
     * @return 分页数据
     */
    @GetMapping("/list")
    public AjaxResult getOgcList(@RequestParam(value = "k8sId", required = false) String k8sId,
                                 @RequestParam(value = "ogcName", required = false) String ogcName,
                                 @RequestParam(value = "pageNum", required = true) Integer pageNum,
                                 @RequestParam(value = "pageSize", required = true) Integer pageSize) {
        Map<String, Object> res = openGaussClusterLifeCycleService.openGaussClusterList(k8sId, ogcName, pageNum,
                pageSize);
        return AjaxResult.success(res);
    }

    /**
     * 查询ogc集群详情
     *
     * @param id ogc集群id
     * @return 集群详情数据
     */
    @GetMapping("/{id}")
    public AjaxResult getOgcDetail(@PathVariable("id") Integer id) {
        try {
            OpenGaussClusterDetail detail = openGaussClusterLifeCycleService.openGaussClusterDetail(id);
            return AjaxResult.success(detail);
        } catch (MarsRuntimeException e) {
            return AjaxResult.error(e.getErrorMessage());
        }
    }

    /**
     * 查询ogc集群pod列表
     *
     * @param id ogc集群id
     * @return pod列表数据
     */
    @GetMapping("/pod/list/{id}")
    public AjaxResult getOgcPodList(@PathVariable("id") Integer id) {
        try {
            List<OpenGaussPodVo> podVoList = openGaussClusterLifeCycleService.openGaussClusterPodList(id);
            return AjaxResult.success(podVoList);
        } catch (MarsRuntimeException e) {
            return AjaxResult.error(e.getErrorMessage());
        }
    }

    /**
     * 更新ogc集群
     *
     * @param id                  ogc集群id
     * @param opengaussClusterDto ogc模型
     * @return 更新结果
     */
    @RequestMapping(value = "/{id}", method = RequestMethod.PUT)
    public AjaxResult updateOpengaussCluster(@PathVariable("id") Long id,
                                             @RequestBody OpenGaussClusterDto opengaussClusterDto) {
        String res = openGaussClusterLifeCycleService.updateOpengaussCluster(id, opengaussClusterDto);
        if (CommonConstant.RETURN_CODE_SUCCESS.equals(res)) {
            return AjaxResult.success();
        } else {
            return AjaxResult.error(res);
        }
    }

    /**
     * 重启ogc集群
     *
     * @param id ogc集群id
     * @return 重启结果
     */
    @RequestMapping(value = "/{id}/restart", method = RequestMethod.POST)
    public AjaxResult restartOpengaussCluster(@PathVariable("id") Long id) {
        String res = openGaussClusterLifeCycleService.restartOpengaussCluster(id);
        if (CommonConstant.RETURN_CODE_SUCCESS.equals(res)) {
            return AjaxResult.success("重启成功！");
        } else {
            return AjaxResult.error(res);
        }
    }

    /**
     * 删除（释放）ogc集群pod
     *
     * @param id      ogc集群id
     * @param podName pod名称
     * @return 删除结果
     */
    @DeleteMapping("/release/{id}/pod/{podName}")
    public AjaxResult releaseOgcPod(@PathVariable("id") Long id, @PathVariable("podName") String podName) {
        String res = openGaussClusterLifeCycleService.releaseOgcPod(id, podName);
        if (CommonConstant.RETURN_CODE_SUCCESS.equals(res)) {
            return AjaxResult.success("删除成功！");
        } else {
            return AjaxResult.error(res);
        }
    }

    /**
     * ogc集群切主
     *
     * @param id            ogc集群id
     * @param switchoverDto OpenGaussClusterSwitchoverDto模型
     * @return 切换结果
     */
    @RequestMapping(value = "/switchover/{id}", method = RequestMethod.POST)
    public AjaxResult switchoverOgc(@PathVariable("id") Long id,
                                    @RequestBody OpenGaussClusterSwitchoverDto switchoverDto) {
        if (!Objects.isNull(switchoverDto) && !Objects.isNull(id)) {
            switchoverDto.setId(id);
        }
        String res = openGaussClusterLifeCycleService.switchoverOpengaussCluster(switchoverDto);
        if (CommonConstant.RETURN_CODE_SUCCESS.equals(res)) {
            return AjaxResult.success("切换成功！");
        } else {
            return AjaxResult.error(res);
        }
    }
}
