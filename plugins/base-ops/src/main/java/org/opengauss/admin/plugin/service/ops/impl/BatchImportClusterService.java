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
 * BatchImportClusterService.java
 *
 * IDENTIFICATION
 * base-ops/src/main/java/org/opengauss/admin/plugin/service/ops/impl/BatchImportClusterService.java
 *
 * -------------------------------------------------------------------------
 */

package org.opengauss.admin.plugin.service.ops.impl;

import static org.opengauss.admin.common.constant.ops.SshCommandConstants.CHECK_ENTERPRISE_VERSION;
import static org.opengauss.admin.common.constant.ops.SshCommandConstants.CHECK_LITE_VERSION;
import static org.opengauss.admin.common.constant.ops.SshCommandConstants.CHECK_MINILIST_VERSION;
import static org.opengauss.admin.plugin.enums.ops.ClusterRoleEnum.MASTER;
import static org.opengauss.admin.plugin.enums.ops.ClusterRoleEnum.SLAVE;
import static org.opengauss.admin.plugin.enums.ops.DeployTypeEnum.CLUSTER;
import static org.opengauss.admin.plugin.enums.ops.DeployTypeEnum.SINGLE_NODE;
import static org.opengauss.admin.plugin.enums.ops.OpenGaussVersionEnum.ENTERPRISE;
import static org.opengauss.admin.plugin.enums.ops.OpenGaussVersionEnum.LITE;
import static org.opengauss.admin.plugin.enums.ops.OpenGaussVersionEnum.MINIMAL_LIST;

import com.alibaba.excel.EasyExcel;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.gitee.starblues.bootstrap.annotation.AutowiredType;
import com.jcraft.jsch.Session;

import cn.hutool.core.util.StrUtil;
import lombok.extern.slf4j.Slf4j;

import org.opengauss.admin.common.enums.SysLanguage;
import org.opengauss.admin.common.exception.ops.OpsException;
import org.opengauss.admin.plugin.domain.entity.ops.OpsClusterEntity;
import org.opengauss.admin.plugin.domain.entity.ops.OpsClusterNodeEntity;
import org.opengauss.admin.plugin.domain.entity.ops.OpsImportEntity;
import org.opengauss.admin.plugin.domain.entity.ops.OpsImportSshEntity;
import org.opengauss.admin.plugin.domain.entity.ops.OpsParseExcelEntity;
import org.opengauss.admin.plugin.enums.ops.ClusterRoleEnum;
import org.opengauss.admin.plugin.listener.OpsImportEntityListener;
import org.opengauss.admin.plugin.mapper.ops.OpsClusterMapper;
import org.opengauss.admin.plugin.mapper.ops.OpsImportSshMapper;
import org.opengauss.admin.plugin.service.ops.IOpsClusterNodeService;
import org.opengauss.admin.plugin.utils.DBUtil;
import org.opengauss.admin.plugin.utils.OpsAssert;
import org.opengauss.admin.system.plugin.beans.SshLogin;
import org.opengauss.admin.system.plugin.facade.JschExecutorFacade;
import org.opengauss.admin.system.service.ops.impl.EncryptionUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;

/**
 * BatchImportClusterService 原有集群批量导入功能重构
 * <p>
 * &#064;author:  wangchao
 * &#064;Date:  2024/11/4 12:02
 * &#064;Description:  ImportClusterService
 *
 * @since 7.0.0
 **/
@Slf4j
@Service
public class BatchImportClusterService extends ServiceImpl<OpsClusterMapper, OpsClusterEntity> {
    private static final String ENTER_CONSTANT = "ENTERPRISE";
    private static final String LITE_CONSTANT = "LITE";
    private static final String MINI_CONSTANT = "MINIMAL_LIST";
    private static final String OTHER_CONSTANT = "OTHER";
    private static final String NODE_PRIMARY = "Primary";
    private static final String NODE_STANDBY = "Standby";

    private final OpsClusterEntity opsClusterEntity = new OpsClusterEntity();
    private final List<OpsClusterEntity> opsClusterEntityList = new ArrayList<>();
    private final List<OpsClusterNodeEntity> opsClusterNodeEntityList = new ArrayList<>();

    @Resource
    @AutowiredType(AutowiredType.Type.PLUGIN_MAIN)
    private EncryptionUtils encryptionUtils;
    @Resource
    @AutowiredType(AutowiredType.Type.PLUGIN_MAIN)
    private JschExecutorFacade jschExecutorFacade;
    @Resource
    private DBUtil dbUtil;
    @Resource
    private IOpsClusterNodeService opsClusterNodeService;
    @Resource
    private OpsImportSshMapper opsImportSshMapper;
    private int importSuccessCount;
    private boolean isInfoAndConn;
    private boolean isMasterNormal;
    @Resource
    private OpsClusterMapper opsClusterMapper;

    /**
     * downloadImportFile
     *
     * @param response response
     * @param currentLocale currentLocale
     */
    public void downloadImportFile(HttpServletResponse response, String currentLocale) {
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setCharacterEncoding("utf-8");
        String fileName = null;
        fileName = URLEncoder.encode("模板", StandardCharsets.UTF_8).replaceAll("\\+", "%20");
        response.setHeader("Content-disposition", "attachment;filename*=utf-8''" + fileName + ".xlsx");
        List<OpsImportEntity> usersList = new ArrayList<>();
        OpsImportEntity opsImportEntity = new OpsImportEntity("1", "ip1,ip2", "ip1,ip2", "omm", "gaussdb", "12345",
            5432, "/home/omm/cluster_2024.bashrc", null, null);
        usersList.add(opsImportEntity);
        writeZhOrEnInfo(response, currentLocale, usersList);
    }

    /**
     * dwonloadErrorFile
     *
     * @param response response
     * @param usersList usersList
     * @param currentLocale currentLocale
     */
    public void downloadErrorFile(HttpServletResponse response, List<OpsImportEntity> usersList, String currentLocale) {
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setCharacterEncoding("utf-8");
        String fileName = null;
        fileName = URLEncoder.encode("模板", StandardCharsets.UTF_8).replaceAll("\\+", "%20");
        response.setHeader("Content-disposition", "attachment;filename*=utf-8''" + fileName + ".xlsx");
        writeZhOrEnInfo(response, currentLocale, usersList);
    }

    /**
     * import success count
     *
     * @return count
     */
    public int responseImportSuccessCount() {
        return importSuccessCount;
    }

    private void markErrorInfo(OpsImportEntity opsImportEntity) {
        isInfoAndConn = false;
        opsImportEntity.setImportStatus("fail");
    }

    /**
     * upload import file
     *
     * @param file file
     * @return opsImportEntityList
     */
    public List<OpsImportEntity> uploadImportFile(MultipartFile file) {
        List<OpsImportEntity> opsImportEntityList = new ArrayList<>();
        OpsImportEntityListener userDataListener = new OpsImportEntityListener();
        try {
            opsImportEntityList = EasyExcel.read(file.getInputStream(), OpsImportEntity.class, userDataListener)
                .sheet()
                .doReadSync();
        } catch (IOException e) {
            log.error("upload fail!" + e);
            throw new OpsException("upload fail!");
        }
        return opsImportEntityList;
    }

    /**
     * parse excel
     *
     * @param list list
     * @return list
     */
    public List<OpsImportEntity> parseExcel(List<OpsImportEntity> list) {
        importSuccessCount = 0;
        if (list == null || list.isEmpty()) {
            return list;
        }
        Session session = null;
        for (int i = 0; i < list.size(); i++) {
            OpsImportEntity clusterInfo = list.get(i);
            try {
                clusterInfo.checkConfig();
            } catch (OpsException e) {
                clusterInfo.setImportStatus("fail");
                clusterInfo.setErrorInfo("please fill in all the blanks!" + e);
                continue;
            }
            isInfoAndConn = true;
            isMasterNormal = false;
            String[] hostIps = clusterInfo.getPublicIp().split(",");
            for (int j = 0; j < hostIps.length; j++) {
                try {
                    String hostIp = hostIps[j];
                    OpsImportSshEntity hostUserInfos = getExistHostUserInfos(hostIp, clusterInfo);
                    OpsAssert.nonNull(hostUserInfos,
                        "hostUser is not exit : " + hostIp + ":" + clusterInfo.getInstallUsername());
                    SshLogin sshLogin = new SshLogin(hostIps[0], hostUserInfos.getPort(),
                        clusterInfo.getInstallUsername(), encryptionUtils.decrypt(hostUserInfos.getPassword()));
                    String versionType = judgeVersionType(sshLogin, clusterInfo);
                    OpsParseExcelEntity opsParseExcelEntity = new OpsParseExcelEntity(versionType, sshLogin,
                        clusterInfo, hostUserInfos, hostIps[j], j, hostIps.length);
                    selectClusterInfo(opsParseExcelEntity);
                } catch (OpsException opsException) {
                    markErrorInfo(clusterInfo);
                    clusterInfo.setErrorInfo("selectClusterInfo opsException:" + opsException);
                } finally {
                    closeSession(session);
                }
            }
            boolean isSameCluster = isClusterExist(hostIps, clusterInfo);
            saveCluster(isSameCluster, clusterInfo);
        }
        return list;
    }

    private void saveCluster(boolean isSameCluster, OpsImportEntity opsImportEntity) {
        if (!isSameCluster && isInfoAndConn && isMasterNormal) {
            saveBatch(opsClusterEntity, opsClusterNodeEntityList, opsImportEntity);
        }
        opsClusterEntityList.clear();
        opsClusterNodeEntityList.clear();
    }

    /**
     * batch save cluster
     *
     * @param opsClusterEntity opsClusterEntity
     * @param opsClusterNodeEntityList opsClusterNodeEntityList
     * @param opsImportEntity opsImportEntity
     */
    @Transactional
    public void saveBatch(OpsClusterEntity opsClusterEntity, List<OpsClusterNodeEntity> opsClusterNodeEntityList,
        OpsImportEntity opsImportEntity) {
        try {
            save(opsClusterEntity);
            opsClusterNodeService.saveBatch(opsClusterNodeEntityList);
            importSuccessCount++;
            opsImportEntity.setImportStatus("success");
        } catch (OpsException e) {
            log.error("database saveBatch error!");
            opsImportEntity.setImportStatus("fail");
            opsImportEntity.setErrorInfo("please change clusterId that already exists!");
        }
    }

    private boolean isClusterExist(String[] hosts, OpsImportEntity opsImportEntity) {
        LambdaQueryWrapper<OpsClusterEntity> queryWrapper = Wrappers.lambdaQuery(OpsClusterEntity.class)
                .eq(OpsClusterEntity::getClusterId, opsImportEntity.getClusterName());
        List<OpsClusterEntity> getSameNameCluster = opsClusterMapper.selectList(queryWrapper);
        opsClusterEntity.setDeployType(hosts.length > 1 ? CLUSTER : SINGLE_NODE);
        if (getSameNameCluster.size() > 0) {
            opsImportEntity.setImportStatus("fail");
            opsImportEntity.setErrorInfo("The clusterName is already exist.");
            return true;
        }
        for (String host : hosts) {
            List<String> clusterId = opsImportSshMapper.checkPublicIpAndPort(host, opsImportEntity.getPort() + "");
            if (clusterId.size() > 0) {
                opsImportEntity.setImportStatus("fail");
                opsImportEntity.setErrorInfo("The public IP and port that are inputted already exist, "
                    + "Similar to it are :" + clusterId);
                return true;
            }
        }
        return false;
    }

    private void selectClusterInfo(OpsParseExcelEntity opsParseExcelEntity) {
        String envpath = opsParseExcelEntity.getOpsImportEntity().getEnvPath();
        String enterAndLiteGaussHome = String.format("source %s;echo $GAUSSHOME", envpath);
        String versionNum = String.format("source %s;gsql -V|awk '{print $3}'", envpath);
        String command = null;
        if (opsParseExcelEntity.getVersionType().equals(ENTER_CONSTANT)) {
            String enterPGDATA = String.format("source %s;gs_om -t view|awk '/^sshChannel 1:%s/,"
                + "/^============================================================/'"
                + "|grep datanodeLocalDataPath|awk -F ':' '{print $2}'", envpath, opsParseExcelEntity.getPublicIp());
            String enterJudgeMasterOrSlave = String.format(
                "source %s;gs_om -t query|grep %s|" + "awk '{print $7}'", envpath, opsParseExcelEntity.getPublicIp());
            command = enterAndLiteGaussHome + ";" + enterPGDATA + ";" + versionNum + ";" + enterJudgeMasterOrSlave;
        } else if (opsParseExcelEntity.getVersionType().equals(LITE_CONSTANT)) {
            String liteGaussData = String.format("source %s;echo $GAUSSDATA", envpath);
            command = enterAndLiteGaussHome + ";" + liteGaussData + ";" + versionNum;
        } else if (opsParseExcelEntity.getVersionType().equals(MINI_CONSTANT)) {
            versionNum = "gsql -V|awk '{print $3}'";
            String miniGaussHome = String.format("source %s;echo $GAUSSHOME", envpath);
            String miniPgData = String.format("source %s;echo $PGDATA", envpath);
            command = miniGaussHome + ";" + miniPgData + ";" + versionNum;
        } else {
            throw new OpsException("It is not any of the openGauss versions.");
        }
        List<String> resultList = new ArrayList<>();
        try {
            String result = jschExecutorFacade.execCommand(opsParseExcelEntity.getSshLogin(), command);
            String regex = "(\\S+)";
            Pattern pattern = Pattern.compile(regex);
            Matcher matcher = pattern.matcher(result);
            while (matcher.find()) {
                resultList.add(matcher.group());
            }
        } catch (OpsException e) {
            log.error("command fail, please check the excel info and database envFile!");
        }
        if (opsParseExcelEntity.getVersionType().equals(ENTER_CONSTANT)) {
            checkEnterNodesNum(opsParseExcelEntity, opsParseExcelEntity.getSshLogin());
        }
        packingClusterInfo(resultList, opsParseExcelEntity);
    }

    private void checkEnterNodesNum(OpsParseExcelEntity opsParseExcelEntity, SshLogin sshLogin) {
        String enterNodesCommand = String.format("source %s;gs_om -t query|grep %s|awk '{print $3}'",
            opsParseExcelEntity.getOpsImportEntity().getEnvPath(), opsParseExcelEntity.getOpsImportEntity().getPort());
        List<String> nodesList = new ArrayList<>();
        try {
            String enterNodesIp = jschExecutorFacade.execCommand(sshLogin, enterNodesCommand);
            String regex = "(\\S+)";
            Pattern pattern = Pattern.compile(regex);
            Matcher matcher = pattern.matcher(enterNodesIp);
            while (matcher.find()) {
                nodesList.add(matcher.group());
            }
            if (nodesList.size() != opsParseExcelEntity.getIpNum()) {
                markErrorInfo(opsParseExcelEntity.getOpsImportEntity());
                opsParseExcelEntity.getOpsImportEntity().setErrorInfo("please check nodeNum is or not enough!");
            }
        } catch (OpsException e) {
            log.error("command fail, please check the excel info and database env file!");
        }
    }

    private void packingClusterInfo(List<String> resultList, OpsParseExcelEntity opsParseExcelEntity) {
        opsClusterEntity.setClusterId(opsParseExcelEntity.getOpsImportEntity().getClusterName());
        opsClusterEntity.setVersion(opsParseExcelEntity.getVersionType().equals(ENTER_CONSTANT)
            ? ENTERPRISE
            : opsParseExcelEntity.getVersionType().equals(LITE_CONSTANT) ? LITE : MINIMAL_LIST);
        if (opsParseExcelEntity.getVersionType().equals(MINI_CONSTANT) && resultList.size() < 3) {
            opsClusterEntity.setVersionNum(resultList.get(1));
        } else {
            opsClusterEntity.setVersionNum(resultList.get(2));
        }
        opsClusterEntity.setDatabasePassword(
            encryptionUtils.encrypt(opsParseExcelEntity.getOpsImportEntity().getDatabasePassword()));
        opsClusterEntity.setDatabaseUsername(opsParseExcelEntity.getOpsImportEntity().getDatabaseUsername());
        opsClusterEntity.setPort(opsParseExcelEntity.getOpsImportEntity().getPort());
        opsClusterEntity.setEnvPath(opsParseExcelEntity.getOpsImportEntity().getEnvPath());
        opsClusterEntity.setInstallPath(resultList.get(0));
        OpsClusterNodeEntity opsClusterNodeEntity = new OpsClusterNodeEntity();
        opsClusterNodeEntity.setClusterNodeId(StrUtil.uuid());
        String localRole = judgeLocalRole(resultList, opsParseExcelEntity);
        try {
            ClusterRoleEnum clusterRole = localRole.equals(NODE_PRIMARY) ? MASTER : SLAVE;
            opsClusterNodeEntity.setClusterRole(clusterRole);
        } catch (NullPointerException e) {
            log.error("lack of clusterInfo!");
        }
        judgeNodeConnSuccess(opsParseExcelEntity.getPublicIp(), opsParseExcelEntity.getOpsImportEntity(), localRole);
        opsClusterNodeEntity.setHostId(opsParseExcelEntity.getHostAndUserId().getHostId() + "");
        opsClusterNodeEntity.setInstallUserId(opsParseExcelEntity.getHostAndUserId().getHostUserId() + "");
        opsClusterNodeEntity.setInstallPath(resultList.get(0));
        opsClusterNodeEntity.setDataPath(getDataPath(opsParseExcelEntity, resultList));
        opsClusterNodeEntity.setClusterId(opsParseExcelEntity.getOpsImportEntity().getClusterName());
        opsClusterNodeEntityList.add(opsClusterNodeEntity);
    }

    private String getDataPath(OpsParseExcelEntity opsParseExcelEntity, List<String> resultList) {
        if (resultList.size() > 2) {
            return resultList.get(1);
        }
        String dataPath = resultList.get(0) + "/data";
        if (opsParseExcelEntity.getVersionType().equals(MINI_CONSTANT)) {
            if (opsParseExcelEntity.getIpNum() > 1) {
                if (opsParseExcelEntity.getIpSequence() == 0) {
                    dataPath = dataPath + "/master";
                } else {
                    dataPath = dataPath + "/slave";
                }
            } else {
                dataPath = dataPath + "/single_node";
            }
        }
        return dataPath;
    }

    private void judgeNodeConnSuccess(String publicIp, OpsImportEntity opsImportEntity, String localRole) {
        Connection connection = null;
        try {
            connection = dbUtil.getSession(publicIp, opsImportEntity.getPort(), opsImportEntity.getDatabaseUsername(),
                    opsImportEntity.getDatabasePassword())
                .orElseThrow(() -> new OpsException("please check databaseUser is or not a origianl User"));
        } catch (OpsException | SQLException | ClassNotFoundException e) {
            if (localRole == null || localRole.equals("")) {
                opsImportEntity.setImportStatus("fail");
            } else {
                if (localRole.equals(NODE_PRIMARY)) {
                    opsImportEntity.setImportStatus("fail");
                    isInfoAndConn = false;
                }
            }
            opsImportEntity.setErrorInfo(publicIp + " Connection fail :" + e);
        } finally {
            dbUtil.closeConn(connection);
        }
    }

    private String judgeLocalRole(List<String> resultList, OpsParseExcelEntity opsParseExcelEntity) {
        String localRole = null;
        if (opsParseExcelEntity.getVersionType().equals(ENTER_CONSTANT)) {
            int selectCommandLength = 4;
            if (resultList.size() != selectCommandLength) {
                markErrorInfo(opsParseExcelEntity.getOpsImportEntity());
                opsParseExcelEntity.getOpsImportEntity()
                    .setErrorInfo("There is a command that failed to execute. Please check the logs.");
                for (int i = 0; i < resultList.size(); i++) {
                    log.error("result " + i + ":" + resultList.get(i));
                }
                return NODE_PRIMARY;
            }
            if (resultList.get(selectCommandLength - 1).equals(NODE_PRIMARY)) {
                localRole = resultList.get(selectCommandLength - 1);
                isMasterNormal = true;
            } else if (resultList.get(selectCommandLength - 1).equals(NODE_STANDBY)) {
                localRole = resultList.get(selectCommandLength - 1);
            } else {
                localRole = resultList.get(selectCommandLength - 1);
                opsParseExcelEntity.getOpsImportEntity().setErrorInfo("check node status error: " + localRole);
            }
        } else if (opsParseExcelEntity.getVersionType().equals(LITE_CONSTANT)) {
            try {
                isMasterNormal = true;
                String selectLocal = String.format(
                    "source %s;gs_ctl query -D %s|grep local_role|awk '{print $3}'|head -n 1",
                    opsParseExcelEntity.getOpsImportEntity().getEnvPath(), resultList.get(2));
                localRole = jschExecutorFacade.execCommand(opsParseExcelEntity.getSshLogin(), selectLocal);
                localRole = opsParseExcelEntity.getIpNum() == 1 ? NODE_PRIMARY : localRole;
            } catch (OpsException e) {
                markErrorInfo(opsParseExcelEntity.getOpsImportEntity());
                opsParseExcelEntity.getOpsImportEntity().setErrorInfo("lite localRole Exception:" + e);
            }
        } else {
            isMasterNormal = true;
            localRole = opsParseExcelEntity.getIpSequence() == 0 ? NODE_PRIMARY : NODE_STANDBY;
        }
        return localRole;
    }

    private String judgeVersionType(SshLogin sshLogin, OpsImportEntity opsImportEntity) {
        String omCommand = String.format(CHECK_ENTERPRISE_VERSION, opsImportEntity.getEnvPath(),
            opsImportEntity.getEnvPath());
        String liteCommand = String.format(CHECK_LITE_VERSION, opsImportEntity.getEnvPath(),
            opsImportEntity.getEnvPath());
        String miniCommand = String.format(CHECK_MINILIST_VERSION, opsImportEntity.getEnvPath(),
            opsImportEntity.getEnvPath());
        List<String> commandList = new ArrayList<>();
        commandList.add(omCommand);
        commandList.add(liteCommand);
        commandList.add(miniCommand);
        return getOpenGaussVersion(sshLogin, commandList, opsImportEntity);
    }

    private String getOpenGaussVersion(SshLogin sshLogin, List<String> commandList, OpsImportEntity opsImportEntity) {
        try {
            for (String s : commandList) {
                String result = jschExecutorFacade.execCommand(sshLogin, s);
                if (result.contains("datanodeLocalDataPath")) {
                    return ENTER_CONSTANT;
                } else if (result.contains("openGauss-lite")) {
                    return LITE_CONSTANT;
                } else if (result.contains("openGauss")) {
                    return MINI_CONSTANT;
                } else {
                    log.warn("{} could not find the corresponding result!", s);
                }
            }
        } catch (OpsException e) {
            log.error("get openGauss version command error:", e);
        }
        markErrorInfo(opsImportEntity);
        opsImportEntity.setErrorInfo("get OpenGauss Version error!");
        return OTHER_CONSTANT;
    }

    private OpsImportSshEntity getExistHostUserInfos(String hostIp, OpsImportEntity opsImportEntity) {
        OpsImportSshEntity opsImportSshInfo = opsImportSshMapper.queryHostInfo(opsImportEntity.getInstallUsername(),
            hostIp);
        if (Objects.isNull(opsImportSshInfo)) {
            markErrorInfo(opsImportEntity);
            opsImportEntity.setErrorInfo("please import hostUser information.");
        }
        return opsImportSshInfo;
    }

    private void writeZhOrEnInfo(HttpServletResponse response, String currentLocale, List<OpsImportEntity> usersList) {
        OutputStream out = null;
        try {
            out = response.getOutputStream();
            if (SysLanguage.ZH.getInfo().equals(currentLocale)) {
                EasyExcel.write(out, OpsImportEntity.class)
                    .head(createChineseHeaders())
                    .sheet("用户信息")
                    .doWrite(usersList);
            } else {
                EasyExcel.write(out, OpsImportEntity.class)
                    .head(createEnglishHeaders())
                    .sheet("userInfo")
                    .doWrite(usersList);
            }
        } catch (IOException e) {
            throw new OpsException("download fail!");
        } finally {
            try {
                out.flush();
            } catch (IOException e) {
                log.error("flush fail");
            }
            try {
                out.close();
            } catch (IOException e) {
                log.error("close fail");
            }
        }
    }

    private void closeSession(Session session) {
        if (Objects.nonNull(session) && session.isConnected()) {
            session.disconnect();
        }
    }

    private List<List<String>> createEnglishHeaders() {
        return new ArrayList<>(
            Arrays.asList(List.of("clusterName"), List.of("privateIp"), List.of("publicIp"), List.of("installUsername"),
                List.of("databaseUsername"), List.of("databasePassword"), List.of("port"), List.of("envPath"),
                List.of("importStatus"), List.of("errorInfo")));
    }

    private List<List<String>> createChineseHeaders() {
        return new ArrayList<>(
            Arrays.asList(List.of("集群名称"), List.of("内网IP"), List.of("外网IP"), List.of("安装用户"),
                List.of("数据库用户名"), List.of("数据库密码"), List.of("端口号"), List.of("环境变量路径"),
                List.of("执行状态"), List.of("报错信息")));
    }
}