/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2024-2024. All rights reserved.
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
 * JschExecutorFacade.java
 *
 * IDENTIFICATION
 * openGauss-visualtool
 * /visualtool-service/src/main/java/org/opengauss/admin/system/plugin/facade/JschExecutorFacade.java
 *
 * -------------------------------------------------------------------------
 */

package org.opengauss.admin.system.plugin.facade;

import com.jcraft.jsch.Session;

import org.opengauss.admin.system.plugin.beans.SshLogin;
import org.opengauss.admin.system.service.JschExecutorService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * Jsch Executor Facade operations
 *
 * @author wangchao
 * @since 2024/10/30 9:41
 **/
@Service
public class JschExecutorFacade {
    @Resource
    private JschExecutorService jschExecutorService;

    /**
     * Get the os of the remote machine
     *
     * @param sshLogin ssh login info
     * @return os
     */
    public String getOs(SshLogin sshLogin) {
        return jschExecutorService.getOs(sshLogin);
    }

    /**
     * Get the os version of the remote machine
     *
     * @param sshLogin ssh login info
     * @return os version
     */
    public String getOsVersion(SshLogin sshLogin) {
        return jschExecutorService.getOsVersion(sshLogin);
    }

    /**
     * Get the cpu architecture of the remote machine
     *
     * @param sshLogin ssh login info
     * @return cpu architecture
     */
    public String getCpuArch(SshLogin sshLogin) {
        return jschExecutorService.getCpuArch(sshLogin);
    }

    /**
     * Get the cpu info of the remote machine
     *
     * @param sshLogin ssh login info
     * @param hasNetName true 包含网卡名称，false 不包含网卡名称
     * @return net info
     */
    public String[] getNetMonitor(SshLogin sshLogin, boolean hasNetName) {
        return jschExecutorService.getNetMonitor(sshLogin, hasNetName);
    }

    /**
     * 获取cpu使用率
     *
     * @param sshLogin ssh登录信息
     * @return cpu使用率
     */
    public String getCpuUsing(SshLogin sshLogin) {
        return jschExecutorService.getCpuUsing(sshLogin);
    }

    /**
     * 获取内存使用率
     *
     * @param sshLogin ssh登录信息
     * @return 内存使用率
     */
    public String getMemoryUsing(SshLogin sshLogin) {
        return jschExecutorService.getMemoryUsing(sshLogin);
    }

    /**
     * 获取cpu核心数
     *
     * @param sshLogin ssh登录信息
     * @return cpu核心数
     */
    public String getCpuCoreNum(SshLogin sshLogin) {
        return jschExecutorService.getCpuCoreNum(sshLogin);
    }

    /**
     * 获取内存总量
     *
     * @param sshLogin ssh登录信息
     * @return 内存总量
     */
    public String getMemoryTotal(SshLogin sshLogin) {
        return jschExecutorService.getMemoryTotal(sshLogin);
    }

    /**
     * 获取openGasuus版本号
     *
     * @param session ssh会话
     * @param envPath 环境路径
     * @return openGasuus版本号
     */
    public String getOpenGaussMainVersionNum(Session session, String envPath) {
        return jschExecutorService.getOpenGaussMainVersionNum(session, envPath);
    }

    /**
     * 检查java版本号
     *
     * @param sshLogin ssh登录信息
     * @return java版本号
     */
    public String checkJavaVersion(SshLogin sshLogin) {
        return jschExecutorService.checkJavaVersion(sshLogin);
    }

    /**
     * 检查端口是否被占用
     *
     * @param sshLogin ssh登录信息
     * @param port 端口号
     * @return true 未被占用，false 被占用
     * @author wangchao
     * &#064;date  2022/10/27 15:08
     * @since 7.0.0
     */
    public boolean checkOsPortConflict(SshLogin sshLogin, int port) {
        return jschExecutorService.checkOsPortConflict(sshLogin, port);
    }

    /**
     * 执行命令
     *
     * @param sshLogin ssh登录信息
     * @param command 命令
     * @return 命令执行结果
     */
    public String execCommand(SshLogin sshLogin, String command) {
        return jschExecutorService.execCommand(sshLogin, command);
    }

    /**
     * 执行ssh远程命令，并获取返回值；执行命令时，需要指定环境变量
     *
     * @param sshLogin ssh登录信息
     * @param command 命令
     * @param envPath 环境变量路径
     * @return 命令执行结果
     */
    public String execCommand(SshLogin sshLogin, String command, String envPath) {
        return jschExecutorService.execCommand(sshLogin, command, envPath);
    }

    /**
     * 检查路径是否为空，为空返回true
     *
     * @param sshLogin ssh登录信息
     * @param path 路径
     * @return true 路径为空，false 路径不为空
     */
    public boolean checkPathEmpty(SshLogin sshLogin, String path) {
        return jschExecutorService.checkPathEmpty(sshLogin, path);
    }

    /**
     * 检查文件是否存在，存在返回true
     *
     * @param sshLogin ssh登录信息
     * @param file 文件绝对路径
     * @return true 文件存在，false 文件不存在
     */
    public boolean checkFileExist(SshLogin sshLogin, String file) {
        return jschExecutorService.checkFileExist(sshLogin, file);
    }
}
