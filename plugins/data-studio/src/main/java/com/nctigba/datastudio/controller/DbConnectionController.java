/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package com.nctigba.datastudio.controller;

import com.nctigba.datastudio.base.ClusterManager;
import com.nctigba.datastudio.dao.ConnectionMapDAO;
import com.nctigba.datastudio.model.dto.DbConnectionCreateDTO;
import com.nctigba.datastudio.model.dto.GetConnectionAttributeDTO;
import com.nctigba.datastudio.model.entity.DatabaseConnectionDO;
import com.nctigba.datastudio.service.DbConnectionService;
import lombok.extern.slf4j.Slf4j;
import org.opengauss.admin.common.core.domain.model.ops.OpsClusterVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.sql.SQLException;
import java.util.List;

/**
 * DbConnectionController
 *
 * @since 2023-6-26
 */
@Slf4j
@RestController
@RequestMapping(value = "/dataStudio/web/v1")
public class DbConnectionController {
    @Autowired
    private ClusterManager clusterManager;

    @Resource
    private DbConnectionService dbConnectionService;

    @Resource
    private ConnectionMapDAO connectionMapDAO;

    /**
     * get all cluster
     *
     * @return List
     */
    @GetMapping(value = "/clusters", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<OpsClusterVO> allCluster() {
        List<OpsClusterVO> list = clusterManager.getAllOpsCluster();
        log.info("cluster list is: " + list);
        return list;
    }

    /**
     * connection database
     *
     * @param request request
     * @return DatabaseConnectionDO
     */
    @PostMapping(value = "/connections", produces = MediaType.APPLICATION_JSON_VALUE)
    public DatabaseConnectionDO create(@RequestBody DbConnectionCreateDTO request) {
        return dbConnectionService.addDatabaseConnection(request);
    }

    /**
     * database attribute
     *
     * @param request request
     * @return DatabaseConnectionDO
     */
    @GetMapping(value = "/connections/attribute", produces = MediaType.APPLICATION_JSON_VALUE)
    public DatabaseConnectionDO attribute(GetConnectionAttributeDTO request) {
        return dbConnectionService.databaseAttributeConnection(request);
    }

    /**
     * delete database connection list
     *
     * @param id id
     */
    @DeleteMapping(value = "/connections/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public void deleteList(@PathVariable("id") String id) {
        dbConnectionService.deleteDatabaseConnectionList(id);
    }

    /**
     * delete connection
     *
     * @param uuid uuid
     * @throws SQLException SQLException
     */
    @DeleteMapping(value = "/connections/close/{uuid}", produces = MediaType.APPLICATION_JSON_VALUE)
    public void delete(@PathVariable("uuid") String uuid) throws SQLException {
        connectionMapDAO.deleteConnection(uuid);
    }

    /**
     * database connection list
     *
     * @param webUser webUser
     * @return List
     */
    @GetMapping(value = "/connections", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<DatabaseConnectionDO> dataList(@RequestParam(value = "webUser") String webUser) {
        return dbConnectionService.databaseConnectionList(webUser);
    }

    /**
     * update database connection
     *
     * @param request request
     * @return DatabaseConnectionDO
     * @throws SQLException SQLException
     */
    @PutMapping(value = "/connections", produces = MediaType.APPLICATION_JSON_VALUE)
    public DatabaseConnectionDO updateTable(@RequestBody DbConnectionCreateDTO request) throws SQLException {
        return dbConnectionService.updateDatabaseConnection(request);
    }

}
