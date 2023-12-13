/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2012-2022. All rights reserved.
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
 *
 * -------------------------------------------------------------------------
 */

package org.opengauss.collect.service.impl;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.gitee.starblues.bootstrap.annotation.AutowiredType;
import com.jcraft.jsch.Session;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.opengauss.collect.config.common.Constant;
import org.opengauss.collect.domain.CollectPeriod;
import org.opengauss.collect.domain.LinuxConfig;
import org.opengauss.collect.mapper.CollectPeriodMapper;
import org.opengauss.collect.service.SqlOperation;
import org.opengauss.collect.service.SqlTaskService;
import org.opengauss.collect.utils.AssertUtil;
import org.opengauss.collect.utils.IdUtils;
import org.opengauss.collect.utils.JschUtil;
import org.opengauss.collect.utils.StringUtil;
import org.opengauss.collect.utils.ValidatorUtil;
import org.opengauss.collect.utils.response.RespBean;
import org.opengauss.admin.common.core.domain.entity.ops.OpsHostEntity;
import org.opengauss.admin.system.service.ops.IHostService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 功能描述
 *
 * @author liu
 * @since 2022-10-01
 */
@Service
public class SqlTaskServiceImpl implements SqlTaskService {
    @Autowired
    private CollectPeriodMapper periodMapper;

    @Autowired
    private SqlOperation operation;

    @Autowired
    @AutowiredType(AutowiredType.Type.PLUGIN_MAIN)
    private IHostService hostService;

    @Override
    public RespBean getTaskList(CollectPeriod period) {
        QueryWrapper wrapper = new QueryWrapper<CollectPeriod>()
                .eq(StrUtil.isNotEmpty(period.getTaskName()), "task_name", period.getTaskName())
                .eq(StrUtil.isNotEmpty(period.getHost()), "host", period.getHost());
        List<CollectPeriod> list = periodMapper.selectList(wrapper);
        return RespBean.success("success", list, list.size());
    }

    @Override
    public RespBean saveTask(CollectPeriod period) {
        // check taskName
        checkName(period.getTaskName());
        AssertUtil.isTrue(!ValidatorUtil.validatePath(period.getFilePath()), "The file path is incorrect");
        Long id = IdUtils.SNOWFLAKE.nextId();
        period.setTaskId(id);
        Optional<LinuxConfig> config = operation.getLinuxConfig(period.getHost());
        AssertUtil.isTrue(!config.isPresent(), "host message is not exist");
        Session session = JschUtil.obtainSession(config.get());
        // Check and create collection.sql if necessary  Check and create stack.txt if necessary
        String pid = StringUtil.getPid(period.getPid());
        // period.getFilePath 检验/ljp/quew/  以/ 结尾
        String suffix = Constant.CHECK_SUFFIX.replace(Constant.INSERTION_UPLOADPATH, period.getFilePath());
        String command = Constant.CHECK_PREFIX + pid + suffix;
        AssertUtil.isTrue(JschUtil.executeCommand(session, command).contains("not"),
                "Process with pid " + pid + " does not exist");
        // close session
        JschUtil.closeSession(session);
        periodMapper.insert(period);
        return RespBean.success("Save success");
    }

    @Override
    public RespBean updateTask(CollectPeriod period) {
        // 状态是comp run 不能改
        CollectPeriod status = periodMapper.selectById(period.getTaskId());
        AssertUtil.isTrue(!ValidatorUtil.validatePath(period.getFilePath()), "The file path is incorrect");
        AssertUtil.isTrue(!status.getCurrentStatus().equals(Constant.TASK_STOP),
                "Running or completed tasks cannot be modified");
        // 检查taskName是否重复
        CollectPeriod existingPeriod = periodMapper.selectOne(new QueryWrapper<CollectPeriod>()
                .eq("task_name", period.getTaskName()));
        if (existingPeriod != null && !existingPeriod.getTaskId().equals(period.getTaskId())) {
            return RespBean.error("Task name already exists");
        }
        periodMapper.updateById(period);
        return RespBean.success("Update success");
    }

    @Override
    public Optional<CollectPeriod> selectById(Long id) {
        return Optional.ofNullable(periodMapper.selectById(id));
    }

    @Override
    public RespBean deleteTask(List<Long> ids) {
        periodMapper.deleteBatchIds(ids);
        return RespBean.success("delete success");
    }

    @Override
    public RespBean checkName(String name) {
        // 检查taskName是否重复
        CollectPeriod existingPeriod = periodMapper.selectOne(new QueryWrapper<CollectPeriod>()
                .eq("task_name", name));
        AssertUtil.isTrue(ObjectUtil.isNotEmpty(existingPeriod), "Task name already exists");
        return RespBean.success("ok");
    }

    @Override
    public RespBean getIps() {
        List<String> ips = hostService.list().stream().map(OpsHostEntity::getPublicIp).collect(Collectors.toList());
        return RespBean.success("success", ips);
    }

    @Override
    public RespBean getCompleteProcess() {
        List<String> pids = periodMapper.selectListPid();
        return RespBean.success("success", pids);
    }

    @Override
    public List<CollectPeriod> getListByPid(String pid) {
        return periodMapper.selectList(
                new QueryWrapper<CollectPeriod>().eq(StrUtil.isNotEmpty(pid), "pid", pid));
    }
}
