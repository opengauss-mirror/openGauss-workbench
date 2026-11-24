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

import { findNodesByType } from '@/utils/findNode';
import EventBus, { EventTypeName } from '@/utils/event-bus';

export const updateConnectInfo = (connectionList, newConnectInfo, willRefreshSidebar?: boolean) => {
  const { connectionid: uuid, ...connectInfo } = newConnectInfo;
  const targetRootNode = connectionList.find((item) => item.id === connectInfo.id);
  if (targetRootNode) {
    // Update the connectInfo in 'root'
    targetRootNode.connectInfo = connectInfo;

    // If you want to refresh sidebar tree.The 'root' has been updated above, and then update the specific 'database'.
    if (willRefreshSidebar) {
      const targetDatabase = findNodesByType(targetRootNode, 'database').find(
        (item) => item.uuid === uuid,
      );
      if (targetDatabase) {
        EventBus.notify(EventTypeName.REFRESH_ASIDER, 'database', {
          rootId: newConnectInfo.id,
          databaseId: targetDatabase.id,
        });
      }
    }
  }
};
