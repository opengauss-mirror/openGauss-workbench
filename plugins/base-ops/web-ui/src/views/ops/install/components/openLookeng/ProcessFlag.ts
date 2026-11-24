/*
 * Copyright (c) 2026 Huawei Technologies Co., Ltd.
 * openGauss-workbench is licensed under Mulan PSL v2.
 * You can use this software according to the terms and conditions of the Mulan PSL v2.
 * You may obtain a copy of Mulan PSL v2 at:
 *          http://license.coscl.org.cn/MulanPSL2
 * THIS SOFTWARE IS PROVIDED ON AN "AS IS" BASIS, WITHOUT WARRANTIES OF ANY KIND,
 * EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO NON-INFRINGEMENT,
 * MERCHANTABILITY OR FIT FOR A PARTICULAR PURPOSE.
 * See the Mulan PSL v2 for more details.
 */

export enum ProcessFlag {
  END_UPLOAD = 'Uploading Packages Successfully Completed',
  END_INSTALL_SHARDING_PROXY = 'Install ShardingProxy On .+ Complete',
  END_INSTALL_ZOOKEEPER = 'Install Zookeeper On .+ Complete',
  END_INSTALL_OLK = 'Install OpenLooKeng On .+ Complete',
  END_INSTALL = 'Install All Packages Complete',
  END_SERVICE = 'Start All Service Complete',
  START_DESTROY = "Start Destroy OpenLooKeng Service...",
  END_DESTROY_IN_ERROR = "Destroy OpenLooKeng Service Process Abort With Error:",
  END_DESTROY = "OpenLooKeng Deploy Process Successfully Completed",
  START_SERVICE = "Start OpenLooKeng Service...",
  END_START_SERVICE_IN_ERROR = "Start OpenLooKeng Service Process Abort With Error:",
  END_START_SERVICE = "OpenLooKeng Service Successfully Started",
  START_STOP_SERVICE = "Stopping OpenLooKeng Service...",
  END_STOP_SERVICE_IN_ERROR = "Stop OpenLooKeng Service Process Abort With Error:",
  END_STOP_SERVICE = "OpenLooKeng Service Successfully Stopped"
}
