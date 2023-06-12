import { ref } from 'vue';
import { useAppStore } from '@/store/modules/app';
import { useUserStore } from '@/store/modules/user';
import { loadingInstance } from '@/utils';
import { connectListPersist } from '@/config';
import { getDatabaseList } from '@/api/database';
import { getSchemaList, getSchemaObjectList } from '@/api/metaData';
import type { FetchNode } from '../Sidebar/types';
import { useI18n } from 'vue-i18n';

export const useSidebarFetchData = (connectionList, findNode) => {
  const AppStore = useAppStore();
  const UserStore = useUserStore();
  const { t } = useI18n();
  const loading = ref(null);

  const getAllConnectList = (connectObj?: { id: string | number }) => {
    let list = JSON.parse(connectListPersist.storage.getItem(connectListPersist.key) || '[]');
    if (list.length && connectObj) {
      const repeatList: boolean[] = list.map((item) => item.id === connectObj.id);
      repeatList.includes(true)
        ? (list = list.map((item) => (item.id === connectObj.id ? connectObj : item)))
        : list.unshift(connectObj);
    } else if (!list.length && connectObj) {
      list = [connectObj];
    }
    return list;
  };
  const updateConnectListPersist = () => {
    connectListPersist.storage.setItem(
      connectListPersist.key,
      JSON.stringify(connectionList.value),
    );
  };
  const fetchDBList = async (connectInfo, uuid) => {
    loading.value = loadingInstance();
    try {
      const data = (await getDatabaseList(uuid)) as unknown as string[];
      const cloneConnectInfo = JSON.parse(JSON.stringify(connectInfo));
      const { id, name, ip, port } = connectInfo;
      const root = {
        id,
        parentId: null,
        label: `${name} (${ip}:${port})`,
        name,
        type: 'root',
        connectInfo: cloneConnectInfo,
        isLeaf: false,
        children: data.map((dbName) => {
          const hasConnectDb = AppStore.connectListMap
            .find((item) => item.id === connectInfo.id)
            ?.connectedDatabase.find((item) => item.name == dbName);
          const isConnect = !!hasConnectDb;
          return {
            id: `${id}_${dbName}`,
            rootId: id,
            parentId: id,
            uuid: hasConnectDb?.uuid || '',
            label: dbName,
            name: dbName,
            connectInfo: cloneConnectInfo,
            type: 'database',
            isLeaf: false,
            children: [],
            isConnect,
            connectTime: hasConnectDb?.connectTime || null,
          };
        }),
      };
      connectionList.value = getAllConnectList(root);
      AppStore.updateCurrentNode(root);
      updateConnectListPersist();
    } finally {
      loading.value.close();
    }
    return true;
  };
  const fetchSchemaList = async (rootId, parentId, databaseId, databaseName, uuid, connectInfo) => {
    const data = (await getSchemaList({
      uuid,
      connectionName: connectInfo.name,
      webUser: UserStore.userId,
    })) as unknown as FetchNode[];
    const schemaList = [];
    data.forEach((item) => {
      const obj = {
        id: `${parentId}_${item.oid}`,
        oid: item.oid,
        rootId,
        parentId,
        label: item.name,
        name: item.name,
        connectInfo,
        databaseName,
        databaseId,
        uuid,
        type: item.name === 'public' ? 'public' : 'person',
        isLeaf: false,
        children: [],
      };
      item.name === 'public' ? schemaList.unshift(obj) : schemaList.push(obj);
    });
    const dbNode = findNode({ rootId: connectInfo.id, databaseId });
    dbNode.children = schemaList;
    updateConnectListPersist();
    return schemaList;
  };
  const fetchSchemaContentList = async (
    rootId,
    parentId,
    uuid,
    databaseId,
    databaseName,
    schemaId,
    schemaName,
    connectInfo,
  ) => {
    const data = (await getSchemaObjectList({
      connectionName: connectInfo.name,
      webUser: UserStore.userId,
      schema: schemaName,
      uuid,
    })) as unknown as any[];
    let arr = [];
    if (data.length) {
      arr = handleSchemaList(
        data[0],
        rootId,
        parentId,
        uuid,
        databaseId,
        databaseName,
        schemaId,
        schemaName,
        connectInfo,
      );
    }
    return arr;
  };
  // User Mode - public/no public
  const handleSchemaList = (
    obj,
    rootId,
    parentId,
    uuid,
    databaseId,
    databaseName,
    schemaId,
    schemaName,
    connectInfo,
  ) => {
    const array = [];
    const order = ['table', 'fun_pro', 'sequence', 'view', 'synonym'];
    let keys = Object.keys(obj);
    keys = keys.sort((a, b) => {
      return order.indexOf(a) - order.indexOf(b);
    });
    keys.forEach((key) => {
      if (!order.includes(key)) return;
      const { label, type, childType } = sortType(key);
      const keyId = `${parentId}_${key}`;
      array.push({
        id: keyId,
        rootId,
        parentId,
        uuid,
        label: `${label} (${obj[key].length})`,
        name: label,
        type,
        key,
        connectInfo,
        databaseId,
        databaseName,
        schemaId,
        schemaName,
        children: handleTypeList(
          obj[key],
          childType,
          rootId,
          keyId,
          uuid,
          databaseId,
          databaseName,
          schemaId,
          schemaName,
          connectInfo,
        ),
        isLeaf: false,
      });
    });
    return array;
  };
  // User Mode-children
  const sortType = (key) => {
    const obj = { label: '', type: '', childType: '' };
    switch (key) {
      case 'table':
        obj.label = t('database.regular_table');
        obj.type = 'tableCollect';
        obj.childType = 'table';
        break;
      case 'fun_pro':
        obj.label = t('database.function_process');
        obj.type = 'terminalCollect';
        obj.childType = 'terminal';
        break;
      case 'view':
        obj.label = t('database.view');
        obj.type = 'viewCollect';
        obj.childType = 'view';
        break;
      case 'synonym':
        obj.label = t('database.synonym');
        obj.type = 'synonymCollect';
        obj.childType = 'synonym';
        break;
      case 'sequence':
        obj.label = t('database.sequence');
        obj.type = 'sequenceCollect';
        obj.childType = 'sequence';
        break;
    }
    return obj;
  };
  // User Mode-children data - [table/func/view]...
  const handleTypeList = (
    array,
    childType,
    rootId,
    parentId,
    uuid,
    databaseId,
    databaseName,
    schemaId,
    schemaName,
    connectInfo,
  ) => {
    return array.map((item: FetchNode) => ({
      id: `${parentId}_${item.oid}`,
      oid: item.oid,
      rootId,
      parentId,
      uuid,
      label: item.name,
      name: item.name,
      type: childType,
      databaseId,
      databaseName,
      schemaId,
      schemaName,
      connectInfo,
      isLeaf: true,
    }));
  };
  return {
    getAllConnectList,
    updateConnectListPersist,
    fetchDBList,
    fetchSchemaList,
    fetchSchemaContentList,
    handleSchemaList,
    sortType,
  };
};
