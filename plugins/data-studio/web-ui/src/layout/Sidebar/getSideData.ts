import { useAppStore } from '@/store/modules/app';
import { useUserStore } from '@/store/modules/user';
import { i18n } from '@/i18n/index';
import { getDatabaseList } from '@/api/database';
import { getSchemaList, getSchemaObjectList } from '@/api/metaData';
import { getUserRoleList } from '@/api/userRole';

import type { FetchNode } from './types';

const t = i18n.global.t;

const getDbOrRoleCollectLabel = (
  type: 'databaseCollect' | 'userRoleCollect',
  count = null,
  showCount = true,
) => {
  const countText = showCount ? ` (${count || 0})` : '';
  return {
    databaseCollect: `${t('database.database')}${countText}`,
    userRoleCollect: `${t('userRole.name')}${countText}`,
  }[type];
};

const generateRoot = async (connectInfo, uuid) => {
  const cloneConnectInfo = JSON.parse(JSON.stringify(connectInfo));
  const { id, name, ip, port } = connectInfo;
  const rootChildrenType = ['databaseCollect', 'userRoleCollect'];
  const dbList = await generateDBList(id, `${id}_databaseCollect`, uuid, cloneConnectInfo);
  const userRoleList = await generateUserRoleList(
    id,
    `${id}_userRoleCollect`,
    uuid,
    cloneConnectInfo,
  );
  return {
    id,
    parentId: null,
    label: `${name} (${ip}:${port})`,
    name,
    type: 'root',
    connectInfo: cloneConnectInfo,
    isLeaf: false,
    children: rootChildrenType.map((childType) => {
      const thisId = `${id}_${childType}`;
      let dbOrRoleLabel = '';
      let dbOrRoleList = [];
      if (childType === 'databaseCollect') {
        dbOrRoleList = dbList;
        dbOrRoleLabel = getDbOrRoleCollectLabel('databaseCollect', dbOrRoleList.length);
      }
      if (childType === 'userRoleCollect') {
        dbOrRoleList = userRoleList;
        dbOrRoleLabel = getDbOrRoleCollectLabel('userRoleCollect', dbOrRoleList.length);
      }
      return {
        id: thisId,
        rootId: id,
        parentId: id,
        label: dbOrRoleLabel,
        name: childType,
        type: childType,
        connectInfo: cloneConnectInfo,
        isLeaf: false,
        children: dbOrRoleList,
      };
    }),
  };
};

const generateUserRoleList = async (rootId, parentId, uuid, connectInfo) => {
  const res = await getUserRoleList(uuid);
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

const generateDBList = async (rootId, parentId, uuid, connectInfo) => {
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
      type: 'database',
      isLeaf: false,
      children: [],
      isConnect,
      connectTime: hasConnectDb?.connectTime || null,
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
    const order = ['table', 'fun_pro', 'sequence', 'view', 'synonym'];
    let keys = Object.keys(obj);
    // Running results 'keys' such as: ['schema_name', 'table', 'view', 'fun_pro', 'synonym', 'sequence']
    keys = keys.sort((a, b) => {
      return order.indexOf(a) - order.indexOf(b);
    });
    // Running results 'keys' such as: ['schema_name', 'table', 'fun_pro', 'sequence', 'view', 'synonym']
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

const generateSchemaContent = (
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
    const { label, type, childType } = getLocalType(key);
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
  generateDBList,
  generateSchemaList,
  generateSchemaContentList,
  getLocalType,
  getDbOrRoleCollectLabel,
};
