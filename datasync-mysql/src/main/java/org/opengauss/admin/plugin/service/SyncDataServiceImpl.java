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
 * SyncDataServiceImpl.java
 *
 * IDENTIFICATION
 * datasync-mysql/src/main/java/org/opengauss/admin/plugin/service/SyncDataServiceImpl.java
 *
 * -------------------------------------------------------------------------
 */


package org.opengauss.admin.plugin.service;

import cn.hutool.core.bean.BeanUtil;
import lombok.extern.slf4j.Slf4j;
import org.opengauss.admin.plugin.bean.SyncConfigDto;
import org.opengauss.admin.plugin.bean.SyncConstants;
import org.opengauss.admin.plugin.util.LocalCache;
import org.opengauss.admin.plugin.util.ShellUtil;
import org.opengauss.admin.plugin.util.TextUtil;
import org.springframework.stereotype.Service;

import java.io.File;
import java.sql.*;

/**
 * @className: SyncDataServiceImpl
 * @description: SyncDataServiceImpl
 * @author: xielibo
 * @date: 2022-10-28 17:21
 **/
@Service
@Slf4j
public class SyncDataServiceImpl {


    /**
     * Execute sync script
     *
     * @param configDto
     */
    public boolean syncHandler(SyncConfigDto configDto) {
        LocalCache.put(SyncConstants.CACHE_KEY_SYNC_PARAMS, configDto);
        String workDir = "/opt/files/openGauss-tools-chameleon";
        String shellName = "sync_shell.sh";
        String shellPath = workDir + File.separator + shellName;
        TextUtil.replaceTemplateWriteFile(BeanUtil.beanToMap(configDto), shellPath);
        String result = ShellUtil.execForStr(new File(workDir), "/bin/bash", shellName);
        log.info("Synchronize script execution results,{}", result);
        if (result.contains("ClientCannotConnectError")) {
            log.error("exec error");
            syncCloseHandler();
            return false;
        }
        return true;
    }

    /**
     * Execute shutdown sync script
     */
    public void syncCloseHandler() {
        LocalCache.remove(SyncConstants.CACHE_KEY_SYNC_PARAMS);
        String workDir = "/opt/files/openGauss-tools-chameleon";
        String shellName = "sync_close_shell.sh";
        String shellPath = workDir + File.separator + shellName;
        TextUtil.writeFile(shellPath);
        String result = ShellUtil.execForStr(new File(workDir), "/bin/bash", shellName);
        log.info("Turn off synchronous script execution results,{}", result);
    }

    /**
     * Check if the synchronized schema exists
     *
     * @return
     */
    public boolean checkSyncSuccess(SyncConfigDto configDto) {
        Connection connection = null;
        Statement statement = null;
        ResultSet resultSet = null;
        try {
            String url = String.format("jdbc:opengauss://%s:%s/%s?user=%s&password=%s", configDto.getOgHost(),
                    configDto.getOgPort(), configDto.getOgDatabase(), configDto.getOgUser(), configDto.getOgPass());
            Class.forName("org.opengauss.Driver");
            connection = DriverManager.getConnection(url);

            String sql = String.format("SELECT count(1) FROM pg_namespace t where t.nspname = '%s'", configDto.getOgSchema());
            statement = connection.createStatement();
            resultSet = statement.executeQuery(sql);
            resultSet.next();
            Integer count = resultSet.getInt(1);
            if (count == 0) {
                return false;
            }
            String sqlTable = String.format("SELECT count(1) FROM pg_tables WHERE SCHEMANAME = '%s'", configDto.getOgSchema());
            resultSet = statement.executeQuery(sqlTable);
            resultSet.next();
            Integer tableCount = resultSet.getInt(1);
            if (tableCount > 0) {
                syncCloseHandler();
                return true;
            }
        } catch (SQLException | ClassNotFoundException e) {
        } finally {
            if (resultSet != null) {
                try {
                    resultSet.close();
                } catch (SQLException e) {
                }
            }
            if (statement != null) {
                try {
                    statement.close();
                } catch (SQLException e) {
                }
            }
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException e) {
                }
            }
        }
        return false;
    }

}
