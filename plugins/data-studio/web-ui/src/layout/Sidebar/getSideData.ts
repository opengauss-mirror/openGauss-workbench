import { useAppStore } from '@/store/modules/app';
import { useUserStore } from '@/store/modules/user';
import { i18n } from '@/i18n/index';
import { getDatabaseList } from '@/api/database';
import { getSchemaList, getSchemaObjectList } from '@/api/metaData';
import { getUserRoleList } from '@/api/userRole';
import { getTablespaceListApi } from '@/api/tablespace';

import { NodeEnum, FetchNode, ConnectInfo } from './types';

const t = i18n.global.t;

const getRootChildCollectLabel = (
  type: 'databaseCollect' | 'userRoleCollect' | 'tablespaceCollect' | 'job',
  count = null,
  showCount = true,
) => {
  const countText = showCount ? ` (${count || 0})` : '';
  return {
    databaseCollect: `${t('database.database')}${countText}`,
    userRoleCollect: `${t('userRole.name')}${countText}`,
    tablespaceCollect: `${t('tablespace.name')}${countText}`,
    job: `${t('job.name')}`,
  }[type];
};

const generateRoot = async (connectInfo, uuid: string) => {
  const cloneConnectInfo = JSON.parse(JSON.stringify(connectInfo));
  const { id, name, ip, port } = connectInfo;
  const rootChildrenType = ['databaseCollect', 'userRoleCollect', 'tablespaceCollect', 'job'];
  const dbList = await generateDBList(id, `${id}_databaseCollect`, uuid, cloneConnectInfo);
  const userRoleList = await generateUserRoleList(
    id,
    `${id}_userRoleCollect`,
    uuid,
    cloneConnectInfo,
  );
  const tablespaceList = await generateTablespaceList(
    id,
    `${id}_tablespaceCollect`,
    uuid,
    cloneConnectInfo,
  );
  return {
    id,
    parentId: null,
    label: `${name} (${ip}:${port})`,
    name,
    type: NodeEnum.ROOT,
    connectInfo: cloneConnectInfo,
    isLeaf: false,
    children: rootChildrenType.map((childType) => {
      const thisId = `${id}_${childType}`;
      let childLabel = '';
      let childList = [];
      let isLeaf = false;
      if (childType === 'databaseCollect') {
        childList = dbList;
        childLabel = getRootChildCollectLabel('databaseCollect', childList.length);
      }
      if (childType === 'userRoleCollect') {
        childList = userRoleList;
        childLabel = getRootChildCollectLabel('userRoleCollect', childList.length);
      }
      if (childType === 'tablespaceCollect') {
        childList = tablespaceList;
        childLabel = getRootChildCollectLabel('tablespaceCollect', childList.length);
      }
      if (childType === 'job') {
        childLabel = null;
        childLabel = getRootChildCollectLabel('job');
        isLeaf = true;
      }
      return {
        id: thisId,
        rootId: id,
        parentId: id,
        label: childLabel,
        name: childType,
        type: childType,
        connectInfo: cloneConnectInfo,
        isLeaf,
        children: childList,
      };
    }),
  };
};

const generateUserRoleList = async (rootId, parentId, uuid, connectInfo) => {
  const res = await getUserRoleList({ uuid });
  const data: { name: string; oid: string; type: 'user' | 'role' }[] = [].concat(
    res.user.map((item: { name: string; oid: string }) => ({ ...item, type: 'user' })),
    res.role.map((item: { name: string; oid: string }) => ({ ...item, type: 'role' })),
  );
  return data.map((item) => ({
    id: `${parentId}_${item.oid}`,
    rootId,
    parentId,
    label: item.name,
    name: item.name,
    connectInfo,
    type: item.type,
    isLeaf: true,
  }));
};

const generateTablespaceList = async (
  rootId: string,
  parentId: string,
  uuid: string,
  connectInfo,
) => {
  const data = (await getTablespaceListApi({ uuid })) as unknown as { name: string; oid: string }[];
  return data.map((item) => ({
    id: `${parentId}_${item.oid}`,
    oid: item.oid,
    rootId,
    parentId,
    label: item.name,
    name: item.name,
    connectInfo,
    type: 'tablespace',
    isLeaf: true,
  }));
};

const generateDBList = async (
  rootId: string,
  parentId: string,
  uuid: string,
  connectInfo: ConnectInfo,
) => {
  const AppStore = useAppStore();
  const data = (await getDatabaseList(uuid)) as unknown as string[];
  return data.map((dbName) => {
    const hasConnectDb = AppStore.connectListMap
      .find((item) => item.id === connectInfo.id)
      ?.connectedDatabase.find((item) => item.name == dbName);
    const isConnect = !!hasConnectDb;
    return {
      id: `${parentId}_${dbName}`,
      rootId,
      parentId,
      uuid: hasConnectDb?.uuid || '',
      label: dbName,
      name: dbName,
      connectInfo,
      type: NodeEnum.DATABASE,
      isLeaf: false,
      children: [],
      isConnect,
      connectTime: (hasConnectDb?.connectTime as number) || null,
    };
  });
};

const generateSchemaList = async (
  rootId,
  parentId,
  databaseId,
  databaseName,
  uuid,
  connectInfo,
) => {
  const UserStore = useUserStore();
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
  return schemaList;
};

const generateSchemaContentList = async (
  rootId,
  parentId,
  uuid,
  databaseId,
  databaseName,
  schemaId,
  schemaName,
  connectInfo,
) => {
  const UserStore = useUserStore();
  const data = (await getSchemaObjectList({
    connectionName: connectInfo.name,
    webUser: UserStore.userId,
    schema: schemaName,
    uuid,
  })) as unknown as FetchNode[];
  const array = [];
  if (data.length) {
    const obj = data[0];
    const order = ['table', 'foreignTable', 'trigger', 'fun_pro', 'sequence', 'view', 'synonym'];
    let keys = Object.keys(obj);
    keys = keys.sort((a, b) => {
      return order.indexOf(a) - order.indexOf(b);
    });
    // Running results 'keys' such as: ['schema_name', 'table', 'foreignTable', 'trigger', 'fun_pro', 'sequence', 'view', 'synonym']
    keys.forEach((key) => {
      if (!order.includes(key)) return;
      const { label, type, childType } = getLocalType(key);
      const keyId = `${parentId}_${key}`;
      array.push({
        id: keyId,
        rootId,
        parentId,
        uuid,
        label: `${label} (${obj[key]?.length || 0})`,
        name: label,
        type,
        key,
        connectInfo,
        databaseId,
        databaseName,
        schemaId,
        schemaName,
        children: generateFileList(
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
  }
  return array;
};

const getLocalType = (key) => {
  // Transform the type of the api to a local type
  const obj = { label: '', type: '', childType: '' };
  switch (key) {
    case 'table':
      obj.label = t('database.regular_table');
      obj.type = 'tableCollect';
      obj.childType = 'table';
      break;
    case 'foreignTable':
      obj.label = t('database.foreign_table');
      obj.type = 'foreignTableCollect';
      obj.childType = 'foreignTable';
      break;
    case 'trigger':
      obj.label = t('database.trigger');
      obj.type = 'triggerCollect';
      obj.childType = 'trigger';
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

const generateFileList = (
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
  if (!Array.isArray(array)) return [];
  return array.map((item: FetchNode) => {
    return {
      id: `${parentId}_${item.oid}`,
      oid: item.oid,
      parttype: item.parttype,
      tableName: item.tableName,
      isTableTrigger: item.isTableTrigger,
      enabled: !!item.enabled,
      rootId,
      parentId,
      uuid,
      label: item.name,
      name: item.name,
      type: item.isPackage ? 'package' : childType,
      databaseId,
      databaseName,
      schemaId,
      schemaName,
      connectInfo,
      isInPackage: false,
      children: item.isPackage
        ? generatePackageContent(
            item.children,
            rootId,
            parentId,
            uuid,
            databaseId,
            databaseName,
            schemaId,
            schemaName,
            item.oid,
            item.name,
            connectInfo,
          )
        : [],
      isLeaf: !item.isPackage,
    };
  });
};

const generatePackageContent = (
  array,
  rootId,
  parentId,
  uuid,
  databaseId,
  databaseName,
  schemaId,
  schemaName,
  packageId,
  packageName,
  connectInfo,
) => {
  return array.map((pkChild) => {
    return {
      id: `${parentId}_${pkChild.oid}`,
      oid: pkChild.oid,
      rootId,
      parentId,
      uuid,
      label: pkChild.name,
      name: pkChild.name,
      type: 'terminal',
      databaseId,
      databaseName,
      schemaId,
      schemaName,
      packageId,
      packageName,
      connectInfo,
      isInPackage: true,
      isLeaf: true,
    };
  });
};

export {
  generateRoot,
  generateUserRoleList,
  generateTablespaceList,
  generateDBList,
  generateSchemaList,
  generateSchemaContentList,
  getLocalType,
  getRootChildCollectLabel,
};
