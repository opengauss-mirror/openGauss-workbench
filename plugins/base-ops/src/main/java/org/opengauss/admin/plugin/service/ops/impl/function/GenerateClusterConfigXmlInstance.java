/*
 * Copyright (c) 2024 Huawei Technologies Co.,Ltd.
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
 * GenerateClusterConfigXmlInstance.java
 *
 * IDENTIFICATION
 * plugins/base-ops/src/main/java/org/opengauss/admin/plugin/service/ops/impl/function/GenerateClusterConfigXmlInstance.java
 *
 * -------------------------------------------------------------------------
 */

package org.opengauss.admin.plugin.service.ops.impl.function;

import lombok.extern.slf4j.Slf4j;
import org.opengauss.admin.common.exception.ops.OpsException;
import org.opengauss.admin.plugin.domain.model.ops.EnterpriseInstallConfig;
import org.opengauss.admin.plugin.domain.model.ops.InstallContext;
import org.opengauss.admin.plugin.domain.model.ops.SharingStorageInstallConfig;
import org.opengauss.admin.plugin.domain.model.ops.node.EnterpriseInstallNodeConfig;
import org.opengauss.admin.plugin.enums.ops.ClusterRoleEnum;
import org.opengauss.admin.plugin.enums.ops.ConnectTypeEnum;
import org.opengauss.admin.plugin.enums.ops.DatabaseKernelArch;
import org.opengauss.admin.plugin.mapper.ops.OpsBaseHostMapper;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import jakarta.annotation.Resource;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static java.lang.Math.min;

/**
 * GenerateClusterConfigXml
 *
 * @author wangchao
 * @date 2024/6/22 9:41
 **/
@Slf4j
@Service
public class GenerateClusterConfigXmlInstance {
    private static final int DEFAULT_SSH_PORT = 22;

    @Resource
    private OpsBaseHostMapper opsBaseHostMapper;

    private final GenerateClusterConfigXml clusterConfigXml = (installContext) -> {
        String xmlString = "";
        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        try {
            // Disallow the DTDs (doctypes) entirely.
            documentBuilderFactory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);

            // Or do the following:
            documentBuilderFactory.setFeature("http://xml.org/sax/features/external-general-entities", false);
            documentBuilderFactory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
            documentBuilderFactory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
            documentBuilderFactory.setXIncludeAware(false);
            documentBuilderFactory.setExpandEntityReferences(false);
            DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
            Document document = documentBuilder.newDocument();
            document.setXmlVersion("1.0");
            document.setXmlStandalone(true);
            Element rootElement = document.createElement("ROOT");

            Element cluster = document.createElement("CLUSTER");
            appendClusterParam(document, cluster, installContext);

            Element deviceList = document.createElement("DEVICELIST");
            appendDeviceList(document, deviceList, installContext);

            rootElement.appendChild(cluster);
            rootElement.appendChild(deviceList);

            document.appendChild(rootElement);

            TransformerFactory transFactory = TransformerFactory.newInstance();
            transFactory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
            transFactory.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
            transFactory.setAttribute(XMLConstants.ACCESS_EXTERNAL_STYLESHEET, "");
            Transformer transformer = transFactory.newTransformer();
            transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            DOMSource domSource = new DOMSource(document);
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            transformer.transform(domSource, new StreamResult(byteArrayOutputStream));
            xmlString = byteArrayOutputStream.toString(StandardCharsets.UTF_8);
        } catch (Exception e) {
            log.error("generate xml exception：", e);
            throw new OpsException("generate xml exception");
        }
        return xmlString;
    };

    /**
     * generate openGauss enterprise cluster config xml
     *
     * @param installContext install context
     * @return xml
     */
    public String generate(InstallContext installContext) {
        return clusterConfigXml.generate(installContext);
    }

    private void appendDeviceList(Document document, Element deviceList, InstallContext installContext) {
        List<EnterpriseInstallNodeConfig> nodeConfigList = installContext
                .getEnterpriseInstallConfig().getNodeConfigList();
        for (EnterpriseInstallNodeConfig enterpriseInstallNodeConfig : nodeConfigList) {
            Element device = document.createElement("DEVICE");
            device.setAttribute("sn", enterpriseInstallNodeConfig.getHostname());
            deviceList.appendChild(device);

            appendDeviceParam(document, device, enterpriseInstallNodeConfig,
                    nodeConfigList, installContext);
        }
    }

    private void appendSshPortParam(Document document, Element device,
                                    EnterpriseInstallNodeConfig enterpriseInstallNodeConfig,
                                    InstallContext installContext) {
        int port = opsBaseHostMapper.queryPortByHostId(enterpriseInstallNodeConfig.getHostId());
        String opengaussVersion = installContext.getOpenGaussVersionNum();
        if (opengaussVersion != null && !opengaussVersion.isEmpty()) {
            String versionNum = opengaussVersion.split("\\.")[0];
            if (Integer.parseInt(versionNum) >= 7) {
                Element sshPort = document.createElement("PARAM");
                sshPort.setAttribute("name", "sshPort");
                sshPort.setAttribute("value", String.valueOf(port));
                device.appendChild(sshPort);
            } else {
                if (port != DEFAULT_SSH_PORT) {
                    throw new OpsException("invalid ssh port, expected is: 22, actual is: " + port);
                }
            }
        }
    }

    private void appendDeviceParam(Document document, Element device,
                                   EnterpriseInstallNodeConfig enterpriseInstallNodeConfig,
                                   List<EnterpriseInstallNodeConfig> nodeConfigList,
                                   InstallContext installContext) {
        Element name = document.createElement("PARAM");
        name.setAttribute("name", "name");
        name.setAttribute("value", enterpriseInstallNodeConfig.getHostname());
        device.appendChild(name);

        Element azName = document.createElement("PARAM");
        azName.setAttribute("name", "azName");
        azName.setAttribute("value", enterpriseInstallNodeConfig.getAzName());
        device.appendChild(azName);

        Element azPriority = document.createElement("PARAM");
        azPriority.setAttribute("name", "azPriority");
        azPriority.setAttribute("value", enterpriseInstallNodeConfig.getAzPriority());
        device.appendChild(azPriority);

        Element backIp1 = document.createElement("PARAM");
        backIp1.setAttribute("name", "backIp1");
        backIp1.setAttribute("value", enterpriseInstallNodeConfig.getPrivateIp());
        device.appendChild(backIp1);

        appendSshPortParam(document, device, enterpriseInstallNodeConfig, installContext);
        EnterpriseInstallConfig enterpriseInstallConfig = installContext.getEnterpriseInstallConfig();
        if (enterpriseInstallConfig.getIsInstallCM()) {
            if (enterpriseInstallNodeConfig.getIsCMMaster()) {
                Element cmsNum = document.createElement("PARAM");
                cmsNum.setAttribute("name", "cmsNum");
                cmsNum.setAttribute("value", "1");
                device.appendChild(cmsNum);

                Element cmServerPortBase = document.createElement("PARAM");
                cmServerPortBase.setAttribute("name", "cmServerPortBase");
                cmServerPortBase.setAttribute("value", String.valueOf(enterpriseInstallNodeConfig.getCmPort()));
                device.appendChild(cmServerPortBase);

                Element cmServerListenIp1 = document.createElement("PARAM");
                cmServerListenIp1.setAttribute("name", "cmServerListenIp1");
                Object[] ips = nodeConfigList.stream().map(EnterpriseInstallNodeConfig::getPrivateIp).toArray();
                cmServerListenIp1.setAttribute("value", StringUtils.arrayToCommaDelimitedString(ips));
                device.appendChild(cmServerListenIp1);

                Element cmServerHaIp1 = document.createElement("PARAM");
                cmServerHaIp1.setAttribute("name", "cmServerHaIp1");
                cmServerHaIp1.setAttribute("value", StringUtils.arrayToCommaDelimitedString(ips));
                device.appendChild(cmServerHaIp1);

                Element cmServerlevel = document.createElement("PARAM");
                cmServerlevel.setAttribute("name", "cmServerlevel");
                cmServerlevel.setAttribute("value", "1");
                device.appendChild(cmServerlevel);

                Element cmServerRelation = document.createElement("PARAM");
                cmServerRelation.setAttribute("name", "cmServerRelation");
                Object[] hostnames = nodeConfigList.stream().map(EnterpriseInstallNodeConfig::getHostname).toArray();
                cmServerRelation.setAttribute("value", StringUtils.arrayToCommaDelimitedString(hostnames));
                device.appendChild(cmServerRelation);

                Element cmDir = document.createElement("PARAM");
                cmDir.setAttribute("name", "cmDir");
                cmDir.setAttribute("value", enterpriseInstallNodeConfig.getCmDataPath());
                device.appendChild(cmDir);
            } else {
                Element cmDir = document.createElement("PARAM");
                cmDir.setAttribute("name", "cmDir");
                cmDir.setAttribute("value", enterpriseInstallNodeConfig.getCmDataPath());
                device.appendChild(cmDir);

                Element cmServerPortStandby = document.createElement("PARAM");
                cmServerPortStandby.setAttribute("name", "cmServerPortStandby");
                cmServerPortStandby.setAttribute("value", String.valueOf(enterpriseInstallNodeConfig.getCmPort()));
                device.appendChild(cmServerPortStandby);
            }


        }

        if (enterpriseInstallNodeConfig.getClusterRole() == ClusterRoleEnum.MASTER) {
            Element dataNum = document.createElement("PARAM");
            dataNum.setAttribute("name", "dataNum");
            dataNum.setAttribute("value", "1");
            device.appendChild(dataNum);

            Element dataPortBase = document.createElement("PARAM");
            dataPortBase.setAttribute("name", "dataPortBase");
            dataPortBase.setAttribute("value", String.valueOf(enterpriseInstallConfig.getPort()));
            device.appendChild(dataPortBase);

            Element dataNode1 = document.createElement("PARAM");
            dataNode1.setAttribute("name", "dataNode1");
            List<EnterpriseInstallNodeConfig> nodeConfigs = nodeConfigList.stream().collect(Collectors.toList());
            EnterpriseInstallNodeConfig master = nodeConfigs.stream().filter(node -> node.getClusterRole() == ClusterRoleEnum.MASTER).findFirst().orElseThrow(() -> new OpsException("master node information not found"));
            StringBuilder dataNodeValue = new StringBuilder(master.getDataPath());
            for (EnterpriseInstallNodeConfig nodeConfig : nodeConfigs) {
                if (!master.equals(nodeConfig)) {
                    dataNodeValue.append(",")
                            .append(nodeConfig.getHostname())
                            .append(",")
                            .append(nodeConfig.getDataPath());
                }

            }
            dataNode1.setAttribute("value", dataNodeValue.toString());
            device.appendChild(dataNode1);

            Element dataNode1_syncNum = document.createElement("PARAM");
            dataNode1_syncNum.setAttribute("name", "dataNode1_syncNum");
            dataNode1_syncNum.setAttribute("value", "0");
            device.appendChild(dataNode1_syncNum);
        }
    }


    private void appendSharingStorageClusterParam(Document document, Element cluster, InstallContext installContext) {
        SharingStorageInstallConfig sharingStorageInstallConfig = installContext.getEnterpriseInstallConfig().getSharingStorageInstallConfig();
        Element isEnableDss = document.createElement("PARAM");
        isEnableDss.setAttribute("name", "enable_dss");
        isEnableDss.setAttribute("value", "on");
        cluster.appendChild(isEnableDss);

        Element dssHome = document.createElement("PARAM");
        dssHome.setAttribute("name", "dss_home");
        dssHome.setAttribute("value", sharingStorageInstallConfig.getDssHome());
        cluster.appendChild(dssHome);

        Element dssVgName = document.createElement("PARAM");
        dssVgName.setAttribute("name", "ss_dss_vg_name");
        dssVgName.setAttribute("value", sharingStorageInstallConfig.getDssVgName());
        cluster.appendChild(dssVgName);

        Element dssVgInfo = document.createElement("PARAM");
        dssVgInfo.setAttribute("name", "dss_vg_info");
        List<String> xlogVgName = Arrays.asList(sharingStorageInstallConfig.getXlogVgName().split(","));
        List<String> xlogLunPath = sharingStorageInstallConfig.getXlogLunLinkPath();
        if (xlogVgName.size() != xlogLunPath.size()) {
            log.warn("xlog vg name size {} is not corresponding to xlog lun path size {}", xlogVgName.size(),
                    xlogLunPath.size());
        }

        List<String> combine = new ArrayList<>();
        for (int i = 0; i < min(xlogVgName.size(), xlogLunPath.size()); ++i) {
            combine.add(xlogVgName.get(i).trim() + ":" + xlogLunPath.get(i).trim());
        }
        String finalStr = StringUtils.collectionToCommaDelimitedString(combine);
        Object[] vgInfo =
                new String[]{sharingStorageInstallConfig.getDssVgName().trim() + ":"
                        + sharingStorageInstallConfig.getDssDataLunLinkPath().trim(), finalStr};
        dssVgInfo.setAttribute("value", StringUtils.arrayToCommaDelimitedString(vgInfo));
        cluster.appendChild(dssVgInfo);

        Element votingLun = document.createElement("PARAM");
        votingLun.setAttribute("name", "votingDiskPath");
        votingLun.setAttribute("value", sharingStorageInstallConfig.getCmVotingLunLinkPath());
        cluster.appendChild(votingLun);

        Element sharingLun = document.createElement("PARAM");
        sharingLun.setAttribute("name", "shareDiskDir");
        sharingLun.setAttribute("value", sharingStorageInstallConfig.getCmSharingLunLinkPath());
        cluster.appendChild(sharingLun);

        Element enableSsl = document.createElement("PARAM");
        enableSsl.setAttribute("name", "dss_ssl_enable");
        String ssl = sharingStorageInstallConfig.isEnableSsl() ? "on" : "off";
        enableSsl.setAttribute("value", ssl);
        cluster.appendChild(enableSsl);

        if (sharingStorageInstallConfig.getInterconnectType() == ConnectTypeEnum.RDMA) {
            Element interconnectType = document.createElement("PARAM");
            interconnectType.setAttribute("name", "ss_interconnect_type");
            interconnectType.setAttribute("value", String.valueOf(sharingStorageInstallConfig.getInterconnectType()));
            cluster.appendChild(interconnectType);

            Element rdmaConfig = document.createElement("PARAM");
            rdmaConfig.setAttribute("name", "ss_rdma_work_config");
            rdmaConfig.setAttribute("value", sharingStorageInstallConfig.getRdamConfig());
            cluster.appendChild(rdmaConfig);

            Element rdmaLogPath = document.createElement("PARAM");
            rdmaLogPath.setAttribute("name", "ss_ock_log_path");
            rdmaLogPath.setAttribute("value", sharingStorageInstallConfig.getRdamLogPath());
            cluster.appendChild(rdmaLogPath);
        }
    }

    private void appendClusterParam(Document document, Element cluster, InstallContext installContext) {
        Element clusterName = document.createElement("PARAM");
        clusterName.setAttribute("name", "clusterName");
        clusterName.setAttribute("value", installContext.getClusterId());
        cluster.appendChild(clusterName);

        Element nodeNames = document.createElement("PARAM");
        nodeNames.setAttribute("name", "nodeNames");
        List<String> hostNames = installContext.getEnterpriseInstallConfig()
                .getNodeConfigList().stream()
                .map(EnterpriseInstallNodeConfig::getHostname).collect(Collectors.toList());
        nodeNames.setAttribute("value", StringUtils.collectionToCommaDelimitedString(hostNames));
        cluster.appendChild(nodeNames);

        Element gaussdbAppPath = document.createElement("PARAM");
        gaussdbAppPath.setAttribute("name", "gaussdbAppPath");
        gaussdbAppPath.setAttribute("value", installContext.getEnterpriseInstallConfig().getInstallPath());
        cluster.appendChild(gaussdbAppPath);

        Element gaussdbLogPath = document.createElement("PARAM");
        gaussdbLogPath.setAttribute("name", "gaussdbLogPath");
        gaussdbLogPath.setAttribute("value", installContext.getEnterpriseInstallConfig().getLogPath());
        cluster.appendChild(gaussdbLogPath);

        Element tmpMppdbPath = document.createElement("PARAM");
        tmpMppdbPath.setAttribute("name", "tmpMppdbPath");
        tmpMppdbPath.setAttribute("value", installContext.getEnterpriseInstallConfig().getTmpPath());
        cluster.appendChild(tmpMppdbPath);

        Element gaussdbToolPath = document.createElement("PARAM");
        gaussdbToolPath.setAttribute("name", "gaussdbToolPath");
        gaussdbToolPath.setAttribute("value", installContext.getEnterpriseInstallConfig().getOmToolsPath());
        cluster.appendChild(gaussdbToolPath);

        Element corePath = document.createElement("PARAM");
        corePath.setAttribute("name", "corePath");
        corePath.setAttribute("value", installContext.getEnterpriseInstallConfig().getCorePath());
        cluster.appendChild(corePath);

        Element backIp1s = document.createElement("PARAM");
        backIp1s.setAttribute("name", "backIp1s");
        Object[] privateIps = installContext.getEnterpriseInstallConfig().getNodeConfigList().stream().map(EnterpriseInstallNodeConfig::getPrivateIp).toArray();
        backIp1s.setAttribute("value", StringUtils.arrayToCommaDelimitedString(privateIps));
        cluster.appendChild(backIp1s);

        if (installContext.getEnterpriseInstallConfig().getEnableDCF()) {
            Element enableDcf = document.createElement("PARAM");
            enableDcf.setAttribute("name", "enable_dcf");
            enableDcf.setAttribute("value", "on");
            cluster.appendChild(enableDcf);

            Element dcfConfig = document.createElement("PARAM");
            dcfConfig.setAttribute("name", "dcf_config");

            StringBuilder res = new StringBuilder("[");
            String template = "'{'\"stream_id\":1,\"node_id\":{0},\"ip\":\"{1}\",\"port\":{2},\"role\":\"{3}\"'}'";
            List<EnterpriseInstallNodeConfig> nodeConfigList = installContext.getEnterpriseInstallConfig().getNodeConfigList();
            for (int i = 0; i < nodeConfigList.size(); i++) {
                EnterpriseInstallNodeConfig enterpriseInstallNodeConfig = nodeConfigList.get(i);
                String format = MessageFormat.format(template, i + 1,
                        enterpriseInstallNodeConfig.getPrivateIp(),
                        String.valueOf(installContext.getEnterpriseInstallConfig().getDcfPort()),
                        enterpriseInstallNodeConfig
                                .getClusterRole() == ClusterRoleEnum.MASTER ? "LEADER" : "FOLLOWER");
                res.append(format);
                res.append(",");
            }

            res.deleteCharAt(res.length() - 1);
            res.append("]");

            dcfConfig.setAttribute("value", res.toString());
            cluster.appendChild(dcfConfig);
        }

        if (installContext.getEnterpriseInstallConfig().getDatabaseKernelArch() == DatabaseKernelArch.SHARING_STORAGE) {
            appendSharingStorageClusterParam(document, cluster, installContext);
        }
    }
}
