import { useAppStore } from '@/store/modules/app';
import { i18n } from '@/i18n/index';
import { getDatabaseList } from '@/api/database';
import { getSchemaList, getSchemaObjects, getSchemaObjectCount } from '@/api/metaData';
import { getUserRoleList } from '@/api/userRole';
import { getTablespaceListApi } from '@/api/tablespace';

import { NodeEnum, FetchNode, ConnectInfo } from './types';

const t = i18n.global.t;

type schemaContentCollect =
  | 'table'
  | 'foreignTable'
  | 'trigger'
  | 'terminal'
  | 'sequence'
  | 'view'
  | 'synonym';

const getLocalType = (key: schemaContentCollect) => {
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
    case 'terminal':
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
const getI18nLocalType = (
  key: schemaContentCollect,
  oldLabel?: string, // If you want to get the count in old label
  newCount?: string | number,
) => {
  const oldCountMatch = oldLabel?.match(/\((.*?)\)/);
  return `${getLocalType(key).label}(${newCount ?? (oldCountMatch?.[1] || 0)})`;
};

const getFileType = (key) => {
  return {
    tableCollect: 'table',
    foreignTableCollect: 'foreignTable',
    triggerCollect: 'trigger',
    terminalCollect: 'terminal',
    sequenceCollect: 'sequence',
    viewCollect: 'view',
    synonymCollect: 'synonym',
  }[key];
};

const getSchemaContentListCount = async (schema, uuid) => {
  const res = await getSchemaObjectCount({ schema, uuid });
  return {
    table: res.table || 0,
    foreignTable: res.foreignTable || 0,
    trigger: res.trigger || 0,
    terminal: res.function || 0,
    sequence: res.sequence || 0,
    view: res.view || 0,
    synonym: res.synonym || 0,
  };
};

const getRootChildCollectLabel = (
  type: 'databaseCollect' | 'userRoleCollect' | 'tablespaceCollect' | 'job',
  count = null,
  showCount = true,
) => {
  const countText = showCount ? `(${count || 0})` : '';
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
  let res: any = { user: [], role: [] };
  try {
    res = await getUserRoleList({ uuid });
  } catch {
    res = { user: [], role: [] };
  }
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
  let data = [];
  try {
    data = (await getTablespaceListApi({ uuid })) as unknown as { name: string; oid: string }[];
  } catch (error) {
    data = [];
  }
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
  const data = (await getSchemaList({
    uuid,
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
  const orderArray = [
    'table',
    'foreignTable',
    'trigger',
    'terminal',
    'sequence',
    'view',
    'synonym',
  ];
  const array = [];
  const countObj = await getSchemaContentListCount(schemaName, uuid);
  orderArray.forEach((key: schemaContentCollect) => {
    const { label, type } = getLocalType(key);
    const keyId = `${parentId}_${key}`;
    array.push({
      id: keyId,
      rootId,
      parentId,
      uuid,
      label: getI18nLocalType(key, label, countObj[key]),
      name: label,
      type,
      key,
      connectInfo,
      databaseId,
      databaseName,
      schemaId,
      schemaName,
      children: [],
      isLeaf: false,
    });
  });
  return array;
};

const generateFileList = async (
  type: schemaContentCollect,
  rootId,
  parentId,
  uuid,
  databaseId,
  databaseName,
  schemaId,
  schemaName,
  connectInfo,
) => {
  if (
    !['table', 'foreignTable', 'trigger', 'terminal', 'sequence', 'view', 'synonym'].includes(type)
  )
    return [];
  const apiType = {
    table: 'table',
    foreignTable: 'foreignTable',
    trigger: 'trigger',
    terminal: 'function',
    view: 'view',
    synonym: 'synonym',
    sequence: 'sequence',
  };
  // getSchemaContentListCount(schemaName, uuid);
  const array = (await getSchemaObjects({
    schema: schemaName,
    uuid,
    type: apiType[type],
  })) as unknown as FetchNode[];
  if (!Array.isArray(array)) return [];
  const { childType } = getLocalType(type);
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
  generateFileList,
  getLocalType,
  getI18nLocalType,
  getFileType,
  getRootChildCollectLabel,
};
