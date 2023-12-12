import { Ref } from 'vue';
import { findNodeById, findNodesByType } from '@/utils/findNode';
import type { Tree } from '../Sidebar/types';

type CollectType =
  | 'databaseCollect'
  | 'userRoleCollect'
  | 'tableCollect'
  | 'terminalCollect'
  | 'viewCollect'
  | 'synonymCollect'
  | 'sequenceCollect';

export const useSidebarFetchData = (connectionList: Ref<Array<Tree>>) => {
  const findNode = ({
    rootId,
    databaseId,
    schemaId,
    roleId,
  }: {
    rootId: string;
    databaseId?: string;
    schemaId?: string;
    roleId?: string;
  }) => {
    for (let i = 0; i < connectionList.value.length; i++) {
      const cListItem = connectionList.value[i];
      if (cListItem.id == rootId) {
        if (!databaseId && !roleId) return cListItem;
        if (databaseId) {
          const databaseCollectNode = cListItem.children.find(
            (item) => item.type === 'databaseCollect',
          );
          for (let j = 0; j < databaseCollectNode.children.length; j++) {
            const dbListItem = databaseCollectNode.children[j];
            if (dbListItem.id == databaseId) {
              if (!schemaId) return dbListItem;
              for (let k = 0; k < dbListItem.children.length; k++) {
                const schemaItem = dbListItem.children[k];
                if (schemaItem.id == schemaId) return schemaItem;
              }
            }
          }
        }
        if (roleId) {
          const roleCollectNode = cListItem.children.find(
            (item) => item.type === 'userRoleCollect',
          );
          for (let j = 0; j < roleCollectNode.children.length; j++) {
            const roleItem = roleCollectNode.children[j];
            if (roleItem.id == roleId) return roleItem;
          }
        }
      }
    }
  };

  const findNode2 = ({
    rootId,
    databaseId,
    schemaId,
    roleId,
  }: {
    rootId: string;
    databaseId?: string;
    schemaId?: string;
    roleId?: string;
  }) => {
    const connectItem = findNodeById(connectionList.value, rootId);
    if (rootId && databaseId && schemaId) {
      const databaseItem = findNodeById(connectItem, databaseId);
      return findNodeById(databaseItem, schemaId);
    } else if (rootId && (databaseId || roleId)) {
      return findNodeById(connectItem, databaseId || roleId);
    } else if (rootId) {
      return connectItem;
    } else {
      return null;
    }
  };

  const findCollectByType = (rootId: string, targetType: CollectType) => {
    const connection = connectionList.value.find((item) => item.id === rootId);
    return findNodesByType(connection, targetType)[0];
  };

  return {
    findNode,
    findNode2,
    findCollectByType,
  };
};
