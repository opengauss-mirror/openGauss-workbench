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
          :data="database"
          :props="databaseProps"
          lazy
          :load="loadNode"
          :indent="14"
          highlight-current
          :filter-node-method="filterNode"
          @current-change="handleTreeCurrentChange"
          @node-click="handleNodeClick"
          @node-contextmenu="handleContextmenu"
        >
          <template #default="{ data }">
            <svg-icon
              v-if="['database'].includes(data.type)"
              icon-class="database"
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
            <li @click="openConnectDialog('edit')">{{ $t('connection.edit') }}</li>
            <li @click="openConnectInfo()">{{ $t('connection.props') }}</li>
            <li @click="refresh()">{{ $t('connection.refresh') }}</li>
          </ul>
          <ul
            v-if="treeContext.modeVisible"
            v-click-outside="
              () => {
                treeContext.modeVisible = false;
              }
            "
          >
            <li @click="refreshMode"> {{ $t('connection.refresh') }}</li>
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
  import { dropFunctionSP } from '@/api/functionSP';
  import { dropView } from '@/api/view';
  import { dropSequence } from '@/api/sequence';
  import { dropSynonym } from '@/api/synonym';
  import { useUserStore } from '@/store/modules/user';
  import { connectMenuPersist } from '@/config';

  const AppStore = useAppStore();
  const treeRef = ref<InstanceType<typeof ElTree>>();
  const filterTreeText = ref<string>('');
  const input2 = ref<string>('');
  const { t } = useI18n();

  /* There are the following types of nodeType:
  {
    root = 'root',
    database = 'database',
    public = 'public',
    person = 'person',
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
    children?: Tree[];
  }

  const treeClass = ref('no-tree');
  const viewDialog = ref(false);
  const synonymDialog = ref(false);
  const sequenceDialog = ref(false);
  const UserStore = useUserStore();
  const connectInfo = reactive({
    name: '',
    id: '',
    ip: '',
    dataName: '',
  });
  const databaseProps = {
    value: 'id',
    label: 'label',
    children: 'children',
    class: 'my-class',
    isLeaf: 'isLeaf',
  };
  const database = ref<Array<Tree>>([]);
  const currentContextNodeData = reactive({
    id: '',
    connectInfo: { id: '', name: '' },
    schema_name: '',
    label: '',
  });
  let loading = ref(null);

  onMounted(async () => {
    // maybe update database list, and schema, databaseName... so must close all tabs.
    EventBus.listen(EventTypeName.GET_DATABASE_LIST, async (connectData) => {
      EventBus.notify(EventTypeName.CLOSE_ALL_TAB);
      connectData && Object.assign(connectInfo, connectData);
      await requestDBList();
      /* const time = Date.now();
      router.replace({
        path: '/home',
        query: {
          connectInfoName: AppStore.currentConnectInfo.name,
          time,
        },
      }); */
      parent.location.reload();
    });
    // only update database list. not schema/databaseName...
    EventBus.listen(EventTypeName.UPDATE_DATABASE_LIST, (connectData) => {
      connectData && Object.assign(connectInfo, connectData);
      requestDBList();
    });

    // get stotage already data
    database.value = getAllConnectMenu();
    AppStore.updateCurrentNode(database.value[0] || {});
  });
  onUnmounted(() => {
    EventBus.unListen(EventTypeName.GET_DATABASE_LIST);
    EventBus.unListen(EventTypeName.UPDATE_DATABASE_LIST);
  });

  const loadNode = async (node, resolve) => {
    if (node.data.children && node.data.children.length) {
      return resolve(node.data.children);
    } else if (node.level == 2) {
      const data = await fetchSchemaObjectList(
        node.data.id,
        node.data.label,
        node.data.connectInfo,
      );
      // save value to storage
      database.value.forEach((item) => {
        if (item.id == node.data.connectInfo.id) {
          for (let j = 0; j < item.children.length; j++) {
            if (item.children[j].id == node.data.id) {
              item.children[j].children = data;
              break;
            }
          }
        }
      });
      connectMenuPersist.storage.setItem(connectMenuPersist.key, JSON.stringify(database.value));
      return resolve(data);
    } else {
      return resolve([]);
    }
  };

  const requestDBList = async () => {
    loading.value = loadingInstance();
    try {
      await fetchSchemaList();
    } finally {
      loading.value.close();
    }
    return true;
  };
  // current database's connect list(node level2)
  const fetchSchemaList = async () => {
    const data = (await getSchemaList({
      connectionName: connectInfo.name,
      webUser: UserStore.userId,
    })) as unknown as string[];
    const { id, name, ip, dataName } = connectInfo;
    const connectObj = {
      id,
      label: `${name} (${dataName}:${ip})`,
      type: 'root',
      connectInfo,
      isLeaf: false,
      children: [],
    };
    data.forEach((schema_name) => {
      const obj = {
        id: `${id}_${schema_name}`,
        label: schema_name,
        connectInfo,
        type: schema_name === 'public' ? 'public' : 'person',
        isLeaf: false,
        children: [],
      };
      schema_name === 'public' ? connectObj.children.unshift(obj) : connectObj.children.push(obj);
    });
    database.value = getAllConnectMenu(connectObj);
    AppStore.updateCurrentNode(database.value[0] || {});
    connectMenuPersist.storage.setItem(connectMenuPersist.key, JSON.stringify(database.value));
    return;
  };

  // fetch node level3
  const fetchSchemaObjectList = async (nodeId, schema_name, connectInfo) => {
    const data = (await getSchemaObjectList({
      connectionName: connectInfo.name,
      webUser: UserStore.userId,
      schema: schema_name,
    })) as unknown as any[];
    let arr = [];
    if (data.length) {
      arr = handleSchemaList(data[0], nodeId, schema_name, connectInfo);
    }
    return arr;
  };
  // User Mode - public/no public - [table/func/view]...
  const handleSchemaList = (obj, parent_id, schema_name, connectInfo) => {
    const array = [];
    const order = ['table', 'fun_pro', 'sequence', 'view', 'synonym'];
    let keys = Object.keys(obj);
    keys = keys.sort((a, b) => {
      return order.indexOf(a) - order.indexOf(b);
    });
    keys.forEach((key) => {
      if (!order.includes(key)) return;
      const { label, type, zType } = sortType(key);
      array.push({
        id: `${parent_id}_${key}`,
        label: `${label} (${obj[key].length})`,
        type,
        key,
        connectInfo,
        schema_name,
        children: handleTypeList(obj[key], zType, `${parent_id}_${key}`, schema_name, connectInfo),
        isLeaf: false,
      });
    });
    return array;
  };
  // User Mode-children
  const sortType = (list) => {
    const obj = { label: '', type: '', zType: '' };
    switch (list) {
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
  // User Mode-children-data
  const handleTypeList = (array, zType, parent_id, schema_name, connectInfo) => {
    return array.map((item) => ({
      id: `${parent_id}_${item}`,
      label: item,
      type: zType,
      schema_name,
      connectInfo,
      isLeaf: true,
    }));
  };

  const getAllConnectMenu = (obj?: { id: string | number }) => {
    let connectMenu = JSON.parse(
      connectMenuPersist.storage.getItem(connectMenuPersist.key) || '[]',
    );
    if (connectMenu.length && obj) {
      const isRepeat = connectMenu.map((item) => item.id === obj.id);
      isRepeat.includes(true)
        ? (connectMenu = connectMenu.map((item) => (item.id === obj.id ? obj : item)))
        : connectMenu.push(obj);
    } else if (!connectMenu.length && obj) {
      connectMenu = [obj];
    }
    return connectMenu;
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
  const refresh = () => {
    treeContext.rootVisible = false;
    Object.assign(connectInfo, currentContextNodeData.connectInfo);
    requestDBList();
  };
  const refreshMode = async () => {
    treeContext.modeVisible = false;
    // clear node.children
    database.value.forEach((item) => {
      if (item.id == currentContextNodeData.connectInfo.id) {
        for (let j = 0; j < item.children.length; j++) {
          if (item.children[j].id == currentContextNodeData.id) {
            item.children[j].children = [];
            break;
          }
        }
      }
    });
    let node = treeRef.value.getNode(currentContextNodeData.id);
    if (node) {
      node.loaded = false;
      node.expand();
    }
  };

  watch(database, (val) => {
    treeClass.value = val.length ? 'tree' : 'no-tree';
  });

  // Filter TreeNode
  watch(filterTreeText, (val) => {
    treeRef.value!.filter(val);
    treeClass.value = treeRef.value.isEmpty ? 'no-tree' : 'tree';
    if (!val) resetExpandNodes();
  });
  const filterNode = (value: string, data: Tree) => {
    if (!value) return true;
    if (!data.label) return false;
    return data.label.includes(value);
  };

  const handleTreeCurrentChange = (target) => {
    AppStore.updateCurrentNode(target.data);
  };

  const handleNodeClick = (target, node) => {
    Object.keys(treeContext).forEach((item) => {
      if (item.indexOf('Visible') !== -1) treeContext[item] = false;
    });
    if (node.isLeaf) {
      const connectInfoName = AppStore.currentConnectInfo.name;
      const schema = target.schema_name;
      if (target.type == 'table') {
        router.push({
          path: `/table/${target.id}`,
          query: {
            title: `${target.label}@${connectInfoName}`,
            tableName: target.label,
            connectInfoName,
            schema,
          },
        });
      } else if (target.type == 'terminal') {
        setTimeout(() => {
          const dataName = AppStore.currentConnectInfo.dataName;
          const terminalNum = TagsViewStore.maxTerminalNum + 1;
          const time = Date.now();
          router.push({
            path: `/debug/${encodeURIComponent(target.id)}`,
            query: {
              title: `${target.label}@${connectInfoName}`,
              funcname: target.label,
              dbname: dataName,
              connectInfoName,
              terminalNum,
              schema,
              time,
            },
          });
        }, 300);
      } else if (target.type == 'view') {
        router.push({
          path: `/view/${target.id}`,
          query: {
            title: `${target.label}@${connectInfoName}`,
            viewName: target.label,
            connectInfoName,
            schema,
          },
        });
      } else if (target.type == 'sequence') {
        router.push({
          path: `/sequence/${target.id}`,
          query: {
            title: `${target.label}@${connectInfoName}`,
            sequenceName: target.label,
            connectInfoName,
            schema,
          },
        });
      } else if (target.type == 'synonym') {
        router.push({
          path: `/synonym/${target.id}`,
          query: {
            title: `${target.label}@${connectInfoName}`,
            synonymName: target.label,
            connectInfoName,
            schema,
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
        schema: currentContextNodeData.schema_name,
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
        schema: currentContextNodeData.schema_name,
        [type + 'Name']: currentContextNodeData.label,
        webUser: UserStore.userId,
      };
      await api(params);
      ElMessage.success(`${t(`delete.${type}`)}${t('success')}`);
    });
  };

  const resetExpandNodes = () => {
    database.value.forEach((level1) => {
      database.value.length <= 1
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
        const connectMenu = getAllConnectMenu();
        connectMenu.forEach((list) => {
          if (list.children) {
            list.children.forEach((item) => {
              item.children.forEach((type) => {
                type.label = `${sortType(type.key).label} (${type.children.length})`;
              });
            });
          }
        });
        database.value = connectMenu;
        connectMenuPersist.storage.setItem(connectMenuPersist.key, JSON.stringify(connectMenu));
      }
    },
    { immediate: true, deep: true },
  );
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
        color: #c5c8ce;
        cursor: not-allowed;
      }
      &:not(.disabled):hover {
        background-color: var(--color-secondary-disabled);
      }
    }
  }
</style>
