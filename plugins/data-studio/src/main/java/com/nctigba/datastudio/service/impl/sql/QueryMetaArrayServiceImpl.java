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
 *  QueryMetaArrayServiceImpl.java
 *
 *  IDENTIFICATION
 *  plugins/data-studio/src/main/java/com/nctigba/datastudio/service/impl/sql/QueryMetaArrayServiceImpl.java
 *
 *  -------------------------------------------------------------------------
 */

package com.nctigba.datastudio.service.impl.sql;

import com.nctigba.datastudio.compatible.GainObjectSQLService;
import com.nctigba.datastudio.config.ConnectionConfig;
import com.nctigba.datastudio.model.query.DatabaseMetaArrayColumnQuery;
import com.nctigba.datastudio.model.query.DatabaseMetaArrayQuery;
import com.nctigba.datastudio.model.query.DatabaseMetaArraySchemaQuery;
import com.nctigba.datastudio.model.query.UserQuery;
import com.nctigba.datastudio.service.QueryMetaArrayService;
import lombok.extern.slf4j.Slf4j;
import org.opengauss.admin.common.exception.CustomException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import java.util.HashSet;
import java.util.Deque;
import java.util.ArrayDeque;

import static com.nctigba.datastudio.constants.CommonConstants.NAME;
import static com.nctigba.datastudio.constants.CommonConstants.OID;
import static com.nctigba.datastudio.utils.DebugUtils.comGetUuidType;

/**
 * QueryMetaArrayServiceImpl
 *
 * @since 2023-6-26
 */
@Slf4j
@Service
public class QueryMetaArrayServiceImpl implements QueryMetaArrayService {
    @Autowired
    private ConnectionConfig connectionConfig;

    private Map<String, GainObjectSQLService> gainObjectSQLService;

    /**
     * set gain object sql service
     *
     * @param SQLServiceList SQLServiceList
     */
    @Resource
    public void setGainObjectSQLService(List<GainObjectSQLService> SQLServiceList) {
        gainObjectSQLService = new HashMap<>();
        for (GainObjectSQLService service : SQLServiceList) {
            gainObjectSQLService.put(service.type(), service);
        }
    }

    @Override
    public List<String> databaseList(String uuid) {
        log.info("schemaList request is: " + uuid);
        String sql;
        List<String> databaseList = new ArrayList<>();
        try (
                Connection connection = connectionConfig.connectDatabase(uuid);
                Statement statement = connection.createStatement()
        ) {
            sql = gainObjectSQLService.get(comGetUuidType(uuid)).databaseList();
            try (
                    ResultSet resultSet = statement.executeQuery(sql)
            ) {
                while (resultSet.next()) {
                    databaseList.add(resultSet.getString("datname"));
                }
                log.info("schemaList response is: " + databaseList);
                return databaseList;

            } catch (SQLException e) {
                log.info(e.toString());
                throw new CustomException(e.getMessage());
            }
        } catch (SQLException e) {
            log.info(e.toString());
            throw new CustomException(e.getMessage());
        }
    }

    @Override
    public List<Map<String, String>> schemaList(DatabaseMetaArraySchemaQuery request) throws SQLException {
        log.info("schemaList request is: " + request);
        return gainObjectSQLService.get(comGetUuidType(request.getUuid())).schemaList(request);
    }

    @Override
    public List<String> objectList(DatabaseMetaArrayQuery request) throws SQLException {
        log.info("objectList request is: " + request);
        return gainObjectSQLService.get(comGetUuidType(request.getUuid())).objectList(request);
    }

    @Override
    public List<String> tableColumnList(DatabaseMetaArrayColumnQuery request) throws SQLException {
        log.info("tableColumnList request is: " + request);
        return gainObjectSQLService.get(comGetUuidType(request.getUuid())).tableColumnList(request);
    }

    @Override
    public List<String> baseTypeList(String uuid) throws SQLException {
        try (
                Connection connection = connectionConfig.connectDatabase(uuid);
                Statement statement = connection.createStatement();
                ResultSet resultSet = statement.executeQuery(
                        gainObjectSQLService.get(comGetUuidType(uuid)).baseTypeListSQL())
        ) {
            List<String> columnList = new ArrayList<>();
            while (resultSet.next()) {
                columnList.add(resultSet.getString("type"));
            }
            return columnList;
        }
    }

    @Override
    public List<String> tablespaceList(String uuid) throws SQLException {
        try (
                Connection connection = connectionConfig.connectDatabase(uuid);
                Statement statement = connection.createStatement();
                ResultSet resultSet = statement.executeQuery(
                        gainObjectSQLService.get(comGetUuidType(uuid)).tablespaceListSQL())
        ) {
            List<String> columnList = new ArrayList<>();
            while (resultSet.next()) {
                columnList.add(resultSet.getString(1));
            }
            return columnList;
        }
    }

    @Override
    public List<Map<String, String>> tablespaceOidList(String uuid) throws SQLException {
        try (
                Connection connection = connectionConfig.connectDatabase(uuid);
                Statement statement = connection.createStatement();
                ResultSet resultSet = statement.executeQuery(
                        gainObjectSQLService.get(comGetUuidType(uuid)).tablespaceOidListSQL())
        ) {
            List<Map<String, String>> tableSpaceList = new ArrayList<>();
            while (resultSet.next()) {
                Map<String, String> tableSpaceMap = new HashMap<>();
                tableSpaceMap.put(OID, resultSet.getString(1));
                tableSpaceMap.put(NAME, resultSet.getString(2));
                tableSpaceList.add(tableSpaceMap);
            }
            return tableSpaceList;
        }
    }

    @Override
    public UserQuery userList(String uuid) throws SQLException {
        try (
                Connection connection = connectionConfig.connectDatabase(uuid);
                Statement statement = connection.createStatement();
                ResultSet resultSet = statement.executeQuery(
                        gainObjectSQLService.get(comGetUuidType(uuid)).userSql())
        ) {
            List<Map<String, String>> userList = new ArrayList<>();
            List<Map<String, String>> roleList = new ArrayList<>();
            while (resultSet.next()) {
                if (resultSet.getString(2).equals("1")) {
                    Map<String, String> userMap = new HashMap<>();
                    userMap.put(NAME, resultSet.getString(1));
                    userMap.put(OID, resultSet.getString(3));
                    userList.add(userMap);
                } else {
                    Map<String, String> roleMap = new HashMap<>();
                    roleMap.put(NAME, resultSet.getString(1));
                    roleMap.put(OID, resultSet.getString(3));
                    roleList.add(roleMap);
                }
            }
            UserQuery userQuery = new UserQuery();
            userQuery.setUser(userList);
            userQuery.setRole(roleList);
            return userQuery;
        }
    }

    @Override
    public List<String> resourceList(String uuid) throws SQLException {
        try (
                Connection connection = connectionConfig.connectDatabase(uuid);
                Statement statement = connection.createStatement();
                ResultSet resultSet = statement.executeQuery(
                        gainObjectSQLService.get(comGetUuidType(uuid)).resourceListSQL())
        ) {
            List<String> resourceList = new ArrayList<>();
            while (resultSet.next()) {
                resourceList.add(resultSet.getString(1));
            }
            return resourceList;
        }
    }

    @Override
    public List<Map<String, String>> userMemberList(String uuid) throws SQLException {
        try (
                Connection connection = connectionConfig.connectDatabase(uuid);
                Statement statement = connection.createStatement();
                ResultSet userInfo = statement.executeQuery(
                        gainObjectSQLService.get(comGetUuidType(uuid)).currentUserInfoSQL())
        ) {
            // 目前尝试实现方法：记忆化BFS
            String currentRoleId = "";
            String currentRoleName = "";
            boolean isSystemAdmin = false;
            while (userInfo.next()) {
                currentRoleId = userInfo.getString(1);
                currentRoleName = userInfo.getString(2);
                isSystemAdmin = userInfo.getBoolean(3);
            }
            if (currentRoleId.isBlank()) {
                throw new CustomException("Get current user information failed!");
            }
            return userMemberBfs(uuid, currentRoleId, currentRoleName, isSystemAdmin);
        }
    }

    private List<Map<String, String>> userMemberBfs(String uuid, String roleId,
                                                    String rolName, boolean isSystemAdmin) throws SQLException {
        try (
                Connection connection = connectionConfig.connectDatabase(uuid);
                Statement statement = connection.createStatement()
        ) {
            List<Map<String, String>> userMemberList = new ArrayList<>();

            // 该用户是系统管理员用户
            if (isSystemAdmin) {
                log.info("user " + rolName + "is System Admin.");
                return userMemberAdmin(uuid, roleId);
            }

            // 该用户不是系统管理员用户
            log.info("user" + rolName + "is not System Admin.");

            // BFS
            Set<String> set = new HashSet<>();
            Deque<String> deque = new ArrayDeque<>();
            // 添加自己
            Map<String, String> self = new HashMap<>();
            self.put(OID, roleId);
            self.put(NAME, rolName);
            userMemberList.add(self);
            set.add(roleId);
            deque.offer(roleId);

            // bfs方式添加所有的直接和间接成员
            while (!deque.isEmpty()) {
                int len = deque.size();
                for (int i = 0; i < len; i++) {
                    String curRoleId = deque.poll();
                    List<Map<String, String>> subUserMemberList =
                            userMemberBfsHelper(uuid, curRoleId, set, deque);
                    userMemberList.addAll(subUserMemberList);
                }
            }

            // 添加上系统管理员用户
            try (
                    ResultSet systemAdminResult = statement.executeQuery(
                            gainObjectSQLService.get(comGetUuidType(uuid)).getAllSystemAdminSQL())
            ) {
                while (systemAdminResult.next()) {
                    String systemAdminRoleId = systemAdminResult.getString(1);
                    if (set.contains(systemAdminRoleId)) {
                        continue;
                    }
                    Map<String, String> map = new HashMap<>();
                    map.put(OID, systemAdminRoleId);
                    map.put(NAME, systemAdminResult.getString(2));
                    userMemberList.add(map);
                }
            }

            // 基于用户名称进行升序排序，之后返回结果
            userMemberList.sort((o1, o2) ->
                    o1.values().iterator().next().compareTo(o2.values().iterator().next()));

            return userMemberList;
        }
    }

    private List<Map<String, String>> userMemberAdmin(String uuid, String roleId) throws SQLException {
        try (
                Connection connection = connectionConfig.connectDatabase(uuid);
                Statement statement = connection.createStatement();
                ResultSet adminResultSet = statement.executeQuery(
                        gainObjectSQLService.get(comGetUuidType(uuid)).userMemberSQL(roleId, true))
        ) {
            List<Map<String, String>> adminMemberList = new ArrayList<>();
            while (adminResultSet.next()) {
                Map<String, String> map = new HashMap<>();
                map.put(OID, adminResultSet.getString(1));
                map.put(NAME, adminResultSet.getString(2));
                adminMemberList.add(map);
            }
            return adminMemberList;
        }
    }

    private List<Map<String, String>> userMemberBfsHelper(String uuid, String curRoleId,
                                                          Set<String> set, Deque<String> deque) throws SQLException {
        try (
                Connection connection = connectionConfig.connectDatabase(uuid);
                Statement statement = connection.createStatement();
                ResultSet resultSet = statement.executeQuery(
                        gainObjectSQLService.get(comGetUuidType(uuid)).userMemberSQL(curRoleId, false))
        ) {
            List<Map<String, String>> subUserMemberList = new ArrayList<>();
            while (resultSet.next()) {
                String masterId = resultSet.getString(1);
                if (!set.contains(masterId)) {
                    Map<String, String> map = new HashMap<>();
                    map.put(OID, masterId);
                    map.put(NAME, resultSet.getString(2));
                    subUserMemberList.add(map);
                    set.add(masterId);
                    deque.offer(masterId);
                }
            }
            return subUserMemberList;
        }
    }
}