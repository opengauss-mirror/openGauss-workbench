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
            <li @click="openConnectInfo()"> {{ $t('connection.props') }} </li>
            <li @click="openConnectDialog('edit')"> {{ $t('connection.edit') }} </li>
            <li @click="handleDeleteConnect">{{ $t('connection.delete') }}</li>
            <li
              :class="{ disabled: !treeContextFindAvailableUuid }"
              @click="treeContextFindAvailableUuid && handleDisAllconnection()"
            >
              {{ $t('connection.disAllconnection') }}
            </li>
            <li
              :class="{ disabled: !treeContextFindAvailableUuid }"
              @click="!!treeContextFindAvailableUuid && handleCreateDb()"
            >
              {{ $t('database.create') }}
            </li>
            <li
              :class="{ disabled: !treeContextFindAvailableUuid }"
              @click="
                treeContextFindAvailableUuid &&
                  refresh('connection', { uuid: treeContextFindAvailableUuid })
              "
            >
              {{ $t('connection.refresh') }}
            </li>
          </ul>
          <ul
            v-if="treeContext.databaseVisible"
            v-click-outside="
              () => {
                treeContext.databaseVisible = false;
              }
            "
          >
            <li
              :class="{ disabled: treeContextDbStatus }"
              @click="
                !treeContextDbStatus &&
                  handleDbConnect(
                    currentContextNodeData.connectInfo.id,
                    currentContextNodeData.id,
                    true,
                  )
              "
            >
              {{ $t('database.open') }}
            </li>
            <li
              :class="{ disabled: !treeContextDbStatus }"
              @click="
                treeContextDbStatus &&
                  handleDbConnect(
                    currentContextNodeData.connectInfo.id,
                    currentContextNodeData.id,
                    false,
                  )
              "
            >
              {{ $t('database.close') }}
            </li>
            <li
              :class="{ disabled: !treeContextDbStatus }"
              @click="treeContextDbStatus && handleOpenNewTerminal()"
            >
              {{ $t('create.openNewTerminal') }}
            </li>
            <li
              :class="{ disabled: treeContextDbStatus }"
              @click="!treeContextDbStatus && handleEditDb()"
            >
              {{ $t('database.edit') }}
            </li>
            <li
              :class="{ disabled: treeContextDbStatus }"
              @click="!treeContextDbStatus && hanldeDeleteDb()"
            >
              {{ $t('database.remove') }}
            </li>
            <li
              :class="{ disabled: !treeContextDbStatus }"
              @click="treeContextDbStatus && handleDbInfo()"
            >
              {{ $t('database.property') }}
            </li>
            <li
              :class="{ disabled: !treeContextDbStatus }"
              @click="
                treeContextDbStatus &&
                  refresh('database', {
                    rootId: currentContextNodeData.connectInfo.id,
                    databaseId: currentContextNodeData.id,
                  })
              "
            >
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
            <li
              @click="
                refresh('mode', {
                  rootId: currentContextNodeData.connectInfo.id,
                  databaseId: currentContextNodeData.databaseId,
                  schemaId: currentContextNodeData.id,
                })
              "
            >
              {{ $t('connection.refresh') }}
            </li>
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
            v-if="treeContext.functionSPVisible"
            v-click-outside="
              () => {
                treeContext.functionSPVisible = false;
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
    <CreateDbDialog
      v-if="showCreateDbDialog"
      v-model="showCreateDbDialog"
      :type="createDbType"
      :data="currentContextNodeData"
      :uuid="treeContextFindAvailableUuid"
      @success="createDbSuccess"
    />
    <DbInfoDialog
      v-if="showDbInfoDialog"
      v-model="showDbInfoDialog"
      :uuid="currentContextNodeData.uuid"
      :databaseName="currentContextNodeData.label"
    />
    <CreateViewDialog
      type="create"
      :connectData="currentContextNodeData"
      v-model="viewDialog"
      @success="refreshModeByContext"
    />
    <CreateSynonymDialog
      v-if="synonymDialog"
      v-model="synonymDialog"
      type="create"
      :connectData="currentContextNodeData"
      @success="refreshModeByContext"
    />
    <CreateSequenceDialog
      v-if="sequenceDialog"
      v-model="sequenceDialog"
      type="create"
      :connectData="currentContextNodeData"
      @success="refreshModeByContext"
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
  import { computed, nextTick, onMounted, reactive, ref, onUnmounted, watch } from 'vue';
  import { useRoute, useRouter } from 'vue-router';
  import { useAppStore } from '@/store/modules/app';
  import { useTagsViewStore } from '@/store/modules/tagsView';
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
  const TagsViewStore = useTagsViewStore();
  const route = useRoute();
  const router = useRouter();
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
    parentId: string;
    label: string;
    name: string;
    isLeaf?: boolean;
    type?: string;
    isConnect?: boolean;
    connectTime: null | number;
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
    name: '',
    children: [],
  });
  let loading = ref(null);
  const createDbType = ref<'create' | 'edit'>('create');

  const isActive = (rou) => {
    return rou.path === route.path;
  };
  const routerGo = (tag) => {
    router.push({
      path: tag.path,
      query: tag.query,
    });
  };
  const findNode = (
    options: { rootId: string; databaseId?: string; schemaId?: string } = {
      rootId: '',
      databaseId: '',
      schemaId: '',
    },
  ) => {
    for (let i = 0; i < connectionList.value.length; i++) {
      const cListItem = connectionList.value[i];
      if (cListItem.id == options.rootId) {
        if (!options.databaseId) return cListItem;
        for (let j = 0; j < cListItem.children.length; j++) {
          const dbListItem = cListItem.children[j];
          if (dbListItem.id == options.databaseId) {
            if (!options.schemaId) return dbListItem;
            for (let k = 0; k < dbListItem.children.length; k++) {
              const schemaItem = dbListItem.children[k];
              if (schemaItem.id == options.schemaId) {
                return schemaItem;
              }
            }
          }
        }
      }
    }
  };
  const loadNode = async (node, resolve) => {
    if (node.data.children && node.data.children.length) {
      return resolve(node.data.children);
    } else if (node.data?.type == 'database' && node.data?.isConnect) {
      try {
        const data = await fetchSchemaList(
          node.data.id,
          node.data.id,
          node.data.label,
          node.data.uuid,
          node.data.connectInfo,
        );
        resolve(data);
      } catch (error) {
        return resolve([]);
      }
    } else if (['public', 'person'].includes(node.data?.type)) {
      try {
        const data = await fetchSchemaContentList(
          node.data.id,
          node.data.uuid,
          node.data.databaseId,
          node.data.databaseName,
          node.data.id,
          node.data.label,
          node.data.connectInfo,
        );
        const schemaNode = findNode({
          rootId: node.data.connectInfo.id,
          databaseId: node.data.databaseId,
          schemaId: node.data.id,
        });
        schemaNode.children = data;
        updateConnectListPersist();
        return resolve(data);
      } catch (error) {
        return resolve([]);
      }
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

  const insertDbItem = (rootId, databaseName, connectInfo, isConnect) => {
    const databaseId = `${rootId}_${databaseName}`;
    connectionList.value.forEach((list) => {
      if (list.id == rootId) {
        list.children.push({
          id: databaseId,
          parentId: rootId,
          uuid: '',
          label: databaseName,
          name: databaseName,
          connectInfo: JSON.parse(JSON.stringify(connectInfo)),
          type: 'database',
          isLeaf: false,
          children: [],
          isConnect,
          connectTime: null,
        });
      }
    });
    updateConnectListPersist();
    isConnect &&
      nextTick(() => {
        handleDbConnect(rootId, databaseId, isConnect);
      });
  };
  const deleteDbItem = (rootId, databaseId) => {
    treeRef.value.remove(databaseId);
    for (let i = 0; i < connectionList.value.length; i++) {
      const list = connectionList.value[i];
      if (list.id == rootId) {
        for (let j = 0; j < list.children.length; j++) {
          const db = list.children[j];
          if (db.id == databaseId) {
            list.children.splice(j, 1);
            break;
          }
        }
        break;
      }
    }
    updateConnectListPersist();
    refreshConnectListMap();
  };

  const fetchSchemaList = async (parentId, databaseId, databaseName, uuid, connectInfo) => {
    const data = (await getSchemaList({
      uuid,
      connectionName: connectInfo.name,
      webUser: UserStore.userId,
    })) as unknown as string[];
    const schemaList = [];
    data.forEach((schemaName) => {
      const obj = {
        id: `${parentId}_${schemaName}`,
        parentId,
        label: schemaName,
        name: schemaName,
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
    const dbNode = findNode({ rootId: connectInfo.id, databaseId });
    dbNode.children = schemaList;
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
      const { label, type, childType } = sortType(key);
      const keyId = `${parentId}_${key}`;
      array.push({
        id: keyId,
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
      name: item,
      type: childType,
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
  const openConnectDialog = (type: 'create' | 'edit') => {
    treeContext.rootVisible = false;
    EventBus.notify(EventTypeName.OPEN_CONNECT_DIALOG, {
      dialogType: type,
      connectInfo: currentContextNodeData.connectInfo,
      uuid: type == 'create' ? '' : treeContextFindAvailableUuid.value,
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
  const refreshConnectListMap = () => {
    AppStore.connectListMap = connectionList.value.map((listItem) => {
      const rootId = listItem.connectInfo.id;
      return {
        id: rootId,
        info: { ...listItem.connectInfo },
        connectedDatabase: listItem.children
          .filter((item) => item.isConnect)
          .map((item) => ({
            rootId,
            name: item.label,
            uuid: item.uuid,
            connectTime: item.connectTime,
          })),
      };
    });
  };
  const handleDeleteConnect = () => {
    treeContext.rootVisible = false;
    ElMessageBox.confirm(
      t('message.deleteConnect', { name: currentContextNodeData.connectInfo.name }),
    ).then(async () => {
      for (let i = 0; i < currentContextNodeData.children.length; i++) {
        const db = currentContextNodeData.children[i];
        db.isConnect && (await closeConnections(db.uuid));
      }
      // const index = connectionList.value.findIndex((item) => item.id == currentContextNodeData.id);
      treeRef.value.remove(treeRef.value.getNode(currentContextNodeData.id));
      // index > -1 && connectionList.value.splice(index, 1);
      updateConnectListPersist();
      refreshConnectListMap();
      if (connectionList.value.length == 0) {
        // EventBus.notify(EventTypeName.CLOSE_ALL_TAB);
        router.push('/home');
      }
      AppStore.currentConnectNode =
        connectionList.value.find((item) => item.id == AppStore.lastestConnectDatabase.rootId) ||
        {};
    });
  };
  const handleDisAllconnection = () => {
    treeContext.rootVisible = false;
    ElMessageBox.confirm(
      t('message.disConnect', { name: currentContextNodeData.connectInfo.name }),
    ).then(async () => {
      for (let i = 0; i < connectionList.value.length; i++) {
        const list = connectionList.value[i];
        if (list.id == currentContextNodeData.id) {
          for (let j = 0; j < list.children.length; j++) {
            const db = list.children[j];
            if (db.isConnect) {
              await closeConnections(db.uuid);
              db.children = [];
              db.uuid = '';
              db.isConnect = false;
              db.connectTime = null;
            }
          }
        }
      }
      if (AppStore.currentConnectNode.id == currentContextNodeData.id) {
        AppStore.currentConnectNode = connectionList.value[0] || {};
      }
      updateConnectListPersist();
      refreshConnectListMap();
    });
  };
  const handleDbConnect = async (rootId, databaseId, willOpen) => {
    treeContext.databaseVisible = false;
    const rootData = findNode({ rootId });
    const dbData = findNode({ rootId, databaseId });
    if (!rootData) return;
    let mergeObj = {};
    if (willOpen) {
      const params = {
        id: rootData.connectInfo.id,
        dataName: dbData.name,
        webUser: UserStore.userId,
      };
      const res: any = await openDatabaseConnection(params);
      ElMessage.success(t('message.connectSuccess'));
      mergeObj = { uuid: res.connectionid, connectTime: Date.now() };
    } else {
      await ElMessageBox.confirm(t('message.disConnect', { name: currentContextNodeData.name }));
      await closeConnections(dbData.uuid);
      ElMessage.success(t('message.disconnectSuccess'));
      mergeObj = { uuid: '', connectTime: null };
    }
    Object.assign(dbData, {
      isConnect: willOpen,
      // isLeaf: !willOpen,
      ...mergeObj,
    });
    treeRef.value.updateKeyChildren(dbData.id, dbData.children);
    const node = treeRef.value.getNode(dbData.id);
    if (node) {
      node.loaded = false;
      node.updateLeafState();
      node.collapse();
    }
    updateConnectListPersist();
    refreshConnectListMap();
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
  const createDbSuccess = (data) => {
    showCreateDbDialog.value = false;
    if (createDbType.value == 'create') {
      insertDbItem(
        currentContextNodeData.id,
        data.name,
        currentContextNodeData.connectInfo,
        data.isConnect,
      );
    } else {
      const rootId = currentContextNodeData.connectInfo.id;
      const db = findNode({
        rootId,
        databaseId: currentContextNodeData.id,
        schemaId: '',
      });
      const newId = `${rootId}_${data.name}`;
      Object.assign(db as any, {
        id: newId,
        isConnect: false,
        connectTime: null,
        label: data.name,
        name: data.name,
      });
      treeRef.value.updateKeyChildren(newId, db.children);
      updateConnectListPersist();
      if (data.isConnect) {
        nextTick(() => {
          handleDbConnect(rootId, newId, true);
        });
      }
    }
  };
  const handleEditDb = () => {
    treeContext.databaseVisible = false;
    showCreateDbDialog.value = true;
    createDbType.value = 'edit';
  };
  const hanldeDeleteDb = () => {
    treeContext.databaseVisible = false;
    ElMessageBox.confirm(t('message.deleteDatabase', { name: currentContextNodeData.label })).then(
      async () => {
        await deleteDatabase({
          uuid: treeContextFindAvailableUuid.value,
          databaseName: currentContextNodeData.label,
        });
        ElMessage.success(t('message.deleteSuccess'));
        deleteDbItem(currentContextNodeData.connectInfo.id, currentContextNodeData.id);
      },
    );
  };
  const handleDbInfo = () => {
    treeContext.databaseVisible = false;
    showDbInfoDialog.value = true;
  };
  interface RefreshOptions {
    connectInfo: ConnectInfo | undefined;
    rootId: string;
    uuid: string;
    databaseId: string;
    schemaId: string;
    nodeId: string;
  }
  const refresh = async (
    mode: 'connection' | 'database' | 'mode',
    options: Partial<RefreshOptions> = {
      connectInfo: undefined,
      rootId: '',
      uuid: '',
      databaseId: '',
      schemaId: '',
      nodeId: '',
    },
  ) => {
    let nodeId = '';
    if (mode == 'connection') {
      treeContext.rootVisible = false;
      await fetchDBList(
        options.connectInfo || currentContextNodeData.connectInfo,
        options.uuid || currentContextNodeData.uuid,
      );
      nodeId = options.connectInfo?.id || currentContextNodeData.connectInfo?.id || options.rootId;
    } else if (mode == 'database') {
      treeContext.databaseVisible = false;
      const dbNode = findNode({ rootId: options.rootId, databaseId: options.databaseId });
      dbNode.children = [];
      nodeId = options.databaseId;
    } else if (mode == 'mode') {
      treeContext.modeVisible = false;
      const schemaNode = findNode({
        rootId: options.rootId,
        databaseId: options.databaseId,
        schemaId: options.schemaId,
      });
      schemaNode.children = [];
      nodeId = options.schemaId;
    }
    let node = treeRef.value.getNode(options.nodeId || nodeId);
    if (node) {
      node.loaded = false;
      node.expand();
    }
  };

  const refreshModeByContext = () => {
    refresh('mode', {
      rootId: currentContextNodeData.connectInfo.id,
      databaseId: currentContextNodeData.databaseId,
      schemaId: currentContextNodeData.schemaId,
    });
  };

  const filterNode = (value: string, data: Tree) => {
    if (!value) return true;
    if (!data.label) return false;
    return data.label.includes(value);
  };

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

  const visitedViews = computed(() => {
    return input2.value
      ? TagsViewStore.visitedViews.filter((item) => item.title.includes(input2.value))
      : TagsViewStore.visitedViews;
  });

  const treeContext = reactive({
    MenuStyles: {
      top: '0',
      left: '0',
    },
    rootVisible: false,
    databaseVisible: false,
    modeVisible: false,
    terminalCollectVisible: false,
    functionSPVisible: false,
    viewCollectVisible: false,
    synonymCollectVisible: false,
    sequenceCollectVisible: false,
    viewVisible: false,
    sequenceVisible: false,
    synonymVisible: false,
  });
  const treeContextDbStatus = computed(() => {
    return Boolean(
      AppStore.connectListMap
        .find((listItem) => listItem.id === currentContextNodeData.connectInfo.id)
        ?.connectedDatabase.findIndex((item) => item.name == currentContextNodeData.label) > -1,
    );
  });
  const treeContextFindAvailableUuid = computed(() => {
    return AppStore.connectListMap.find(
      (listItem) => listItem.id === currentContextNodeData.connectInfo.id,
    )?.connectedDatabase[0]?.uuid;
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
    const typeMap = {
      root: 'rootVisible',
      database: 'databaseVisible',
      public: 'modeVisible',
      person: 'modeVisible',
      terminalCollect: 'terminalCollectVisible',
      terminal: 'functionSPVisible',
      viewCollect: 'viewCollectVisible',
      view: 'viewVisible',
      sequence: 'sequenceVisible',
      sequenceCollect: 'sequenceCollectVisible',
      synonymCollect: 'synonymCollectVisible',
      synonym: 'synonymVisible',
    };
    if (Object.prototype.hasOwnProperty.call(typeMap, type)) {
      treeContext[typeMap[type]] = true;
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
        uuid: currentContextNodeData.uuid,
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
        treeContext.functionSPVisible = false;
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
      ElMessage.success(t('message.deleteSuccess'));
      refreshModeByContext();
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
      EventBus.notify(EventTypeName.CLOSE_ALL_TAB_TO_LAST);
      connectData && Object.assign(connectInfo, connectData);
      const { connectionid, ...info } = connectData;
      const isRepeatIndex = AppStore.connectListMap.findIndex((item) => item.id == connectData.id);
      const connectListMapItem = {
        id: connectData.id,
        info,
        connectedDatabase: [
          {
            rootId: connectData.id,
            name: connectData.dataName,
            uuid: connectionid,
            connectTime: Date.now(),
          },
        ],
      };
      isRepeatIndex > -1
        ? AppStore.connectListMap.splice(isRepeatIndex, 1, connectListMapItem)
        : AppStore.connectListMap.unshift(connectListMapItem);
      await fetchDBList(info, connectionid);
      parent.location.reload();
    });
    EventBus.listen(EventTypeName.UPDATE_DATABASE_LIST, (connectData) => {
      connectData && Object.assign(connectInfo, connectData);
      const { connectionid, ...info } = connectData;
      const isRepeatIndex = AppStore.connectListMap.findIndex((item) => item.id == connectData.id);
      const connectListMapItem = {
        id: connectData.id,
        info,
        connectedDatabase: [
          {
            rootId: connectData.id,
            name: connectData.dataName,
            uuid: connectionid,
            connectTime: Date.now(),
          },
        ],
      };
      isRepeatIndex > -1
        ? AppStore.connectListMap.splice(isRepeatIndex, 1, connectListMapItem)
        : AppStore.connectListMap.unshift(connectListMapItem);
      fetchDBList(info, connectionid);
    });
    connectionList.value = getAllConnectList();
  });
  onUnmounted(() => {
    EventBus.unListen(EventTypeName.GET_DATABASE_LIST);
    EventBus.unListen(EventTypeName.UPDATE_DATABASE_LIST);
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
    color: var(--color-bg-2);
    i,
    .icon {
      color: var(--color-bg-2);
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
    .left {
      display: flex;
      align-items: center;
    }
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
      &.home,
      &.home .icon {
        color: var(--el-color-primary);
      }
      &.active {
        background-color: var(--normal-color);
      }
      &.active,
      &.active .icon {
        color: var(--color-bg-2);
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
      display: flex;
      align-items: center;
      white-space: nowrap;
      list-style: none;
      margin: 0;
      cursor: pointer;
      outline: 0;
      &.disabled {
        color: var(--color-text-4);
        cursor: not-allowed;
      }
      &:not(.disabled):hover {
        background-color: var(--color-fill-2);
      }
    }
  }
</style>
