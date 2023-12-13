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
