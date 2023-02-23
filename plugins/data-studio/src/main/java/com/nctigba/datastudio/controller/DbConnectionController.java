package com.nctigba.datastudio.controller;

import com.nctigba.datastudio.base.ClusterManager;
import com.nctigba.datastudio.dao.ConnectionMapDAO;
import com.nctigba.datastudio.model.dto.DbConnectionCreateDTO;
import com.nctigba.datastudio.model.entity.DatabaseConnectionDO;
import com.nctigba.datastudio.service.DbConnectionService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
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
import java.util.List;

@Slf4j
@Api(tags = {"Connection management interface"})
@RestController
@RequestMapping(value = "/dataStudio/web/v1")
public class DbConnectionController {
    @Autowired
    private ClusterManager clusterManager;

    @Resource
    private DbConnectionService dbConnectionService;

    @Resource
    private ConnectionMapDAO connectionMapDAO;

    @GetMapping(value = "/clusters", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<OpsClusterVO> allCluster() throws Exception {
        List<OpsClusterVO> list = clusterManager.getAllOpsCluster();
        log.info("cluster list is: " + list);
        return list;
    }


    @ApiOperation(value = "Add connections")
    @PostMapping(value = "/connections", produces = MediaType.APPLICATION_JSON_VALUE)
    public DatabaseConnectionDO create(@RequestBody DbConnectionCreateDTO request) throws Exception {
        return dbConnectionService.addDatabaseConnection(request);
    }


    @ApiOperation(value = "Attribute")
    @GetMapping(value = "/connections/{id}/attribute", produces = MediaType.APPLICATION_JSON_VALUE)
    public DatabaseConnectionDO attribute(@PathVariable("id") String id) throws Exception {
        return dbConnectionService.databaseAttributeConnection(id);
    }


    @ApiOperation(value = "Delete Connectionll")
    @DeleteMapping(value = "/connections/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public void deleteList(@PathVariable("id") String id) throws Exception {
        dbConnectionService.deleteDatabaseConnectionList(id);
    }

    @ApiOperation(value = "Delete connections")
    @DeleteMapping(value = "/connections/close/{uuid}", produces = MediaType.APPLICATION_JSON_VALUE)
    public void delete(@PathVariable("uuid") String uuid) throws Exception {
        connectionMapDAO.deleteConnection(uuid);
    }

    @ApiOperation(value = "Database Connectionll List")
    @GetMapping(value = "/connections", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<DatabaseConnectionDO> dataList(@RequestParam(value = "webUser") String webUser) throws Exception {
        return dbConnectionService.databaseConnectionList(webUser);
    }


    @ApiOperation(value = "Update Connections")
    @PutMapping(value = "/connections", produces = MediaType.APPLICATION_JSON_VALUE)
    public DatabaseConnectionDO updateTable(@RequestBody DbConnectionCreateDTO request) throws Exception {
        return dbConnectionService.updateDatabaseConnection(request);
    }

}
