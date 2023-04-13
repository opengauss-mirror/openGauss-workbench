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
 * OlkProcessFlagStr.java
 *
 * IDENTIFICATION
 * base-ops/src/main/java/org/opengauss/admin/plugin/domain/model/ops/olk/OlkProcessFlagStr.java
 *
 * -------------------------------------------------------------------------
 */

package org.opengauss.admin.plugin.domain.model.ops.olk;

public class OlkProcessFlagStr {
    /**
     * Datakit will use this
     */
    public static final String START_DEPLOY_PROCESS = "Start OpenLooKeng Deploy Process, This may take 5~10 minutes...";
    public static final String END_DEPLOY_PROCESS = "OpenLooKeng Deploy Successfully Completed";
    public static final String START_UPLOAD = "Start Uploading All Packages...";
    public static final String START_UPLOAD_SHARDING_PROXY = "Start Uploading ShardingProxy Package To %s, By User %s";
    public static final String START_UPLOAD_ZOOKEEPER = "Start Uploading Zookeeper Package To %s, By User %s";
    public static final String START_UPLOAD_OLK = "Start Uploading OpenLooKeng Package To %s, By User %s";
    public static final String START_UPLOAD_DAD = "Start Uploading Distributed Deployment Component To %s, By User %s";
    public static final String START_UPLOAD_RULE_YAML = "Start Uploading rule.yaml To %s";
    public static final String END_UPLOAD_RULE_YAML = "Start Uploading rule.yaml To %s Complete";
    public static final String END_UPLOAD_SHARDING_PROXY = "Upload ShardingProxy Package To %s Complete";
    public static final String END_UPLOAD_ZOOKEEPER = "Upload Zookeeper Package To %s Complete";
    public static final String END_UPLOAD_OLK = "Upload OpenLooKeng Package To %s Complete";
    public static final String END_UPLOAD_DAD = "Upload Distributed Deployment Component Package To %s Complete";
    public static final String END_UPLOAD = "Uploading Packages Successfully Completed";
    public static final String SEND_CMD_TO_DAD_SERVICE  = "Send Command To Deployment Service";
    public static final String SEND_CMD_TO_SERVICE_COMPLETE  = "Send Command To Deployment Service Successfully Completed";

    /**
     * From Dad service, main chapter
     */
    public static final String START_DEPLOY = "Start OpenLooKeng Deploy Process...";
    public static final String END_DEPLOY_IN_ERROR = "OpenLooKeng Deploy Process Abort With Error";
    public static final String END_DEPLOY = "OpenLooKeng Deploy Process Successfully Completed";
    public static final String START_DESTROY = "Start Destroy OpenLooKeng Service...";
    public static final String END_DESTROY_IN_ERROR = "Destroy OpenLooKeng Service Process Abort With Error";
    public static final String END_DESTROY = "OpenLooKeng Deploy Process Successfully Completed";
    public static final String START_SERVICE = "Start OpenLooKeng Service...";
    public static final String END_START_SERVICE_IN_ERROR = "Start OpenLooKeng Service Process Abort With Error";
    public static final String END_START_SERVICE = "OpenLooKeng Service Successfully Started";
    public static final String START_STOP_SERVICE = "Stopping OpenLooKeng Service...";
    public static final String END_STOP_SERVICE_IN_ERROR = "Stop OpenLooKeng Service Process Abort With Error";
    public static final String END_STOP_SERVICE = "OpenLooKeng Service Successfully Stopped";

    /**
     * From Dad service, sub chapter
     */
    public static final String START_INSTALL = "Start Install All Packages...";
    public static final String START_INSTALL_SHARDING_PROXY = "Start Install ShardingProxy On %s";
    public static final String START_INSTALL_ZOOKEEPER = "Start Install Zookeeper On %s";
    public static final String START_INSTALL_OLK = "Start Install OpenLooKeng On %s";
    public static final String END_INSTALL_SHARDING_PROXY = "Install ShardingProxy On %s Complete";
    public static final String END_INSTALL_ZOOKEEPER = "Install Zookeeper On %s Complete";
    public static final String END_INSTALL_OLK = "Install OpenLooKeng On %s Complete";
    public static final String END_INSTALL = "Install All Packages Complete";
    public static final String START_SHARDING_PROXY_SERVICE = "Start Sharding Proxy Service On %s";
    public static final String START_ZOOKEEPER_SERVICE = "Start Zookeeper Service On %s";
    public static final String START_OLK_SERVICE = "Start OpenLooKeng Service On %s";
    public static final String END_SHARDING_PROXY_SERVICE = "Sharding Proxy Service On %s Started";
    public static final String END_ZOOKEEPER_SERVICE = "Zookeeper Service On %s Started";
    public static final String END_OLK_SERVICE = "OpenLooKeng Service On %s Started";
    public static final String END_SERVICE = "Start All Service Complete";
    public static final String DAD_PKG_NAME = "Distributed-Automatic-Deployment";
}
