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

import com.gitee.starblues.bootstrap.annotation.AutowiredType;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.opengauss.admin.common.core.domain.entity.ops.OpsJdbcDbClusterNodeEntity;
import org.opengauss.admin.common.enums.ops.DbTypeEnum;
import org.opengauss.admin.common.exception.ServiceException;
import org.opengauss.admin.system.service.ops.IOpsJdbcDbClusterNodeService;
import org.opengauss.admin.system.service.ops.impl.EncryptionUtils;
import org.opengauss.collect.config.common.DataBaseType;
import org.opengauss.collect.domain.Assessment;
import org.opengauss.collect.domain.vo.AssessLibraryVo;
import org.opengauss.collect.mapper.AssessmentMapper;
import org.opengauss.collect.service.Evaluate;
import org.opengauss.collect.utils.AssertUtil;
import org.opengauss.collect.utils.ConnectionUtils;
import org.opengauss.collect.utils.response.RespBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * LinuxConfigController
 *
 * @author liu
 * @since 2022-10-01
 */
@Service
public class AssessmentImpl implements Evaluate {
    @Autowired
    @AutowiredType(AutowiredType.Type.PLUGIN_MAIN)
    private IOpsJdbcDbClusterNodeService nodeService;

    @Autowired
    private AssessmentMapper assessmentMapper;

    @Autowired
    @AutowiredType(AutowiredType.Type.PLUGIN_MAIN)
    private EncryptionUtils encryptionUtils;

    @Override
    public RespBean obtainMysqlByIP(String ip) {
        return obtainDataBaseByIP(ip, DataBaseType.MYSQL);
    }

    @Override
    public RespBean obtainOpenGaussByIP(String ip) {
        return obtainDataBaseByIP(ip, DataBaseType.OPENGAUSS);
    }

    private RespBean obtainDataBaseByIP(String ip, String type) {
        Optional<OpsJdbcDbClusterNodeEntity> optional = getAllDataBase().stream()
                .filter(item -> item.getIp().equals(ip) && item.getUrl().contains(type)).findFirst();
        AssertUtil.isTrue(optional.isEmpty(), "Evaluation does not have the database information");
        OpsJdbcDbClusterNodeEntity entity = optional.get();
        List<String> dbNames = getDatabaseNames(entity, type);
        entity.setPassword("");
        AssessLibraryVo vo = new AssessLibraryVo(entity, dbNames);
        return RespBean.success("success", vo);
    }

    private List<String> getDatabaseNames(OpsJdbcDbClusterNodeEntity node, String type) {
        String databaseQuery;
        String resultColumn;
        if (type.equals(DataBaseType.OPENGAUSS)) {
            databaseQuery = DataBaseType.GAUSS_DATABASE;
            resultColumn = DataBaseType.GAUSS_RES;
        } else {
            databaseQuery = DataBaseType.MYSQL_DATABASE;
            resultColumn = DataBaseType.MYSQL_RES;
        }
        String driverClass = DbTypeEnum.OPENGAUSS.getDriverClass();
        if (type.equals(DataBaseType.MYSQL)) {
            driverClass = DbTypeEnum.MYSQL.getDriverClass();
        }
        List<String> databaseNames = new ArrayList<>();
        try {
            try (Connection connection = ConnectionUtils.getConnection(driverClass, node.getUrl(), node.getUsername(),
                    encryptionUtils.decrypt(node.getPassword()));
                 PreparedStatement preparedStatement = connection.prepareStatement(databaseQuery);
                 ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) {
                    databaseNames.add(resultSet.getString(resultColumn));
                }
            }
        } catch (SQLException e) {
            throw new ServiceException(e.getMessage());
        }
        return databaseNames;
    }

    @Override
    public RespBean obtainMysqlIp() {
        List<String> ips = getAllDataBase().stream()
                .filter(item -> item.getUrl().contains(DataBaseType.MYSQL))
                .map(OpsJdbcDbClusterNodeEntity::getIp)
                .collect(Collectors.toList());
        return RespBean.success("success", ips);
    }

    @Override
    public RespBean openGaussIp() {
        List<String> ips = getAllDataBase().stream()
                .filter(item -> item.getUrl().contains(DataBaseType.OPENGAUSS))
                .map(OpsJdbcDbClusterNodeEntity::getIp)
                .collect(Collectors.toList());
        return RespBean.success("success", ips);
    }

    @Override
    public int saveAssessMent(Assessment assessment) {
        return assessmentMapper.insert(assessment);
    }

    @Override
    public RespBean obtainAllEvaluationResults(Assessment assessment, int pageNum, int pageSize) {
        List<Assessment> list = assessmentMapper.selectAllOrderByStartTimeDesc();
        List<Assessment> paginatedList = list.stream()
                .skip((pageNum - 1) * pageSize)
                .limit(pageSize)
                .map(item -> {
                    item.setOpengaussPassword("");
                    item.setMysqlPassword("");
                    return item;
                })
                .collect(Collectors.toList());
        return RespBean.success("success", paginatedList, list.size());
    }

    @Override
    public RespBean deleteAssess(Long assessmentId) {
        return RespBean.success("success", assessmentMapper.deleteById(assessmentId));
    }

    private List<OpsJdbcDbClusterNodeEntity> getAllDataBase() {
        return Optional.ofNullable(nodeService.list()).orElse(Collections.emptyList());
    }
}
