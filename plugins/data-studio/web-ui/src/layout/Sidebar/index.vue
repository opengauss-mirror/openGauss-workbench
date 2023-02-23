<template>
  <div class="siderbar">
    <div class="database-wrapper group-wrapper">
      <div class="group-title-contain">
        <div class="left">
          <svg-icon icon-class="database" class-name="icon" /> {{ $t('database.list') }}
        </div>
        <div class="right">
          <el-button @click="openConnectDialog('create')" plain>
            {{ $t('connection.new') }}
          </el-button>
        </div>
      </div>
      <div class="group-search-contain">
        <el-input
          v-model="filterTreeText"
          :placeholder="$t('database.placeholder')"
          :prefix-icon="Search"
        />
      </div>
      <div class="group-main-contain">
        <el-tree
          :class="treeClass"
          ref="treeRef"
          node-key="id"
          :data="connectionList"
          :props="connectionProps"
          lazy
          :load="loadNode"
          :indent="14"
          highlight-current
          :filter-node-method="filterNode"
          @node-click="handleNodeClick"
          @node-contextmenu="handleContextmenu"
        >
          <template #default="{ data }">
            <svg-icon
              v-if="data.type == 'database'"
              :icon-class="data.isConnect ? 'database-connected' : 'database-disconnect'"
              class-name="icon"
            />
            <svg-icon v-if="data.type == 'public'" icon-class="public" class-name="icon" />
            <svg-icon v-if="data.type == 'person'" icon-class="person" class-name="icon" />
            <svg-icon
              v-if="data.type == 'terminalCollect'"
              icon-class="file-script"
              class-name="icon"
            />
            <svg-icon v-if="data.type == 'tableCollect'" icon-class="table" class-name="icon" />
            <svg-icon v-if="data.type == 'viewCollect'" icon-class="view" class-name="icon" />
            <svg-icon v-if="data.type == 'synonymCollect'" icon-class="synonym" class-name="icon" />
            <svg-icon
              v-if="data.type == 'sequenceCollect'"
              icon-class="sequence"
              class-name="icon"
            />
            <span>{{ data.label }}</span>
          </template>
        </el-tree>
        <div class="context-menu" :style="treeContext.MenuStyles">
          <ul
            v-if="treeContext.rootVisible"
            v-click-outside="
              () => {
                treeContext.rootVisible = false;
              }
            "
          >
            <li @click="openConnectInfo()">{{ $t('connection.props') }}</li>
            <li @click="openConnectDialog('edit')">{{ $t('connection.edit') }}</li>
            <li @click="handleDeleteConnect">{{ $t('connection.delete') }}</li>
            <li @click="handleDisAllconnection">{{ $t('connection.disAllconnection') }}</li>
            <li @click="handleCreateDb">{{ $t('database.create') }}</li>
            <li @click="refresh('connection')">{{ $t('connection.refresh') }}</li>
          </ul>
          <ul
            v-if="treeContext.databaseVisible"
            v-click-outside="
              () => {
                treeContext.databaseVisible = false;
              }
            "
          >
            <li :class="{ disabled: treeContextDbStatus }" @click="handleDbConnect(true)">
              {{ $t('database.open') }}
            </li>
            <li :class="{ disabled: !treeContextDbStatus }" @click="handleDbConnect(false)">
              {{ $t('database.close') }}
            </li>
            <li :class="{ disabled: !treeContextDbStatus }" @click="handleOpenNewTerminal">
              {{ $t('create.openNewTerminal') }}
            </li>
            <li :class="{ disabled: treeContextDbStatus }" @click="handleEditDb">
              {{ $t('database.edit') }}
            </li>
            <li :class="{ disabled: treeContextDbStatus }" @click="hanldeDeleteDb">
              {{ $t('database.remove') }}
            </li>
            <li :class="{ disabled: !treeContextDbStatus }" @click="handleDbInfo">
              {{ $t('database.property') }}
            </li>
            <li :class="{ disabled: !treeContextDbStatus }" @click="refresh('database')">
              {{ $t('connection.refresh') }}
            </li>
          </ul>
          <ul
            v-if="treeContext.modeVisible"
            v-click-outside="
              () => {
                treeContext.modeVisible = false;
              }
            "
          >
            <li @click="refresh('mode')"> {{ $t('connection.refresh') }}</li>
          </ul>
          <ul
            v-if="treeContext.terminalCollectVisible"
            v-click-outside="
              () => {
                treeContext.terminalCollectVisible = false;
              }
            "
          >
            <li @click="hanldeCreate('function')"> {{ $t('create.function') }}</li>
            <li @click="hanldeCreate('procedure')"> {{ $t('create.process') }}</li>
            <li @click="hanldeCreate('sql')"> {{ $t('create.sql') }}</li>
          </ul>
          <ul
            v-if="treeContext.functionSP"
            v-click-outside="
              () => {
                treeContext.functionSP = false;
              }
            "
          >
            <li @click="deleteConfirm('functionSP')"> {{ $t('delete.functionSP') }}</li>
          </ul>
          <ul
            v-if="treeContext.viewCollectVisible"
            v-click-outside="
              () => {
                treeContext.viewCollectVisible = false;
              }
            "
          >
            <li @click="showCreatDialog('view')"> {{ $t('create.view') }}</li>
          </ul>
          <ul
            v-if="treeContext.sequenceCollectVisible"
            v-click-outside="
              () => {
                treeContext.sequenceCollectVisible = false;
              }
            "
          >
            <li @click="showCreatDialog('sequence')"> {{ $t('create.sequence') }}</li>
          </ul>
          <ul
            v-if="treeContext.synonymCollectVisible"
            v-click-outside="
              () => {
                treeContext.synonymCollectVisible = false;
              }
            "
          >
            <li @click="showCreatDialog('synonym')"> {{ $t('create.synonym') }}</li>
          </ul>
          <ul
            v-if="treeContext.viewVisible"
            v-click-outside="
              () => {
                treeContext.viewVisible = false;
              }
            "
          >
            <li @click="deleteConfirm('view')"> {{ $t('delete.view') }}</li>
          </ul>
          <ul
            v-if="treeContext.sequenceVisible"
            v-click-outside="
              () => {
                treeContext.sequenceVisible = false;
              }
            "
          >
            <li @click="deleteConfirm('sequence')"> {{ $t('delete.sequence') }}</li>
          </ul>
          <ul
            v-if="treeContext.synonymVisible"
            v-click-outside="
              () => {
                treeContext.synonymVisible = false;
              }
            "
          >
            <li @click="deleteConfirm('synonym')"> {{ $t('delete.synonym') }}</li>
          </ul>
        </div>
      </div>
    </div>
    <div class="windows-wrapper group-wrapper">
      <div class="group-title-contain">
        <div class="left">
          <svg-icon icon-class="window" class-name="icon" /> {{ $t('windows.list') }}
        </div>
      </div>
      <div class="group-search-contain">
        <el-input v-model="input2" :placeholder="$t('windows.placeholder')" :prefix-icon="Search" />
      </div>
      <div class="group-main-contain">
        <div class="windows-list">
          <div
            v-for="tag in visitedViews"
            :key="tag.path"
            @click="routerGo(tag)"
            class="windows-item-wrapper"
          >
            <div
              v-if="tag.path == '/home'"
              class="windows-item home"
              :class="isActive(tag) ? 'active' : ''"
            >
              <svg-icon :icon-class="tag.meta.icon" class-name="icon" />
              <span class="name">{{ $t('windows.home') }}</span>
            </div>
            <div v-else class="windows-item" :class="isActive(tag) ? 'active' : ''">
              <svg-icon :icon-class="tag.meta.icon" class-name="icon" />
              <span class="name">{{ tag.title }}</span>
            </div>
          </div>
        </div>
      </div>
    </div>
    <CreateDbDialog :type="createDbType" v-if="showCreateDbDialog" v-model="showCreateDbDialog" />
    <DbInfoDialog
      v-if="showDbInfoDialog"
      v-model="showDbInfoDialog"
      :uuid="currentContextNodeData.uuid"
      :databaseName="currentContextNodeData.label"
    />
    <CreateViewDialog type="create" :connectData="currentContextNodeData" v-model="viewDialog" />
    <CreateSynonymDialog
      type="create"
      :connectData="currentContextNodeData"
      v-model="synonymDialog"
    />
    <CreateSequenceDialog
      type="create"
      :connectData="currentContextNodeData"
      v-model="sequenceDialog"
    />
  </div>
</template>

<script lang="ts" setup name="Siderbar">
  import { Search } from '@element-plus/icons-vue';
  import { ElMessage, ElMessageBox, ElTree } from 'element-plus';
  import CreateDbDialog from './CreateDbDialog.vue';
  import DbInfoDialog from './DbInfoDialog.vue';
  import CreateViewDialog from '@/views/view/CreateViewDialog.vue';
  import CreateSynonymDialog from '@/views/synonym/CreateSynonymDialog.vue';
  import CreateSequenceDialog from '@/views/sequence/CreateSequenceDialog.vue';
  import { computed, onMounted, reactive, ref, onUnmounted, watch } from 'vue';
  import { useRoute, useRouter } from 'vue-router';
  import { useTagsViewStore } from '@/store/modules/tagsView';
  import { useAppStore } from '@/store/modules/app';
  import vClickOutside from '@/directives/clickOutside';
  import EventBus, { EventTypeName } from '@/utils/event-bus';
  import { getSchemaList, getSchemaObjectList } from '@/api/metaData';
  import { loadingInstance } from '@/utils';
  import { useI18n } from 'vue-i18n';
  import { closeConnections } from '@/api/connect';
  import { dropFunctionSP } from '@/api/functionSP';
  import { dropView } from '@/api/view';
  import { dropSequence } from '@/api/sequence';
  import { dropSynonym } from '@/api/synonym';
  import { getDatabaseList, openDatabaseConnection, deleteDatabase } from '@/api/database';
  import { useUserStore } from '@/store/modules/user';
  import { connectListPersist } from '@/config';

  const AppStore = useAppStore();
  const treeRef = ref<InstanceType<typeof ElTree>>();
  const filterTreeText = ref<string>('');
  const input2 = ref<string>('');
  const { t } = useI18n();

  /* There are the following types of nodeType:
  {
    root = 'root',
    database = 'database',
    public = 'public', // schema
    person = 'person', // schema
    tableCollect = 'tableCollect',
    table = 'table',
    terminalCollect = 'codeCollect',
    terminal = 'terminal',
    viewCollect = 'viewCollect',
    view = 'view',
    synonymCollect = 'synonymCollect',
    synonym = 'synonym',
    sequenceCollect = 'sequenceCollect',
    sequence = 'sequence',
  } */

  interface Tree {
    id: string;
    label: string;
    isLeaf?: boolean;
    type?: string;
    isConnect?: boolean;
    uuid?: string;
    children?: Tree[];
    connectInfo: ConnectInfo;
  }
  interface ConnectInfo {
    name: string;
    id: string;
    ip: string;
    port: string;
    userName: string;
    uuid?: string;
    type?: string;
    driver?: '';
    password?: string;
    webUser: string;
  }

  const treeClass = ref('no-tree');
  const showCreateDbDialog = ref(false);
  const showDbInfoDialog = ref(false);
  const viewDialog = ref(false);
  const synonymDialog = ref(false);
  const sequenceDialog = ref(false);
  const UserStore = useUserStore();
  const connectInfo: ConnectInfo = reactive({
    name: '',
    id: '',
    ip: '',
    port: '',
    dataName: '',
    userName: '',
    webUser: '',
  });
  const connectionProps = {
    value: 'id',
    label: 'label',
    children: 'children',
    class: 'my-class',
    isLeaf: 'isLeaf',
  };
  const connectionList = ref<Array<Tree>>([]);
  const currentContextNodeData = reactive({
    id: '',
    uuid: '',
    connectInfo: { id: '', name: '' } as ConnectInfo,
    databaseId: '',
    databaseName: '',
    schemaId: '',
    schemaName: '',
    label: '',
    children: [],
  });
  let loading = ref(null);
  const createDbType = ref<'create' | 'edit'>('create');

  const loadNode = async (node, resolve) => {
    console.log('node', node);
    
    if (node.data.children && node.data.children.length) {
      return resolve(node.data.children);
    } else if (node.data?.type == 'database' && node.data?.isConnect) {
      const data = await fetchSchemaList(
        node.data.id,
        node.data.id,
        node.data.label,
        node.data.uuid,
        node.data.connectInfo,
      );
      resolve(data);
    } else if (['public', 'person'].includes(node.data?.type)) {
      const data = await fetchSchemaContentList(
        node.data.id,
        node.data.uuid,
        node.data.databaseId,
        node.data.databaseName,
        node.data.id,
        node.data.label,
        node.data.connectInfo,
      );
      connectionList.value.forEach((connectItem) => {
        if (connectItem.id == node.data.connectInfo.id) {
          const dbList = connectItem.children;
          for (let j = 0; j < dbList.length; j++) {
            if (dbList[j].label == node.data.databaseName) {
              const schemaList = dbList[j].children;
              for (let k = 0; k < schemaList.length; k++) {
                if (schemaList[k].id == node.data.id) {
                  schemaList[k].children = data;
                  break;
                }
              }
              break;
            }
          }
        }
      });
      updateConnectListPersist();
      return resolve(data);
    } else {
      return resolve([]);
    }
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
        type: 'root',
        connectInfo: cloneConnectInfo,
        isLeaf: false,
        children: data.map((dbName) => {
          return {
            id: `${id}_${dbName}`,
            parentId: id,
            uuid,
            label: dbName,
            connectInfo: cloneConnectInfo,
            type: 'database',
            isLeaf: false,
            children: [],
            isConnect:
              AppStore.connectListMap[connectInfo.id]?.connectedDatabase.findIndex(
                (item) => item.name == dbName,
              ) > -1,
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

  const fetchSchemaList = async (parentId, databaseId, databaseName, uuid, connectInfo) => {
    const data = (await getSchemaList({
      uuid,
      connectionName: connectInfo.userName,
      webUser: UserStore.userId,
    })) as unknown as string[];
    const schemaList = [];
    data.forEach((schemaName) => {
      const obj = {
        id: `${parentId}_${schemaName}`,
        parentId,
        label: schemaName,
        connectInfo,
        databaseName,
        databaseId,
        uuid,
        type: schemaName === 'public' ? 'public' : 'person',
        isLeaf: false,
        children: [],
      };
      schemaName === 'public' ? schemaList.unshift(obj) : schemaList.push(obj);
    });
    connectionList.value.forEach((item) => {
      if (item.id == connectInfo.id) {
        const dbList = item.children;
        for (let j = 0; j < dbList.length; j++) {
          if (dbList[j].label == databaseName) {
            dbList[j].children = schemaList;
            break;
          }
        }
      }
    });
    updateConnectListPersist();
    return schemaList;
  };

  const fetchSchemaContentList = async (
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
      const { label, type, zType } = sortType(key);
      const keyId = `${parentId}_${key}`;
      array.push({
        id: keyId,
        parentId,
        uuid,
        label: `${label} (${obj[key].length})`,
        type,
        key,
        connectInfo,
        databaseId,
        databaseName,
        schemaId,
        schemaName,
        children: handleTypeList(
          obj[key],
          zType,
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
    const obj = { label: '', type: '', zType: '' };
    switch (key) {
      case 'table':
        obj.label = t('database.regular_table');
        obj.type = 'tableCollect';
        obj.zType = 'table';
        break;
      case 'fun_pro':
        obj.label = t('database.function_process');
        obj.type = 'terminalCollect';
        obj.zType = 'terminal';
        break;
      case 'view':
        obj.label = t('database.view');
        obj.type = 'viewCollect';
        obj.zType = 'view';
        break;
      case 'synonym':
        obj.label = t('database.synonym');
        obj.type = 'synonymCollect';
        obj.zType = 'synonym';
        break;
      case 'sequence':
        obj.label = t('database.sequence');
        obj.type = 'sequenceCollect';
        obj.zType = 'sequence';
        break;
    }
    return obj;
  };
  // User Mode-children data - [table/func/view]...
  const handleTypeList = (
    array,
    zType,
    parentId,
    uuid,
    databaseId,
    databaseName,
    schemaId,
    schemaName,
    connectInfo,
  ) => {
    return array.map((item) => ({
      id: `${parentId}_${item}`,
      parentId,
      uuid,
      label: item,
      type: zType,
      databaseId,
      databaseName,
      schemaId,
      schemaName,
      connectInfo,
      isLeaf: true,
    }));
  };

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
  const openConnectDialog = (type) => {
    treeContext.rootVisible = false;
    EventBus.notify(EventTypeName.OPEN_CONNECT_DIALOG, {
      dialogType: type,
      connectInfo: currentContextNodeData.connectInfo,
    });
  };
  const openConnectInfo = () => {
    treeContext.rootVisible = false;
    EventBus.notify(EventTypeName.OPEN_CONNECT_INFO_DIALOG, currentContextNodeData.connectInfo);
  };
  const updateConnectListPersist = () => {
    connectListPersist.storage.setItem(
      connectListPersist.key,
      JSON.stringify(connectionList.value),
    );
  };
  // const updateLastestConnectDatabse = (rootId, databaseName, uuid) => {
  //   AppStore.lastestConnectDatabase = { rootId, databaseName, uuid };
  // };
  const updateConnectListMap = () => {
    const obj = {};
    connectionList.value.forEach((list) => {
      obj[list.id] = {
        info: { ...list.connectInfo },
        connectedDatabase: list.children
          .filter((item) => item.isConnect)
          .map((item) => ({ name: item.label, uuid: item.uuid })),
      };
    });
    AppStore.connectListMap = obj;
  };
  const handleDeleteConnect = () => {
    treeContext.rootVisible = false;
    ElMessageBox.confirm(
      t('connection.message.deleteConnect', { name: currentContextNodeData.connectInfo.name }),
    ).then(async () => {
      for (let i = 0; i < currentContextNodeData.children.length; i++) {
        const db = currentContextNodeData.children[i];
        if (db.isConnect) {
          await closeConnections(db.uuid);
        }
      }
      const index = connectionList.value.findIndex((item) => item.id == currentContextNodeData.id);
      treeRef.value.remove(treeRef.value.getNode(currentContextNodeData.id));
      index > -1 && connectionList.value.splice(index, 1);
      if (AppStore.currentConnectNode.id == currentContextNodeData.id) {
        AppStore.currentConnectNode = connectionList.value[0] || {};
      }
      updateConnectListPersist();
      updateConnectListMap();
      if (connectionList.value.length == 0) router.push('/home');
    });
  };
  const handleDisAllconnection = () => {
    treeContext.rootVisible = false;
    ElMessageBox.confirm(
      t('connection.message.deleteAllConnect', { name: currentContextNodeData.connectInfo.name }),
    ).then(async () => {
      for (let i = 0; i < connectionList.value.length; i++) {
        const list = connectionList.value[i];
        if (list.id == currentContextNodeData.id) {
          for (let j = 0; j < list.children.length; j++) {
            const db = list.children[j];
            if (db.isConnect) {
              await closeConnections(db.uuid);
              db.children = [];
              db.isConnect = false;
              db.uuid = '';
            }
          }
        }
      }
      if (AppStore.currentConnectNode.id == currentContextNodeData.id) {
        AppStore.currentConnectNode = connectionList.value[0] || {};
      }
      updateConnectListPersist();
      updateConnectListMap();
    });
  };
  const handleDbConnect = async (willOpen) => {
    treeContext.databaseVisible = false;
    const { id } = currentContextNodeData.connectInfo;
    let mergeObj = {};
    if (willOpen) {
      const params = {
        id,
        dataName: currentContextNodeData.label,
        webUser: UserStore.userId,
      };
      const res: any = await openDatabaseConnection(params);
      mergeObj = { uuid: res.connectionid };
      AppStore.lastestConnectDatabase = {
        rootId: id,
        databaseName: currentContextNodeData.label,
        uuid: res.connectionid,
      };
    } else {
      await closeConnections(currentContextNodeData.uuid);
      mergeObj = { uuid: '' };
    }
    for (let i = 0; i < connectionList.value.length; i++) {
      const list = connectionList.value[i];
      if (list.id == currentContextNodeData.connectInfo.id) {
        for (let j = 0; j < list.children.length; j++) {
          const db = list.children[j];
          if (db.id == currentContextNodeData.id) {
            Object.assign(db, {
              isConnect: willOpen,
              ...mergeObj,
            });
            break;
          }
        }
        break;
      }
    }
    updateConnectListPersist();
    updateConnectListMap();
  };
  const handleOpenNewTerminal = () => {
    treeContext.databaseVisible = false;
    const { name: connectInfoName, id: rootId } = currentContextNodeData.connectInfo;
    const dbname = currentContextNodeData.label;
    const terminalNum = TagsViewStore.maxTerminalNum + 1;
    const title = `${dbname}@${connectInfoName}(${terminalNum})`;
    const time = Date.now();
    router.push({
      path: '/createTerminal/' + time,
      query: {
        title,
        rootId,
        connectInfoName,
        uuid: currentContextNodeData.uuid,
        dbname,
        // schema: 
        terminalNum,
        time,
      },
    });
  };
  const handleCreateDb = () => {
    treeContext.rootVisible = false;
    showCreateDbDialog.value = true;
    createDbType.value = 'create';
  };
  const handleEditDb = () => {
    treeContext.databaseVisible = false;
    showCreateDbDialog.value = true;
    createDbType.value = 'edit';
  };
  const hanldeDeleteDb = () => {
    treeContext.databaseVisible = false;
    ElMessageBox.confirm(
      t('connection.message.deleteDatabase', { name: currentContextNodeData.label }),
    ).then(async () => {
      const res = await deleteDatabase({
        uuid: currentContextNodeData.uuid,
        databaseName: currentContextNodeData.label,
      });
      console.log('hanldeDeleteDb', res);
      
    });
  };
  const handleDbInfo = () => {
    treeContext.databaseVisible = false;
    showDbInfoDialog.value = true;
  };
  interface RefreshOptions {
    connectInfo: ConnectInfo | undefined;
    connectionId: string;
    uuid: string;
    databaseName: string;
    schemaId: string;
    nodeId: string;
  }
  const refresh = (
    mode: 'connection' | 'database' | 'mode',
    options: Partial<RefreshOptions> = {
      connectInfo: undefined,
      connectionId: '',
      uuid: '',
      databaseName: '',
      schemaId: '',
      nodeId: '',
    },
  ) => {
    if (mode == 'connection') {
      treeContext.rootVisible = false;
      fetchDBList(
        options.connectInfo || currentContextNodeData.connectInfo,
        options.uuid || currentContextNodeData.uuid,
      );
    } else if (mode == 'database') {
      treeContext.databaseVisible = false;
      connectionList.value.forEach((item) => {
        if (item.id == (options.connectionId || currentContextNodeData.connectInfo.id)) {
          const dbList = item.children;
          for (let j = 0; j < dbList.length; j++) {
            if (dbList[j].label == (options.databaseName || currentContextNodeData.label)) {
              dbList[j].children = [];
              break;
            }
          }
        }
      });
    } else if (mode == 'mode') {
      treeContext.modeVisible = false;
      connectionList.value.forEach((item) => {
        if (item.id == (options.connectionId || currentContextNodeData.connectInfo.id)) {
          const dbList = item.children;
          for (let j = 0; j < dbList.length; j++) {
            if (dbList[j].label == (options.databaseName || currentContextNodeData.databaseName)) {
              const schemaList = dbList[j].children;
              for (let k = 0; k < schemaList.length; k++) {
                if (schemaList[k].id == (options.schemaId || currentContextNodeData.id)) {
                  schemaList[k].children = [];
                  break;
                }
              }
              break;
            }
          }
        }
      });
    }
    let node = treeRef.value.getNode(options.nodeId || currentContextNodeData.id);
    if (node) {
      node.loaded = false;
      node.expand();
    }
  };

  const filterNode = (value: string, data: Tree) => {
    if (!value) return true;
    if (!data.label) return false;
    return data.label.includes(value);
  };

  // const handleTreeCurrentChange = (target) => {
  //   AppStore.updateCurrentNode(target.data);
  // };

  const handleNodeClick = (target, node) => {
    Object.keys(treeContext).forEach((item) => {
      if (item.indexOf('Visible') !== -1) treeContext[item] = false;
    });
    if (node.isLeaf) {
      const { schemaName, databaseName, uuid } = target;
      const { name: connectInfoName, id: rootId } = target.connectInfo;
      const commonParams = {
        title: `${schemaName}.${target.label}-${databaseName}@${connectInfoName}`,
        rootId,
        connectInfoName,
        uuid,
        dbname: databaseName,
        schema: schemaName,
      };
      if (target.type == 'table') {
        router.push({
          path: `/table/${target.id}`,
          query: {
            tableName: target.label,
            ...commonParams,
          },
        });
      } else if (target.type == 'terminal') {
        setTimeout(() => {
          const terminalNum = TagsViewStore.maxTerminalNum + 1;
          const time = Date.now();
          router.push({
            path: `/debug/${encodeURIComponent(target.id)}`,
            query: {
              funcname: target.label,
              terminalNum,
              ...commonParams,
              time,
            },
          });
        }, 300);
      } else if (target.type == 'view') {
        router.push({
          path: `/view/${target.id}`,
          query: {
            viewName: target.label,
            ...commonParams,
          },
        });
      } else if (target.type == 'sequence') {
        router.push({
          path: `/sequence/${target.id}`,
          query: {
            sequenceName: target.label,
            ...commonParams,
          },
        });
      } else if (target.type == 'synonym') {
        router.push({
          path: `/synonym/${target.id}`,
          query: {
            synonymName: target.label,
            ...commonParams,
          },
        });
      }
    }
  };

  const TagsViewStore = useTagsViewStore();
  const visitedViews = computed(() => {
    return input2.value
      ? TagsViewStore.visitedViews.filter((item) => item.title.includes(input2.value))
      : TagsViewStore.visitedViews;
  });
  const route = useRoute();
  const router = useRouter();
  const isActive = (rou) => {
    return rou.path === route.path;
  };
  const routerGo = (tag) => {
    router.push({
      path: tag.path,
      query: tag.query,
    });
  };

  const treeContext = reactive({
    MenuStyles: {
      top: '0',
      left: '0',
    },
    rootVisible: false,
    databaseVisible: false,
    modeVisible: false,
    terminalCollectVisible: false,
    functionSP: false,
    viewCollectVisible: false,
    synonymCollectVisible: false,
    sequenceCollectVisible: false,
    viewVisible: false,
    sequenceVisible: false,
    synonymVisible: false,
  });
  const treeContextDbStatus = computed(() => {
    return Boolean(
      AppStore.connectListMap[currentContextNodeData.connectInfo.id]?.connectedDatabase.findIndex(
        (item) => item.name == currentContextNodeData.label,
      ) > -1,
    );
  });
  const handleContextmenu = (event, data) => {
    const type = data.type;
    Object.assign(currentContextNodeData, data);
    treeContext.MenuStyles = {
      top: event.y + 2 + 'px',
      left: event.x + 2 + 'px',
    };
    Object.keys(treeContext).forEach((item) => {
      if (item.indexOf('Visible') !== -1) treeContext[item] = false;
    });
    switch (type) {
      case 'root':
        treeContext.rootVisible = true;
        break;
      case 'database':
        treeContext.databaseVisible = true;
        break;
      case 'public':
        treeContext.modeVisible = true;
        break;
      case 'person':
        treeContext.modeVisible = true;
        break;
      case 'terminalCollect':
        treeContext.terminalCollectVisible = true;
        break;
      case 'terminal':
        treeContext.functionSP = true;
        break;
      case 'viewCollect':
        treeContext.viewCollectVisible = true;
        break;
      case 'view':
        treeContext.viewVisible = true;
        break;
      case 'sequence':
        treeContext.sequenceVisible = true;
        break;
      case 'sequenceCollect':
        treeContext.sequenceCollectVisible = true;
        break;
      case 'synonymCollect':
        treeContext.synonymCollectVisible = true;
        break;
      case 'synonym':
        treeContext.synonymVisible = true;
        break;
      default:
        break;
    }
  };
  const hanldeCreate = (type: 'function' | 'procedure' | 'sql') => {
    enum titleMap {
      function = 'create_F',
      procedure = 'create_P',
      sql = 'create_SQL',
    }
    const time = Date.now();
    const connectInfo: any = currentContextNodeData.connectInfo;
    router.push({
      path: '/createDebug/' + time,
      query: {
        type,
        title: titleMap[type],
        dbname: connectInfo.dataName,
        connectInfoName: connectInfo.name,
        connectInfoId: connectInfo.id,
        schema: currentContextNodeData.schemaName,
        time,
      },
    });
    treeContext.terminalCollectVisible = false;
  };

  const showCreatDialog = (type: 'view' | 'synonym' | 'sequence') => {
    switch (type) {
      case 'view':
        viewDialog.value = true;
        treeContext.viewCollectVisible = false;
        break;
      case 'synonym':
        synonymDialog.value = true;
        treeContext.synonymCollectVisible = false;
        break;
      case 'sequence':
        sequenceDialog.value = true;
        treeContext.sequenceCollectVisible = false;
        break;
      default:
        break;
    }
  };

  const deleteConfirm = async (type: 'functionSP' | 'view' | 'synonym' | 'sequence') => {
    let api;
    switch (type) {
      case 'functionSP':
        treeContext.functionSP = false;
        api = dropFunctionSP;
        break;
      case 'view':
        treeContext.viewVisible = false;
        api = dropView;
        break;
      case 'sequence':
        treeContext.sequenceVisible = false;
        api = dropSequence;
        break;
      case 'synonym':
        treeContext.synonymVisible = false;
        api = dropSynonym;
    }
    ElMessageBox.confirm(
      `${t('common.confirm')}
      ${t(`delete.${type}`)}${t('common.colon')}
      ${currentContextNodeData.label}
      ${t('common.if')}`,
    ).then(async () => {
      const params = {
        connectionName: currentContextNodeData.connectInfo.name,
        schema: currentContextNodeData.schemaName,
        [type + 'Name']: currentContextNodeData.label,
        webUser: UserStore.userId,
        uuid: currentContextNodeData.uuid,
      };
      await api(params);
      ElMessage.success(t('message.deleteFile'));
      // ElMessage.success(`${t(`delete.${type}`)}${t('success')}`);
      // refresh('mode', {
      //   connectionId: currentContextNodeData.connectInfo.id,
      //   databaseName: currentContextNodeData.databaseName,
      //   schemaId: currentContextNodeData.schemaId,
      //   nodeId: currentContextNodeData.schemaId,
      // });
    });
  };

  const resetExpandNodes = () => {
    connectionList.value.forEach((level1) => {
      connectionList.value.length <= 1
        ? treeRef.value.getNode(level1.id).expand()
        : treeRef.value.getNode(level1.id).collapse();
      level1.children?.forEach((level2) => {
        treeRef.value.getNode(level2.id).collapse();
      });
    });
  };

  const getLanguage = computed(() => {
    return AppStore.language;
  });

  watch(
    getLanguage,
    (newVal, oldVal) => {
      if (oldVal) {
        const list = getAllConnectList();
        list.forEach((listItem) => {
          if (listItem.children) {
            listItem.children.forEach((dbItem) => {
              dbItem.children.forEach((schemaItem) => {
                schemaItem.children.forEach((type) => {
                  type.label = `${sortType(type.key).label} (${type.children.length})`;
                });
              });
            });
          }
        });
        connectionList.value = list;
        updateConnectListPersist();
      }
    },
    { immediate: true, deep: true },
  );

  watch(
    connectionList,
    (val) => {
      treeClass.value = val.length ? 'tree' : 'no-tree';
    },
    { deep: true },
  );

  watch(filterTreeText, (val) => {
    treeRef.value!.filter(val);
    treeClass.value = treeRef.value.isEmpty ? 'no-tree' : 'tree';
    if (!val) resetExpandNodes();
  });

  onMounted(async () => {
    // maybe update database list, and schema, databaseName... so must close all tabs.
    EventBus.listen(EventTypeName.GET_DATABASE_LIST, async (connectData) => {
      EventBus.notify(EventTypeName.CLOSE_ALL_TAB);
      connectData && Object.assign(connectInfo, connectData);
      const { connectionid, ...info } = connectData;
      AppStore.connectListMap[connectData.id] = {
        info,
        connectedDatabase: [
          {
            name: connectData.dataName,
            uuid: connectionid,
          },
        ],
      };
      AppStore.lastestConnectDatabase = {
        rootId: connectData.id,
        databaseName: connectData.dataName,
        uuid: connectionid,
      };
      await fetchDBList(info, connectionid);
      parent.location.reload();
    });
    EventBus.listen(EventTypeName.UPDATE_DATABASE_LIST, (connectData) => {
      connectData && Object.assign(connectInfo, connectData);
      const { connectionid, ...info } = connectData;
      AppStore.connectListMap[connectData.id] = {
        info,
        connectedDatabase: [
          {
            name: connectData.dataName,
            uuid: connectionid,
          },
        ],
      };
      AppStore.lastestConnectDatabase = {
        rootId: connectData.id,
        databaseName: connectData.dataName,
        uuid: connectionid,
      };
      fetchDBList(info, connectionid);
    });
    // EventBus.listen(EventTypeName.REFRESH_DATABASE_LIST, (config) => {
    //   refresh(config.mode, config.options);
    // });

    connectionList.value = getAllConnectList();
  });
  onUnmounted(() => {
    EventBus.unListen(EventTypeName.GET_DATABASE_LIST);
    EventBus.unListen(EventTypeName.UPDATE_DATABASE_LIST);
    // EventBus.unListen(EventTypeName.REFRESH_DATABASE_LIST);
  });
</script>

<style lang="scss">
  .el-menu-vertical-demo:not(.el-menu--collapse) {
    height: 100%;
  }

  .crollbar-wrapper {
    height: 100%;

    .el-scrollbar__view {
      height: 100%;
    }
  }
  .el-tree {
    .el-tree-node__content {
      height: 22px;
      & > .el-tree-node__expand-icon {
        padding: 4px;
      }
    }
    .el-tree-node__label {
      line-height: 22px;
    }
  }
  .el-tree-node:focus > .el-tree-node__content > .el-tree-node__label {
    background-color: var(--el-tree-node-hover-bg-color);
  }
  .el-tree-node__label:hover {
    background-color: var(--el-tree-node-hover-bg-color);
  }
  .el-tree--highlight-current
    .el-tree-node.is-current
    > .el-tree-node__content
    .el-tree-node__label,
  .el-tree--highlight-current .el-tree-node.is-current > .el-tree-node__content {
    background-color: var(--el-color-primary) !important;
    color: #fff;
    i,
    .icon {
      color: #fff;
    }
  }
</style>
<style lang="scss" scoped>
  @function border-style() {
    @return 1px solid #d8d8d8;
  }
  .siderbar {
    display: flex;
    flex-direction: column;
    padding: 8px;
    user-select: none;
  }
  .tree {
    width: fit-content;
  }
  .no-tree {
    width: auto;
  }
  .database-wrapper {
    height: 66%;
    flex-grow: 0;
  }

  .windows-wrapper {
    height: calc(34% - 15px);
    margin-top: 15px;
  }
  .group-wrapper {
    border: border-style();
    display: flex;
    flex-direction: column;
  }
  .icon {
    margin-right: 6px;
    color: #75757e;
  }
  .group-title-contain {
    padding: 3px 5px;
    display: flex;
    justify-content: space-between;
    border-bottom: border-style();
    .icon {
      font-size: 19px;
    }
    .el-icon {
      font-size: 18px;
      color: #75757e;
      cursor: pointer;
      :hover {
        color: var(--el-color-primary);
      }
    }
  }
  .group-search-contain {
    margin: 5px;
  }
  .group-main-contain {
    flex: 1;
    overflow: auto;
    margin: 5px;
    margin-top: 0;
    border: border-style();
    border-radius: 4px;
    position: relative;
  }
  .windows-list {
    width: fit-content;
    .windows-item {
      white-space: nowrap;
      cursor: pointer;
      margin: 3px;
      padding: 5px;
      &:hover {
        background: var(--el-bg-color-bar);
      }
      .icon {
        font-size: 13px;
      }
      .name {
        font-size: 12px;
      }
      &.home,
      &.home .icon {
        color: red;
      }
      &.active {
        background-color: var(--normal-color);
      }
      &.active,
      &.active .icon {
        color: #fff;
      }
    }
  }

  .context-menu {
    position: fixed;
    z-index: 100;
    box-shadow: 0px 0px 12px rgba(0, 0, 0, 0.12);
    ul {
      padding: 3px 0;
      margin: 0;
      list-style-type: none;
      border-radius: 4px;
      background-color: var(--el-bg-color-overlay);
    }
    li {
      padding: 2px 12px;
      line-height: 20px;
      font-size: 12px;
      display: flex;
      align-items: center;
      white-space: nowrap;
      list-style: none;
      margin: 0;
      color: var(--normal-color);
      cursor: pointer;
      outline: 0;
      &.disabled {
        // color: #c5c8ce;
        color: var(--el-color-info);
        cursor: not-allowed;
      }
      &:not(.disabled):hover {
        background-color: var(--color-secondary-disabled);
      }
    }
  }
</style>
