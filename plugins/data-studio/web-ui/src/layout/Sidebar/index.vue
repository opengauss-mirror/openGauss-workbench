<template>
  <div class="siderbar">
    <Splitpanes class="default-theme" horizontal :dbl-click-splitter="false">
      <Pane>
        <div class="database-wrapper group-wrapper">
          <div class="group-title-contain">
            <div class="left">
              <svg-icon icon-class="database" class-name="icon" /> {{ $t('database.list') }}
            </div>
            <div class="right">
              <el-button type="primary" @click="openConnectDialog('create')" size="small">
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
          <div class="group-main-contain" @scroll="treeContext.menuVisible = false">
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
                  v-if="data.type == 'root'"
                  :icon-class="
                    AppStore.connectedRootInfo?.[data.id] ? 'og-connected' : 'og-disconnect'
                  "
                  class-name="icon"
                />
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
                <svg-icon v-if="data.type == 'package'" icon-class="package" class-name="icon" />
                <svg-icon
                  v-if="data.type == 'tableCollect' || data.type == 'foreignTableCollect'"
                  icon-class="table"
                  class-name="icon"
                />
                <svg v-if="data.type == 'triggerCollect'" class="tree-node-icon" aria-hidden="true">
                  <use xlink:href="#icon-trigger"></use>
                </svg>
                <svg v-if="data.type == 'trigger'" class="tree-node-icon" aria-hidden="true">
                  <use v-if="data.enabled" xlink:href="#icon-trigger-enable"></use>
                  <use v-else xlink:href="#icon-trigger-disable"></use>
                </svg>
                <svg-icon v-if="data.type == 'viewCollect'" icon-class="view" class-name="icon" />
                <svg-icon
                  v-if="data.type == 'synonymCollect'"
                  icon-class="synonym"
                  class-name="icon"
                />
                <svg-icon
                  v-if="data.type == 'sequenceCollect'"
                  icon-class="sequence"
                  class-name="icon"
                />
                <svg v-if="data.type == 'user'" class="tree-node-icon" aria-hidden="true">
                  <use xlink:href="#icon-login"></use>
                </svg>
                <svg v-if="data.type == 'role'" class="tree-node-icon" aria-hidden="true">
                  <use xlink:href="#icon-nologin"></use>
                </svg>
                <svg v-if="data.type == 'tablespace'" class="tree-node-icon" aria-hidden="true">
                  <use xlink:href="#icon-database"></use>
                </svg>
                <span>{{ data.label }}</span>
              </template>
            </el-tree>
            <ContextMenu :offset="treeContext.MenuStyles" v-model:show="treeContext.menuVisible">
              <ul v-if="treeContext.rootVisible">
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
                  @click="
                    treeContextFindAvailableUuid &&
                      refresh('connection', { uuid: treeContextFindAvailableUuid })
                  "
                >
                  {{ $t('connection.refresh') }}
                </li>
              </ul>
              <ul v-if="treeContext.databaseCollectVisible">
                <li
                  :class="{ disabled: !treeContextFindAvailableUuid }"
                  @click="!!treeContextFindAvailableUuid && handleCreateDb()"
                >
                  {{ $t('database.create') }}
                </li>
                <li
                  @click="
                    refresh('databaseCollect', {
                      rootId: currentContextNodeData.connectInfo.id,
                      parentId: currentContextNodeData.id,
                      uuid: treeContextFindAvailableUuid,
                    })
                  "
                >
                  {{ $t('connection.refresh') }}
                </li>
              </ul>
              <ul v-if="treeContext.userRoleCollectVisible">
                <li @click="handleCreateUserRole"> {{ $t('create.userRole') }}</li>
                <li
                  @click="
                    refresh('userRoleCollect', {
                      rootId: currentContextNodeData.connectInfo.id,
                      parentId: currentContextNodeData.id,
                      uuid: treeContextFindAvailableUuid,
                    })
                  "
                >
                  {{ $t('connection.refresh') }}
                </li>
              </ul>
              <ul v-if="treeContext.tablespaceCollectVisible">
                <li @click="handleCreateTablespace"> {{ $t('create.tablespace') }}</li>
                <li
                  @click="
                    refresh('tablespaceCollect', {
                      rootId: currentContextNodeData.connectInfo.id,
                      parentId: currentContextNodeData.id,
                      uuid: treeContextFindAvailableUuid,
                    })
                  "
                >
                  {{ $t('connection.refresh') }}
                </li>
              </ul>
              <ul v-if="treeContext.databaseVisible">
                <li
                  :class="{ disabled: treeContextDbStatus }"
                  @click="
                    !treeContextDbStatus &&
                      handleDbConnect(
                        currentContextNodeData.connectInfo.id,
                        currentContextNodeData.id,
                        currentContextNodeData.uuid,
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
                        currentContextNodeData.uuid,
                        false,
                      )
                  "
                >
                  {{ $t('database.close') }}
                </li>
                <li
                  :class="{ disabled: !treeContextDbStatus }"
                  @click="treeContextDbStatus && handleSetDisconnectionTime()"
                >
                  {{ $t('siderbar.setDisconnectionTime.name') }}
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
                  @click="treeContextDbStatus && handleSchema('create')"
                >
                  {{ $t('create.mode') }}
                </li>
                <li
                  :class="{ disabled: !treeContextDbStatus }"
                  @click="treeContextDbStatus && handleBatchExport('schemaDDL')"
                >
                  {{ $t('export.specific.batchSchemaDdl') }}
                </li>
                <li
                  :class="{ disabled: !treeContextDbStatus }"
                  @click="treeContextDbStatus && handleBatchExport('schemaDDLData')"
                >
                  {{ $t('export.specific.batchSchemaDdlData') }}
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
              <ul v-if="treeContext.modeVisible">
                <li
                  @click="
                    !systemSchema.includes(currentContextNodeData.name) && handleSchema('edit')
                  "
                  :class="{ disabled: systemSchema.includes(currentContextNodeData.name) }"
                >
                  {{ $t('edit.mode') }}
                </li>
                <li
                  @click="
                    !systemSchema.includes(currentContextNodeData.name) && handleSchema('delete')
                  "
                  :class="{ disabled: systemSchema.includes(currentContextNodeData.name) }"
                >
                  {{ $t('delete.mode') }}
                </li>
                <li @click="handleExport('modeDDL')"> {{ $t('export.ddl') }}</li>
                <li @click="handleExport('modeDDLData')"> {{ $t('export.ddlData') }}</li>
                <li
                  @click="
                    refresh('schema', {
                      rootId: currentContextNodeData.connectInfo.id,
                      databaseId: currentContextNodeData.databaseId,
                      schemaId: currentContextNodeData.id,
                    })
                  "
                >
                  {{ $t('connection.refresh') }}
                </li>
              </ul>
              <ul v-if="treeContext.terminalCollectVisible">
                <li @click="hanldeCreate('function')"> {{ $t('create.function') }}</li>
                <li @click="hanldeCreate('procedure')"> {{ $t('create.process') }}</li>
                <li @click="hanldeCreate('sql')"> {{ $t('create.sql') }}</li>
                <li @click="hanldeCreate('anonymous')"> {{ $t('create.anonymous') }}</li>
                <li @click="handleBatchExport('functionDDL')">
                  {{ $t('export.specific.batchFunctionDdl') }}
                </li>
                <li
                  @click="
                    refresh('terminalCollect', {
                      rootId: currentContextNodeData.connectInfo.id,
                      schemaContentCollectId: currentContextNodeData.id,
                    })
                  "
                >
                  {{ $t('connection.refresh') }}
                </li>
              </ul>
              <ul v-if="treeContext.tableCollectVisible">
                <li @click="hanldeCreateTable"> {{ $t('create.table') }}</li>
                <li @click="handleBatchExport('tableDDL')">
                  {{ $t('export.specific.batchTableDdl') }}
                </li>
                <li @click="handleBatchExport('tableDDLData')">
                  {{ $t('export.specific.batchTableDdlData') }}
                </li>
                <li
                  @click="
                    refresh('tableCollect', {
                      rootId: currentContextNodeData.connectInfo.id,
                      schemaContentCollectId: currentContextNodeData.id,
                    })
                  "
                >
                  {{ $t('connection.refresh') }}
                </li>
              </ul>
              <ul v-if="treeContext.foreignTableCollectVisible">
                <li @click="hanldeCreateForeignTable"> {{ $t('create.foreignTable') }}</li>
                <li
                  @click="
                    refresh('foreignTableCollect', {
                      rootId: currentContextNodeData.connectInfo.id,
                      schemaContentCollectId: currentContextNodeData.id,
                    })
                  "
                >
                  {{ $t('connection.refresh') }}
                </li>
              </ul>
              <ul v-if="treeContext.triggerCollectVisible">
                <li @click="hanldeCreateTrigger"> {{ $t('create.trigger') }}</li>
                <li
                  @click="
                    refresh('triggerCollect', {
                      rootId: currentContextNodeData.connectInfo.id,
                      schemaContentCollectId: currentContextNodeData.id,
                    })
                  "
                >
                  {{ $t('connection.refresh') }}
                </li>
              </ul>
              <ul v-if="treeContext.viewCollectVisible">
                <li @click="handleCreateView"> {{ $t('create.view') }}</li>
                <li @click="handleBatchExport('viewDDL')">
                  {{ $t('export.specific.batchViewDdl') }}
                </li>
                <li
                  @click="
                    refresh('viewCollect', {
                      rootId: currentContextNodeData.connectInfo.id,
                      schemaContentCollectId: currentContextNodeData.id,
                    })
                  "
                >
                  {{ $t('connection.refresh') }}
                </li>
              </ul>
              <ul v-if="treeContext.sequenceCollectVisible">
                <li @click="handleCreateSequence"> {{ $t('create.sequence') }}</li>
                <li @click="handleBatchExport('sequenceDDL')">
                  {{ $t('export.specific.batchSequenceDdl') }}
                </li>
                <li @click="handleBatchExport('sequenceDDLData')">
                  {{ $t('export.specific.batchSequenceDdlData') }}
                </li>
                <li
                  @click="
                    refresh('sequenceCollect', {
                      rootId: currentContextNodeData.connectInfo.id,
                      schemaContentCollectId: currentContextNodeData.id,
                    })
                  "
                >
                  {{ $t('connection.refresh') }}
                </li>
              </ul>
              <ul v-if="treeContext.synonymCollectVisible">
                <li @click="handleCreateSynonym"> {{ $t('create.synonym') }}</li>
                <li
                  @click="
                    refresh('synonymCollect', {
                      rootId: currentContextNodeData.connectInfo.id,
                      schemaContentCollectId: currentContextNodeData.id,
                    })
                  "
                >
                  {{ $t('connection.refresh') }}
                </li>
              </ul>
              <ul v-if="treeContext.packageVisible">
                <li @click="handleViewPackage(currentContextNodeData)">
                  {{ $t('siderbar.viewSource') }}
                </li>
                <li @click="deleteConfirm('package')"> {{ $t('delete.package') }}</li>
                <li @click="handleExport('functionDDL')"> {{ $t('export.ddl') }}</li>
              </ul>
              <ul v-if="treeContext.functionSPVisible">
                <li @click="deleteConfirm('functionSP')"> {{ $t('delete.functionSP') }}</li>
                <li @click="handleExport('functionDDL')"> {{ $t('export.ddl') }}</li>
              </ul>
              <ul v-if="treeContext.tableVisible">
                <li @click="handleRelatedSequence">
                  {{ $t('siderbar.table.showRelatedSequence') }}
                </li>
                <li @click="handleAnalyze"> {{ $t('siderbar.table.analyze') }}</li>
                <li @click="handleReindex"> {{ $t('siderbar.table.reindex') }}</li>
                <li @click="handleVacuum"> {{ $t('siderbar.table.vacuum') }}</li>
                <li @click="handleTruncate"> {{ $t('siderbar.table.truncate') }}</li>
                <li @click="handleSetTableSchema"> {{ $t('siderbar.table.setSchema') }}</li>
                <li @click="handleSetTablespace"> {{ $t('siderbar.table.setTablespace') }}</li>
                <li @click="handleUpdateTableDescription">
                  {{ $t('siderbar.table.setDescription') }}
                </li>
                <li @click="handleImport('tableData')"> {{ $t('import.tableData') }}</li>
                <li @click="handleExport('tableDDL')"> {{ $t('export.ddl') }}</li>
                <li @click="handleExport('tableData')"> {{ $t('export.tableData') }}</li>
                <li @click="handleExport('tableDDLData')"> {{ $t('export.ddlData') }}</li>
                <li @click="handleTableRename"> {{ $t('rename.table') }}</li>
                <li @click="handleDropTable"> {{ $t('delete.table') }}</li>
              </ul>
              <ul v-if="treeContext.foreignTableVisible">
                <li @click="deleteConfirm('foreignTable')"> {{ $t('delete.table') }}</li>
              </ul>
              <ul v-if="treeContext.triggerVisible">
                <li @click="handleTriggerRename"> {{ $t('rename.trigger') }}</li>
                <li
                  v-if="!currentContextNodeData.enabled && currentContextNodeData.isTableTrigger"
                  @click="
                    handleTrigger(
                      currentContextNodeData.connectInfo.id,
                      currentContextNodeData.databaseId,
                      currentContextNodeData.schemaId,
                      currentContextNodeData.parentId,
                      true,
                    )
                  "
                >
                  {{ $t('trigger.enableTrigger') }}
                </li>
                <li
                  v-if="currentContextNodeData.enabled && currentContextNodeData.isTableTrigger"
                  @click="
                    handleTrigger(
                      currentContextNodeData.connectInfo.id,
                      currentContextNodeData.databaseId,
                      currentContextNodeData.schemaId,
                      currentContextNodeData.parentId,
                      false,
                    )
                  "
                >
                  {{ $t('trigger.disableTrigger') }}
                </li>
                <li @click="deleteConfirm('trigger')"> {{ $t('delete.trigger') }}</li>
              </ul>
              <ul v-if="treeContext.viewVisible">
                <li @click="handleEdit('view')"> {{ $t('edit.view') }}</li>
                <li @click="deleteConfirm('view')"> {{ $t('delete.view') }}</li>
                <li @click="handleExport('viewDDL')"> {{ $t('export.ddl') }}</li>
              </ul>
              <ul v-if="treeContext.sequenceVisible">
                <li @click="deleteConfirm('sequence')"> {{ $t('delete.sequence') }}</li>
                <li @click="handleExport('sequenceDDL')"> {{ $t('export.ddl') }}</li>
                <li @click="handleExport('sequenceDDLData')"> {{ $t('export.ddlData') }}</li>
              </ul>
              <ul v-if="treeContext.synonymVisible">
                <li @click="deleteConfirm('synonym')"> {{ $t('delete.synonym') }}</li>
              </ul>
              <ul v-if="treeContext.userRoleVisible">
                <li @click="changePassword"> {{ $t('common.changePassword') }}</li>
                <li @click="deleteConfirm('userRole')"> {{ $t('delete.userRole') }}</li>
              </ul>
              <ul v-if="treeContext.tablespaceVisible">
                <li @click="deleteConfirm('tablespace')"> {{ $t('delete.tablespace') }}</li>
              </ul>
            </ContextMenu>
          </div>
        </div>
      </Pane>
    </Splitpanes>
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
    <CreateSchemaDialog
      v-if="showCreateSchemaDialog"
      v-model="showCreateSchemaDialog"
      :uuid="currentContextNodeData.uuid"
      :databaseName="createSchemaData.databaseName"
      :oid="createSchemaData.oid"
      :owner="createSchemaData.owner"
      :type="createSchemaType"
      @success="createSchemaSuccess"
    />
    <CreateViewDialog
      v-if="viewDialog"
      v-model="viewDialog"
      :type="createViewType"
      :nodeData="currentContextNodeData"
      @success="refreshSchemaByContext"
    />
    <ExportTableDataDialog
      v-if="exportTableDialog"
      v-model="exportTableDialog"
      :nodeData="currentContextNodeData"
    />
    <RenameTable
      v-if="renameTableDialog"
      v-model="renameTableDialog"
      :nodeData="currentContextNodeData"
      :type="renameType"
    />
    <UpdateTableDescription
      v-if="updateTableDescriptionDialog"
      v-model="updateTableDescriptionDialog"
      :nodeData="currentContextNodeData"
    />
    <SetTablespace
      v-if="setTablespaceDialog"
      v-model="setTablespaceDialog"
      :nodeData="currentContextNodeData"
    />
    <SetTableSchema
      v-if="setTableSchemaDialog"
      v-model="setTableSchemaDialog"
      :nodeData="currentContextNodeData"
    />
    <ImportTableDataDialog
      v-if="visibleImportDialog"
      v-model="visibleImportDialog"
      :uuid="currentContextNodeData.uuid"
      :schema="currentContextNodeData.schemaName"
      :tableName="currentContextNodeData.name"
      :oid="currentContextNodeData.oid"
    />
    <ChangeUserPasswordDialog
      v-if="
        visibleChangePasswordDialog &&
        (currentContextNodeData.type == 'user' || currentContextNodeData.type == 'role')
      "
      v-model="visibleChangePasswordDialog"
      :uuid="treeContextFindAvailableUuid"
      :userName="currentContextNodeData.label"
      :type="currentContextNodeData.type"
    />
    <SetDisconnectionDialog
      v-if="visibleSetDisconnectionDialog"
      v-model="visibleSetDisconnectionDialog"
      :uuid="treeContextFindAvailableUuid"
    />
    <BatchExportDialog
      v-if="visibleBatchExportDialog"
      v-model="visibleBatchExportDialog"
      :type="batchExportProps.type"
      :uuid="batchExportProps.uuid"
      :database="batchExportProps.database"
      :schema="batchExportProps.schema"
    />
  </div>
</template>

<script lang="ts" setup name="Siderbar">
  import { Search } from '@element-plus/icons-vue';
  import { ElMessage, ElMessageBox, ElTree } from 'element-plus';
  import CreateDbDialog from './components/CreateDbDialog.vue';
  import DbInfoDialog from './components/DbInfoDialog.vue';
  import CreateSchemaDialog from './components/CreateSchemaDialog.vue';
  import CreateViewDialog from '@/views/view/CreateViewDialog.vue';
  import ExportTableDataDialog from './components/ExportTableDataDialog.vue';
  import RenameTable from './components/RenameTable.vue';
  import UpdateTableDescription from './components/UpdateTableDescription.vue';
  import SetTablespace from './components/SetTablespace.vue';
  import SetTableSchema from './components/SetTableSchema.vue';
  import ImportTableDataDialog from '@/components/ImportTableDataDialog.vue';
  import ChangeUserPasswordDialog from './components/ChangeUserPasswordDialog.vue';
  import SetDisconnectionDialog from './components/SetDisconnectionDialog.vue';
  import BatchExportDialog from './components/BatchExportDialog.vue';
  import ContextMenu from '@/components/ContextMenu/index.vue';
  import { useRoute, useRouter } from 'vue-router';
  import { useAppStore, ConnectedDatabase } from '@/store/modules/app';
  import { useUserStore } from '@/store/modules/user';
  import { useTagsViewStore } from '@/store/modules/tagsView';
  import EventBus, { EventTypeName } from '@/utils/event-bus';
  import { loadingInstance, downLoadMyBlobType } from '@/utils';
  import { useI18n } from 'vue-i18n';
  import { closeConnections } from '@/api/connect';
  import { openDatabaseConnection, deleteDatabase } from '@/api/database';
  import { deleteSchema, exportSchemaDdl } from '@/api/schema';
  import { exportTableDdl } from '@/api/table';
  import { dropForeignTableApi } from '@/api/foreignTable';
  import { dropFunctionSP, exportFunctionDdl } from '@/api/functionSP';
  import { dropPackage } from '@/api/functionSP';
  import { dropView, exportViewDdl } from '@/api/view';
  import { dropSequence, exportSequenceDdl } from '@/api/sequence';
  import { dropSynonym } from '@/api/synonym';
  import { dropUser } from '@/api/userRole';
  import { dropTablespaceApi } from '@/api/tablespace';
  import { updateTriggerEnableApi, updateTriggerDisableApi, deleteTriggerApi } from '@/api/trigger';
  import { connectListPersist, hasConnectListPersist } from '@/config';
  import { sidebarForage } from '@/utils/localforage';
  import { useSidebarContext } from '../hooks/useSidebarContext';
  import {
    generateRoot,
    generateUserRoleList,
    generateTablespaceList,
    generateDBList,
    generateSchemaList,
    generateSchemaContentList,
    generateFileList,
    getI18nLocalType,
    getFileType,
    getRootChildCollectLabel,
  } from './getSideData';
  import { Tree, ConnectInfo, RefreshOptions, NodeData, BatchExportType } from './types';
  import { updateConnectInfo } from './sidebarUtils';
  import { findNodeById, findNodesByType } from '@/utils/findNode';

  const AppStore = useAppStore();
  const UserStore = useUserStore();
  const TagsViewStore = useTagsViewStore();
  const router = useRouter();
  const treeRef = ref<InstanceType<typeof ElTree>>();
  const filterTreeText = ref<string>('');
  const { t } = useI18n();

  /* There are the following types of nodeType:
  {
    root: 'root',
    databaseCollect: 'databaseCollect',
    userRoleCollect: 'userRoleCollect',
    database: 'database',
    role: 'role',
    tablespaceCollect: 'tablespaceCollect',
    tablespace: 'tablespace',
    public: 'public', // schema
    person: 'person', // schema
    tableCollect: 'tableCollect',
    table: 'table',
    foreignTableCollect: 'foreignTableCollect',
    foreignTable: 'foreignTable',
    terminalCollect: 'terminalCollect',
    terminal: 'terminal',
    viewCollect: 'viewCollect',
    view: 'view',
    synonymCollect: 'synonymCollect',
    synonym: 'synonym',
    sequenceCollect: 'sequenceCollect',
    sequence: 'sequence',
  } */

  const treeClass = ref('no-tree');
  const showCreateDbDialog = ref(false);
  const showDbInfoDialog = ref(false);
  const showCreateSchemaDialog = ref(false);
  const viewDialog = ref(false);
  const exportTableDialog = ref(false);
  const renameTableDialog = ref(false);
  const updateTableDescriptionDialog = ref(false);
  const setTablespaceDialog = ref(false);
  const setTableSchemaDialog = ref(false);
  const visibleImportDialog = ref(false);
  const visibleChangePasswordDialog = ref(false);
  const visibleSetDisconnectionDialog = ref(false);
  const visibleBatchExportDialog = ref(false);
  const tempConnectInfo: ConnectInfo = reactive({
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
    isLeaf: 'isLeaf',
  };
  const connectionList = ref<Array<Tree>>([]);
  const currentContextNodeData = reactive<NodeData>({
    id: '',
    type: '',
    uuid: '',
    connectInfo: { id: '', name: '' } as ConnectInfo,
    rootId: '',
    parentId: '',
    databaseId: '',
    databaseName: '',
    schemaId: '',
    schemaName: '',
    oid: '',
    label: '',
    name: '',
    children: [],
  });
  let loading = ref(null);
  const createDbType = ref<'create' | 'edit'>('create');
  const createSchemaType = ref<'create' | 'edit'>('create');
  const createViewType = ref<'create' | 'edit'>('create');
  const createSchemaData = reactive({
    databaseName: '',
    databaseId: '',
    oid: '',
    owner: '',
  });

  const systemSchema = ref([
    'blockchain',
    'snapshot',
    'dbe_perf',
    'pkg_service',
    'cstore',
    'pg_toast',
    'pg_catalog',
    'sqladvisor',
    'dbe_pldebugger',
    'dbe_pldeveloper',
    'information_schema',
    'db4ai',
  ]);

  const batchExportProps = reactive({
    type: '' as BatchExportType,
    uuid: '',
    database: '',
    schema: '',
  });

  const renameType = ref<'table' | 'trigger'>('table');

  const findNode = ({
    rootId,
    databaseId,
    schemaId,
    roleId,
    tablespaceId,
  }: {
    rootId: string;
    databaseId?: string;
    schemaId?: string;
    roleId?: string;
    tablespaceId?: string;
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
        if (roleId || tablespaceId) {
          const id = roleId || tablespaceId;
          const type = {
            roleId: 'userRoleCollect',
            tablespaceId: 'tablespaceCollect',
          }[roleId || tablespaceId];
          const collectNode = cListItem.children.find((item) => item.type === type);
          for (let j = 0; j < collectNode.children.length; j++) {
            const roleItem = collectNode.children[j];
            if (roleItem.id == id) return roleItem;
          }
        }
      }
    }
  };
  const findRootChildCollectByType = (
    rootId,
    type: 'databaseCollect' | 'userRoleCollect' | 'tablespaceCollect',
  ) => {
    return connectionList.value
      .find((item) => item.id === rootId)
      ?.children.find((item) => item.type === type);
  };

  const findRootChildCollectById = (rootId, id) => {
    return connectionList.value
      .find((item) => item.id === rootId)
      ?.children.find((item) => item.id === id);
  };

  const findSchemaChildCollectById = (rootId, databaseId, schemaId, id) => {
    return findRootChildCollectByType(rootId, 'databaseCollect')
      ?.children.find((db) => db.id === databaseId)
      ?.children.find((schema) => schema.id === schemaId)
      ?.children.find((childCollect) => childCollect.id === id);
  };

  const loadNode = async (node, resolve) => {
    if (node.data.children && node.data.children.length) {
      return resolve(node.data.children);
    } else if (node.data?.type == 'database' && node.data?.isConnect) {
      try {
        const data = await fetchSchemaList(
          node.data.rootId,
          node.data.id,
          node.data.id,
          node.data.name,
          node.data.uuid,
          node.data.connectInfo,
        );
        resolve(data);
      } catch (error) {
        return resolve([]);
      }
    } else if (['public', 'person'].includes(node.data?.type)) {
      try {
        const data = await generateSchemaContentList(
          node.data.rootId,
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
    } else if (
      [
        'tableCollect',
        'foreignTableCollect',
        'triggerCollect',
        'terminalCollect',
        'sequenceCollect',
        'viewCollect',
        'synonymCollect',
      ].includes(node.data?.type)
    ) {
      try {
        const type = getFileType(node.data?.type);
        const data = await generateFileList(
          type,
          node.data.rootId,
          node.data.id,
          node.data.uuid,
          node.data.databaseId,
          node.data.databaseName,
          node.data.schemaId,
          node.data.schemaName,
          node.data.connectInfo,
        );
        const collectNode = findSchemaChildCollectById(
          node.data.connectInfo.id,
          node.data.databaseId,
          node.data.schemaId,
          node.data.id,
        );
        collectNode.label = getI18nLocalType(getFileType(collectNode.type), undefined, data.length);
        collectNode.children = data as NodeData[];
        updateConnectListPersist();
        // Using setTimeout can reduce lag issues
        setTimeout(() => {
          return resolve(data);
        }, 100);
      } catch (error) {
        return resolve([]);
      }
    } else {
      return resolve([]);
    }
  };

  const fetchRoot = async (connectInfo, uuid) => {
    loading.value = loadingInstance();
    try {
      const root = await generateRoot(connectInfo, uuid);
      connectionList.value = await getAllConnectList(root);
      updateConnectListPersist();
    } finally {
      loading.value.close();
    }
    return true;
  };
  const fetchDBList = async (rootId, parentId, uuid, connectInfo) => {
    const collect = findRootChildCollectById(rootId, parentId);
    const list = await generateDBList(rootId, parentId, uuid, connectInfo);
    collect.children = list;
    collect.label = getRootChildCollectLabel('databaseCollect', list.length);
    updateConnectListPersist();
  };
  const fetchUserRoleList = async (rootId, parentId, uuid, connectInfo) => {
    const collect = findRootChildCollectById(rootId, parentId);
    const list = await generateUserRoleList(rootId, parentId, uuid, connectInfo);
    collect.children = list;
    collect.label = getRootChildCollectLabel('userRoleCollect', list.length);
    updateConnectListPersist();
  };

  const fetchTablespaceList = async (rootId, uuid, connectInfo) => {
    const collect = findRootChildCollectByType(rootId, 'tablespaceCollect');
    if (!collect) return;
    const list = await generateTablespaceList(
      rootId,
      `${rootId}_tablespaceCollect`,
      uuid,
      connectInfo,
    );
    collect.children = list as unknown as NodeData[];
    collect.label = getRootChildCollectLabel('tablespaceCollect', list.length);
    updateConnectListPersist();
  };

  const insertDbItem = (rootId, databaseName, connectInfo, isConnect) => {
    const databaseId = `${rootId}_${databaseName}`;
    connectionList.value.forEach((list) => {
      if (list.id == rootId) {
        const dbList = list.children.find((item) => item.type === 'databaseCollect');
        dbList?.children.push({
          id: databaseId,
          rootId,
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
        handleDbConnect(rootId, databaseId, undefined, isConnect);
      });
  };
  const deleteDbItem = (rootId, databaseId) => {
    treeRef.value.remove(databaseId);
    const dbCollect = findRootChildCollectByType(rootId, 'databaseCollect');
    for (let j = 0; j < dbCollect.children.length; j++) {
      const db = dbCollect.children[j];
      if (db.id == databaseId) {
        dbCollect.children.splice(j, 1);
        break;
      }
    }
    dbCollect.label = getRootChildCollectLabel('databaseCollect', dbCollect.children.length);
    updateConnectListPersist();
    refreshConnectListMap();
  };

  const fetchSchemaList = async (rootId, parentId, databaseId, databaseName, uuid, connectInfo) => {
    const schemaList = await generateSchemaList(
      rootId,
      parentId,
      databaseId,
      databaseName,
      uuid,
      connectInfo,
    );
    const dbNode = findNode({ rootId: connectInfo.id, databaseId });
    dbNode.children = schemaList;
    updateConnectListPersist();
    return schemaList;
  };

  const getAllConnectList = async (connectObj?: { id: string | number }) => {
    let list: any[] = (await sidebarForage.getItem(connectListPersist.key)) || [];
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
  const updateConnectListPersist = async () => {
    await sidebarForage.setItem(
      connectListPersist.key,
      JSON.parse(JSON.stringify(connectionList.value)),
    );
    hasConnectListPersist.storage.setItem(
      hasConnectListPersist.key,
      JSON.stringify(connectionList.value.length > 0),
    );
  };
  const refreshConnectListMap = () => {
    AppStore.connectListMap = connectionList.value.map((listItem) => {
      const rootId = listItem.connectInfo.id;
      const dbList =
        listItem.children.find((item) => item.type === 'databaseCollect')?.children || [];
      let connectedDatabase = dbList
        .filter((item) => item.isConnect)
        .map((item) => ({
          rootId,
          connectInfoName: listItem.connectInfo.name,
          name: item.label,
          uuid: item.uuid,
          isConnect: item.isConnect,
          connectTime: item.connectTime,
        }));
      return {
        id: rootId,
        connectInfo: { ...listItem.connectInfo },
        connectedDatabase,
      };
    });
    AppStore.updateCurrentTerminalInfo();
    AppStore.updateHistoryConnectedDatabase();
  };
  const handleDeleteConnect = () => {
    treeContext.rootVisible = false;
    ElMessageBox.confirm(
      t('message.deleteConnect', { name: currentContextNodeData.connectInfo.name }),
    ).then(async () => {
      const databaseCollect = findRootChildCollectByType(
        currentContextNodeData.id,
        'databaseCollect',
      );
      for (let i = 0; i < databaseCollect.children.length; i++) {
        const db = databaseCollect.children[i];
        if (!import.meta.env.DEV) {
          try {
            db.isConnect && (await closeConnections({ uuid: db.uuid }));
          } catch {
            throw new Error(`fail to close connection: ${db.name}, ${db.uuid}`);
          }
        }
      }
      treeRef.value.remove(treeRef.value.getNode(currentContextNodeData.id));
      ElMessage.success(t('message.deleteSuccess'));
      updateConnectListPersist();
      refreshConnectListMap();
      if (connectionList.value.length == 0) {
        router.push('/home');
      }
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
          const dbCollect = list.children.find((item) => item.type === 'databaseCollect');
          for (let j = 0; j < dbCollect.children.length; j++) {
            const db = dbCollect.children[j];
            if (db.isConnect) {
              await closeConnections({ uuid: db.uuid });
              db.children = [];
              db.isConnect = false;
              db.connectTime = null;
            }
          }
        }
      }
      updateConnectListPersist();
      refreshConnectListMap();
    });
  };
  const handleDbConnect = async (rootId, databaseId, uuid, willOpen) => {
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
        connectionid: uuid,
      };
      const res: any = await openDatabaseConnection(params);
      ElMessage.success(t('message.connectSuccess'));
      mergeObj = { uuid: res.connectionid, connectTime: Date.now() };
    } else {
      await ElMessageBox.confirm(t('message.disConnect', { name: currentContextNodeData.name }));
      await closeConnections({ uuid: dbData.uuid });
      ElMessage.success(t('message.disconnectSuccess'));
      mergeObj = { connectTime: null };
    }
    Object.assign(dbData, {
      isConnect: willOpen,
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
        fileName: title,
        rootId,
        connectInfoName,
        uuid: currentContextNodeData.uuid,
        dbname,
        terminalNum,
        time,
      },
    });
  };

  const handleDbInfo = () => {
    treeContext.databaseVisible = false;
    showDbInfoDialog.value = true;
  };
  const handleSchema = (type: 'create' | 'edit' | 'delete') => {
    treeContext.databaseVisible = false;
    treeContext.modeVisible = false;
    if (type == 'create') {
      createSchemaType.value = 'create';
      Object.assign(createSchemaData, {
        databaseName: currentContextNodeData.label,
        databaseId: currentContextNodeData.id,
        oid: '',
        owner: currentContextNodeData.connectInfo.userName,
      });
      showCreateSchemaDialog.value = true;
    } else if (type == 'edit') {
      createSchemaType.value = 'edit';
      Object.assign(createSchemaData, {
        databaseName: currentContextNodeData.databaseName,
        databaseId: currentContextNodeData.databaseId,
        oid: currentContextNodeData.oid,
        owner: currentContextNodeData.connectInfo.userName,
      });
      showCreateSchemaDialog.value = true;
    } else {
      ElMessageBox.confirm(t('message.deleteMode', { name: currentContextNodeData.label })).then(
        async () => {
          await deleteSchema({
            uuid: currentContextNodeData.uuid,
            schemaName: currentContextNodeData.name,
          });
          ElMessage.success(t('message.deleteSuccess'));
          const rootId = currentContextNodeData.connectInfo.id;
          refresh('database', { rootId, databaseId: currentContextNodeData.databaseId });
        },
      );
    }
  };

  const refresh = async (
    mode:
      | 'connection'
      | 'userRoleCollect'
      | 'tablespaceCollect'
      | 'databaseCollect'
      | 'database'
      | 'schema'
      | 'tableCollect'
      | 'foreignTableCollect'
      | 'triggerCollect'
      | 'terminalCollect'
      | 'sequenceCollect'
      | 'viewCollect'
      | 'synonymCollect',
    options: Partial<RefreshOptions> = {
      connectInfo: undefined,
      rootId: '',
      parentId: '',
      uuid: '',
      databaseId: '',
      databaseName: '',
      schemaId: '',
      schema: '',
      schemaContentCollectId: '',
      nodeId: '',
    },
  ) => {
    let nodeId = '';
    hideTreeContext();
    if (mode == 'connection') {
      await fetchRoot(
        options.connectInfo || currentContextNodeData.connectInfo,
        options.uuid || currentContextNodeData.uuid,
      );
      nodeId = options.connectInfo?.id || currentContextNodeData.connectInfo?.id || options.rootId;
    } else if (mode == 'userRoleCollect') {
      const collectNode = findRootChildCollectByType(options.rootId, 'userRoleCollect');
      await fetchUserRoleList(
        options.rootId,
        options.parentId || collectNode.id,
        options.uuid || currentContextNodeData.uuid,
        options.connectInfo || collectNode.connectInfo,
      );
      nodeId = collectNode.id;
    } else if (mode == 'tablespaceCollect') {
      const collectNode = findRootChildCollectByType(options.rootId, 'tablespaceCollect');
      await fetchTablespaceList(
        options.rootId,
        options.uuid || currentContextNodeData.uuid,
        options.connectInfo || collectNode.connectInfo,
      );
      nodeId = collectNode.id;
    } else if (mode == 'databaseCollect') {
      const collectNode = findRootChildCollectByType(options.rootId, 'databaseCollect');
      await fetchDBList(
        options.rootId,
        options.parentId || collectNode.id,
        options.uuid || currentContextNodeData.uuid,
        options.connectInfo || collectNode.connectInfo,
      );
      nodeId = collectNode.id;
    } else if (mode == 'database') {
      const dbNode = findNode({ rootId: options.rootId, databaseId: options.databaseId });
      dbNode.children = [];
      nodeId = options.databaseId;
    } else if (mode == 'schema') {
      let databaseId = options.databaseId;
      if (!databaseId && options.databaseName) {
        const rootData = findNode({ rootId: options.rootId });
        databaseId =
          options.databaseId ||
          findNodesByType(rootData, 'database')?.find((item) => item.name == options.databaseName)
            ?.id;
      }
      let schemaId = options.schemaId;
      if (!schemaId && options.schema) {
        const dbNode = findNode({ rootId: options.rootId, databaseId });
        schemaId =
          options.schemaId ||
          findNodesByType(dbNode, 'schema')?.find((item) => item.name == options.schema)?.id;
      }
      const schemaNode = findNode({
        rootId: options.rootId,
        databaseId,
        schemaId,
      });
      schemaNode.children = [];
      nodeId = options.schemaId;
    } else if (
      [
        'tableCollect',
        'foreignTableCollect',
        'triggerCollect',
        'terminalCollect',
        'sequenceCollect',
        'viewCollect',
        'synonymCollect',
      ].includes(mode)
    ) {
      const rootData = findNode({ rootId: options.rootId });
      const collectNode = findNodeById(rootData, options.schemaContentCollectId, mode);
      collectNode.children = [];
      nodeId = options.schemaContentCollectId;
    }
    let node = treeRef.value.getNode(options.nodeId || nodeId);
    if (node) {
      node.loaded = false;
      node.expand();
    }
  };

  const refreshSchemaByContext = () => {
    refresh('schema', {
      rootId: currentContextNodeData.connectInfo.id,
      databaseId: currentContextNodeData.databaseId,
      schemaId: currentContextNodeData.schemaId,
    });
  };

  const handleCreateDb = () => {
    treeContext.databaseCollectVisible = false;
    showCreateDbDialog.value = true;
    createDbType.value = 'create';
  };
  const createDbSuccess = (data) => {
    showCreateDbDialog.value = false;
    if (createDbType.value == 'create') {
      insertDbItem(
        currentContextNodeData.connectInfo.id,
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
          handleDbConnect(rootId, newId, undefined, true);
        });
      }
    }
  };

  const handleCreateUserRole = () => {
    const time = Date.now();
    const connectInfo: any = currentContextNodeData.connectInfo;
    router.push({
      path: '/createUserRole/' + time,
      query: {
        title: encodeURIComponent('create_user/role'),
        fileName: encodeURIComponent('create_user/role'),
        rootId: connectInfo.id,
        connectInfoName: connectInfo.name,
        connectInfoId: connectInfo.id,
        uuid: treeContextFindAvailableUuid.value,
      },
    });
    treeContext.userRoleCollectVisible = false;
  };

  const handleCreateSequence = () => {
    const time = Date.now();
    const connectInfo: any = currentContextNodeData.connectInfo;
    router.push({
      path: '/createSequence/' + time,
      query: {
        title: 'create_sequence',
        fileName: 'create_sequence',
        rootId: connectInfo.id,
        connectInfoName: connectInfo.name,
        connectInfoId: connectInfo.id,
        uuid: treeContextFindAvailableUuid.value,
        schema: currentContextNodeData.schemaName,
        schemaContentCollectId: currentContextNodeData.id,
      },
    });
    treeContext.sequenceCollectVisible = false;
  };

  const handleCreateSynonym = () => {
    const time = Date.now();
    const connectInfo: any = currentContextNodeData.connectInfo;
    router.push({
      path: '/createSynonym/' + time,
      query: {
        title: 'create_synonym',
        fileName: 'create_synonym',
        rootId: connectInfo.id,
        connectInfoName: connectInfo.name,
        connectInfoId: connectInfo.id,
        uuid: treeContextFindAvailableUuid.value,
        schema: currentContextNodeData.schemaName,
        schemaContentCollectId: currentContextNodeData.id,
      },
    });
    treeContext.synonymCollectVisible = false;
  };

  const handleCreateView = () => {
    const time = Date.now();
    const connectInfo: any = currentContextNodeData.connectInfo;
    router.push({
      path: '/createView/' + time,
      query: {
        title: 'create_view',
        fileName: 'create_view',
        rootId: connectInfo.id,
        connectInfoName: connectInfo.name,
        connectInfoId: connectInfo.id,
        uuid: treeContextFindAvailableUuid.value,
        schema: currentContextNodeData.schemaName,
        schemaContentCollectId: currentContextNodeData.id,
      },
    });
    treeContext.viewCollectVisible = false;
  };

  const handleCreateTablespace = () => {
    const time = Date.now();
    const connectInfo: any = currentContextNodeData.connectInfo;
    router.push({
      path: '/createTablespace/' + time,
      query: {
        title: 'create_tablespace',
        fileName: 'create_tablespace',
        rootId: connectInfo.id,
        connectInfoName: connectInfo.name,
        connectInfoId: connectInfo.id,
        uuid: treeContextFindAvailableUuid.value,
      },
    });
    treeContext.tablespaceCollectVisible = false;
  };

  const createSchemaSuccess = () => {
    showCreateSchemaDialog.value = false;
    const rootId = currentContextNodeData.connectInfo.id;
    refresh('database', { rootId, databaseId: createSchemaData.databaseId });
  };

  const hanldeCreateTable = () => {
    const time = Date.now();
    const connectInfo: any = currentContextNodeData.connectInfo;
    router.push({
      path: '/createTable/' + time,
      query: {
        title: 'create_table',
        fileName: 'create_table',
        rootId: connectInfo.id,
        connectInfoName: connectInfo.name,
        connectInfoId: connectInfo.id,
        uuid: currentContextNodeData.uuid,
        databaseName: currentContextNodeData.databaseName,
        databaseId: currentContextNodeData.databaseId,
        schema: currentContextNodeData.schemaName,
        schemaId: currentContextNodeData.schemaId,
        time,
      },
    });
    treeContext.tableCollectVisible = false;
  };
  const hanldeCreateForeignTable = () => {
    const time = Date.now();
    const connectInfo: any = currentContextNodeData.connectInfo;
    router.push({
      path: '/createForeignTable/' + time,
      query: {
        title: 'create_foreign_table',
        fileName: 'create_foreign_table',
        rootId: connectInfo.id,
        connectInfoName: connectInfo.name,
        connectInfoId: connectInfo.id,
        uuid: currentContextNodeData.uuid,
        databaseName: currentContextNodeData.databaseName,
        databaseId: currentContextNodeData.databaseId,
        schema: currentContextNodeData.schemaName,
        schemaId: currentContextNodeData.schemaId,
        time,
      },
    });
    treeContext.foreignTableCollectVisible = false;
  };

  const hanldeCreateTrigger = () => {
    const time = Date.now();
    const connectInfo: any = currentContextNodeData.connectInfo;
    router.push({
      path: '/createTrigger/' + time,
      query: {
        title: 'create_trigger',
        fileName: 'create_trigger',
        rootId: connectInfo.id,
        userName: connectInfo.userName,
        databaseId: currentContextNodeData.databaseId,
        schemaId: currentContextNodeData.schemaId,
        uuid: currentContextNodeData.uuid,
        schema: currentContextNodeData.schemaName,
      },
    });
    treeContext.triggerCollectVisible = false;
  };

  const hanldeCreate = (type: 'function' | 'procedure' | 'sql' | 'anonymous') => {
    enum titleMap {
      function = 'create_F',
      procedure = 'create_P',
      sql = 'create_SQL',
      anonymous = 'anonymous_block',
    }
    const time = Date.now();
    const connectInfo: any = currentContextNodeData.connectInfo;
    router.push({
      path: '/createDebug/' + time,
      query: {
        type,
        title: titleMap[type],
        fileName: titleMap[type],
        dbname: connectInfo.dataName,
        databaseId: currentContextNodeData.databaseId,
        connectInfoName: connectInfo.name,
        connectInfoId: connectInfo.id,
        uuid: currentContextNodeData.uuid,
        schema: currentContextNodeData.schemaName,
        schemaId: currentContextNodeData.schemaId,
        time,
      },
    });
    treeContext.terminalCollectVisible = false;
  };

  const handleEditDb = () => {
    treeContext.databaseVisible = false;
    showCreateDbDialog.value = true;
    createDbType.value = 'edit';
  };

  const handleEdit = (type: 'view') => {
    if (type === 'view') {
      createViewType.value = 'edit';
      viewDialog.value = true;
      treeContext.viewVisible = false;
    }
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

  const deleteConfirm = async (
    type:
      | 'userRole'
      | 'tablespace'
      | 'foreignTable'
      | 'trigger'
      | 'functionSP'
      | 'package'
      | 'view'
      | 'synonym'
      | 'sequence',
  ) => {
    const apiMap = {
      userRole: dropUser,
      tablespace: dropTablespaceApi,
      foreignTable: dropForeignTableApi,
      trigger: deleteTriggerApi,
      functionSP: dropFunctionSP,
      package: dropPackage,
      view: dropView,
      sequence: dropSequence,
      synonym: dropSynonym,
    };
    const api = apiMap[type];
    hideTreeContext();
    ElMessageBox.confirm(
      `${t('common.confirm')}${t(`delete.${type}`)}${t('common.colon')}
      ${currentContextNodeData.label}
      ${t('common.if')}`,
    ).then(async () => {
      let params = {};
      let callback;
      if (type === 'userRole') {
        params = {
          uuid: treeContextFindAvailableUuid.value,
          userName: currentContextNodeData.name,
          type: currentContextNodeData.type,
        };
        callback = () =>
          refresh('userRoleCollect', {
            rootId: currentContextNodeData.rootId,
            uuid: treeContextFindAvailableUuid.value,
          });
      }
      if (type === 'tablespace') {
        params = {
          uuid: treeContextFindAvailableUuid.value,
          tablespaceName: currentContextNodeData.name,
        };
        callback = () =>
          refresh('tablespaceCollect', {
            rootId: currentContextNodeData.rootId,
            uuid: treeContextFindAvailableUuid.value,
          });
      }
      if (type === 'foreignTable') {
        params = {
          uuid: currentContextNodeData.uuid,
          schema: currentContextNodeData.schemaName,
          foreignTable: currentContextNodeData.name,
        };
        callback = refreshSchemaByContext;
      }
      if (type === 'trigger') {
        params = {
          uuid: currentContextNodeData.uuid,
          schema: currentContextNodeData.schemaName,
          name: currentContextNodeData.name,
          tableName: currentContextNodeData.tableName,
        };
        callback = refreshSchemaByContext;
      }
      if (['functionSP', 'package', 'view', 'synonym', 'sequence'].includes(type)) {
        params = {
          connectionName: currentContextNodeData.connectInfo.name,
          schema: currentContextNodeData.schemaName,
          [type + 'Name']: currentContextNodeData.label,
          webUser: UserStore.userId,
          uuid: currentContextNodeData.uuid,
          oid: currentContextNodeData.oid,
        };
        callback = refreshSchemaByContext;
      }
      await api(params);
      ElMessage.success(t('message.deleteSuccess'));
      callback && callback();
    });
  };

  const changePassword = () => {
    hideTreeContext();
    visibleChangePasswordDialog.value = true;
  };

  const handleSetDisconnectionTime = () => {
    hideTreeContext();
    visibleSetDisconnectionDialog.value = true;
  };

  const filterNode = (value: string, data: Tree) => {
    if (!value) return true;
    if (!data.label) return false;
    return data.label.includes(value);
  };

  const handleNodeClick = (target, node) => {
    hideTreeContext();
    if (node.isLeaf) {
      const { schemaName, databaseName, uuid, isInPackage, packageName } = target;
      const { name: connectInfoName, id: rootId, type: platform, userName } = target.connectInfo;
      const packagePreText = isInPackage ? `${packageName}.` : '';
      const commonParams = {
        platform,
        title: `${schemaName ? schemaName + '.' : ''}${packagePreText}${target.label}${
          databaseName ? '-' + databaseName : ''
        }@${connectInfoName}`,
        rootId,
        connectInfoName,
        uuid,
        dbname: databaseName,
        schema: schemaName,
        fileName:
          packagePreText +
          target.label.slice(
            0,
            target.label.indexOf('(') > -1 ? target.label.indexOf('(') : target.label.length,
          ),
        oid: target.oid,
      };
      const availableUuid = AppStore.getConnectionOneAvailableUuid(rootId);
      if (!availableUuid) return ElMessage.error(t('message.noConnectionAvailable'));
      if (target.type == 'tablespace') {
        router.push({
          path: `/editTablespace/${target.id}`,
          query: {
            ...commonParams,
            name: target.label,
            uuid: availableUuid,
          },
        });
      } else if (target.type == 'table') {
        router.push({
          path: `/table/${target.id}`,
          query: {
            ...commonParams,
            parttype: target.parttype,
            time: String(Date.now()),
          },
        });
      } else if (target.type == 'foreignTable') {
        router.push({
          path: `/foreignTable/${target.id}`,
          query: {
            ...commonParams,
            parttype: target.parttype,
            time: String(Date.now()),
          },
        });
      } else if (target.type == 'trigger') {
        router.push({
          path: `/trigger/${target.id}`,
          query: {
            ...commonParams,
            name: target.label,
            tableName: target.tableName,
            userName,
          },
        });
      } else if (target.type == 'terminal') {
        setTimeout(() => {
          const terminalNum = TagsViewStore.maxTerminalNum + 1;
          const time = String(Date.now());
          router.push({
            path: `/debug/${encodeURIComponent(target.id)}`,
            query: {
              terminalNum,
              ...commonParams,
              databaseId: target.databaseId,
              schemaId: target.schemaId,
              isInPackage: isInPackage ? 'y' : 'n',
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
      } else if (target.type == 'user' || target.type == 'role') {
        router.push({
          path: `/editUserRole/${target.id}`,
          query: {
            name: target.label,
            type: target.type,
            ...commonParams,
            uuid: availableUuid,
          },
        });
      } else if (target.type == 'job') {
        router.push({
          path: `/job/${rootId}`,
          query: {
            name: target.label,
            ...commonParams,
            title: `scheduled_task@${connectInfoName}`,
            fileName: `scheduled_task`,
            uuid: availableUuid,
          },
        });
      }
    }
  };

  const handleViewPackage = (nodeData) => {
    hideTreeContext();
    const terminalNum = TagsViewStore.maxTerminalNum + 1;
    const time = String(Date.now());
    const connectInfoName = nodeData.connectInfo.name;
    const { label, oid, schemaName, schemaId, databaseName, databaseId, uuid } = nodeData;
    router.push({
      path: `/debug/${encodeURIComponent(nodeData.id)}`,
      query: {
        terminalNum,
        title: `${schemaName}.${label}-${databaseName}@${connectInfoName}`,
        rootId: nodeData.rootId,
        connectInfoName,
        uuid,
        dbname: databaseName,
        databaseId,
        schema: schemaName,
        schemaId,
        fileName: label.slice(0, label.indexOf('(') > -1 ? label.indexOf('(') : label.length),
        oid,
        isPackage: 'y',
        time,
      },
    });
  };

  const treeContext = reactive({
    menuVisible: false,
    MenuStyles: {
      top: 0,
      left: 0,
    },
    rootVisible: false,
    userRoleCollectVisible: false,
    tablespaceCollectVisible: false,
    databaseCollectVisible: false,
    databaseVisible: false,
    modeVisible: false,
    tableCollectVisible: false,
    foreignTableCollectVisible: false,
    triggerCollectVisible: false,
    terminalCollectVisible: false,
    packageVisible: false,
    functionSPVisible: false,
    viewCollectVisible: false,
    synonymCollectVisible: false,
    sequenceCollectVisible: false,
    tableVisible: false,
    viewVisible: false,
    foreignTableVisible: false,
    triggerVisible: false,
    sequenceVisible: false,
    synonymVisible: false,
    userRoleVisible: false,
    tablespaceVisible: false,
  });
  const treeContextDbStatus = computed(() => {
    return !!AppStore.connectedDatabase.find(
      (item) =>
        currentContextNodeData.connectInfo.id === item.rootId &&
        currentContextNodeData.label == item.name,
    );
  });
  const treeContextFindAvailableUuid = computed(() => {
    const selectConnection = AppStore.connectListMap.find(
      (listItem) => listItem.id === currentContextNodeData.connectInfo.id,
    );
    return selectConnection?.connectedDatabase.filter((item) => item.isConnect)[0]?.uuid;
  });
  const hideTreeContext = () => {
    treeContext.menuVisible = false;
    Object.keys(treeContext).forEach((key) => {
      if (key.indexOf('Visible') !== -1 && treeContext[key] === true) treeContext[key] = false;
    });
  };
  const handleContextmenu = (event, data) => {
    const type = data.type;
    Object.assign(currentContextNodeData, data);
    treeContext.MenuStyles = {
      top: event.y + 2,
      left: event.x + 2,
    };
    hideTreeContext();
    const typeMap = {
      root: 'rootVisible',
      userRoleCollect: 'userRoleCollectVisible',
      tablespaceCollect: 'tablespaceCollectVisible',
      databaseCollect: 'databaseCollectVisible',
      database: 'databaseVisible',
      public: 'modeVisible',
      person: 'modeVisible',
      tableCollect: 'tableCollectVisible',
      table: 'tableVisible',
      foreignTableCollect: 'foreignTableCollectVisible',
      foreignTable: 'foreignTableVisible',
      triggerCollect: 'triggerCollectVisible',
      trigger: 'triggerVisible',
      terminalCollect: 'terminalCollectVisible',
      package: 'packageVisible',
      terminal: 'functionSPVisible',
      viewCollect: 'viewCollectVisible',
      view: 'viewVisible',
      sequence: 'sequenceVisible',
      sequenceCollect: 'sequenceCollectVisible',
      synonymCollect: 'synonymCollectVisible',
      synonym: 'synonymVisible',
      user: 'userRoleVisible',
      role: 'userRoleVisible',
      tablespace: 'tablespaceVisible',
    };
    if (Object.prototype.hasOwnProperty.call(typeMap, type) && !data.isInPackage) {
      treeContext[typeMap[type]] = true;
      treeContext.menuVisible = true;
    }
  };

  const handleTrigger = async (rootId, databaseId, schemaId, parentId, isWillEnabled) => {
    treeContext.triggerVisible = false;
    const triggerCollect = findSchemaChildCollectById(rootId, databaseId, schemaId, parentId);
    if (!triggerCollect) return;
    const trigger = triggerCollect.children.find(
      (trigger) => trigger.id === currentContextNodeData.id,
    );
    const msgPrefix = isWillEnabled
      ? t('message.willEnable', { name: currentContextNodeData.name })
      : t('message.willDisable', { name: currentContextNodeData.name });
    await ElMessageBox.confirm(msgPrefix);
    let api = isWillEnabled ? updateTriggerEnableApi : updateTriggerDisableApi;
    const params = {
      uuid: currentContextNodeData.uuid,
      schema: trigger.schemaName,
      name: trigger.name,
      tableName: trigger.tableName,
    };
    await api(params);
    ElMessage.success(t('message.setSuccess'));
    Object.assign(trigger, {
      enabled: isWillEnabled,
    });
    updateConnectListPersist();
  };

  const handleImport = (type: 'tableData') => {
    hideTreeContext();
    if (type == 'tableData') {
      visibleImportDialog.value = true;
    }
  };

  const handleExport = async (
    type:
      | 'modeDDL'
      | 'modeDDLData'
      | 'tableDDL'
      | 'tableData'
      | 'tableDDLData'
      | 'functionDDL'
      | 'viewDDL'
      | 'sequenceDDL'
      | 'sequenceDDLData',
  ) => {
    hideTreeContext();
    const { uuid, schemaName: schema, oid, name, type: fileType } = currentContextNodeData;
    if (type == 'tableData') {
      exportTableDialog.value = true;
    } else {
      const obj = {
        modeDDL: {
          api: exportSchemaDdl,
          params: {
            uuid,
            schemaList: [name],
          },
        },
        modeDDLData: {
          api: exportSchemaDdl,
          params: {
            uuid,
            dataFlag: true,
            schemaList: [name],
          },
        },
        tableDDL: {
          api: exportTableDdl,
          params: {
            uuid,
            schema,
            dataFlag: false,
            tableList: [name],
          },
        },
        tableDDLData: {
          api: exportTableDdl,
          params: {
            uuid,
            schema,
            dataFlag: true,
            tableList: [name],
          },
        },
        functionDDL: {
          api: exportFunctionDdl,
          params: {
            uuid,
            schema,
            functionMap: [
              {
                oid,
                name,
                isPackage: fileType === 'package',
              },
            ],
          },
        },
        viewDDL: {
          api: exportViewDdl,
          params: {
            uuid,
            schema,
            viewList: [name],
          },
        },
        sequenceDDL: {
          api: exportSequenceDdl,
          params: {
            uuid,
            schema,
            sequenceList: [name],
          },
        },
        sequenceDDLData: {
          api: exportSequenceDdl,
          params: {
            uuid,
            schema,
            dataFlag: true,
            sequenceList: [name],
          },
        },
      };
      const { api, params } = obj[type];
      loading.value = loadingInstance();
      try {
        const res = await api(params);
        downLoadMyBlobType(res.name, res.data);
      } finally {
        loading.value.close();
      }
    }
  };

  const handleBatchExport = (type: BatchExportType) => {
    hideTreeContext();
    visibleBatchExportDialog.value = true;
    Object.assign(batchExportProps, {
      type,
      uuid: currentContextNodeData.uuid,
      database: ['schemaDDL', 'schemaDDLData'].includes(type)
        ? currentContextNodeData.name
        : currentContextNodeData.databaseName,
      schema: currentContextNodeData.schemaName,
    });
  };

  const handleTableRename = () => {
    treeContext.tableVisible = false;
    renameType.value = 'table';
    renameTableDialog.value = true;
  };
  const handleTriggerRename = () => {
    treeContext.triggerVisible = false;
    renameType.value = 'trigger';
    renameTableDialog.value = true;
  };
  const handleUpdateTableDescription = () => {
    treeContext.tableVisible = false;
    updateTableDescriptionDialog.value = true;
  };
  const handleSetTablespace = () => {
    treeContext.tableVisible = false;
    setTablespaceDialog.value = true;
  };
  const handleSetTableSchema = () => {
    treeContext.tableVisible = false;
    setTableSchemaDialog.value = true;
  };

  const {
    handleAnalyze,
    handleReindex,
    handleTruncate,
    handleVacuum,
    handleDropTable,
    handleRelatedSequence,
  } = useSidebarContext({
    currentContextNodeData,
    hideTreeContext,
  });

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
    async (newVal, oldVal) => {
      if (oldVal) {
        const list = await getAllConnectList();
        list.forEach((listItem) => {
          if (listItem.children) {
            listItem.children.forEach((childCollect) => {
              childCollect.label = getRootChildCollectLabel(
                childCollect.type,
                childCollect.children?.length || 0,
              );
              if (childCollect.type == 'databaseCollect' && childCollect.children?.length) {
                childCollect.children.forEach((dbItem) => {
                  dbItem.children.forEach((schemaItem) => {
                    schemaItem.children.forEach((type) => {
                      type.label = getI18nLocalType(type.key, type.label);
                    });
                  });
                });
              }
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
    // maybe update  database list, and schema, databaseName... so must close all tabs.
    EventBus.listen(EventTypeName.GET_CONNECTION_LIST, async (connectData) => {
      connectData && Object.assign(tempConnectInfo, connectData);
      const { connectionid, ...connectInfo } = connectData;
      const isRepeatIndex = AppStore.connectListMap.findIndex((item) => item.id == connectData.id);
      const connectListMapItem = {
        id: connectData.id,
        connectInfo,
        connectedDatabase: [
          {
            rootId: connectData.id,
            connectInfoName: connectInfo.name,
            name: connectData.dataName,
            uuid: connectionid,
            isConnect: true,
            connectTime: Date.now(),
          },
        ],
      };
      isRepeatIndex > -1
        ? AppStore.connectListMap.splice(isRepeatIndex, 1, connectListMapItem)
        : AppStore.connectListMap.unshift(connectListMapItem);
      await fetchRoot(connectInfo, connectionid);
      refreshConnectListMap();
      if (!TagsViewStore.visitedViews.find((item) => item.name == 'home' && item.query?.rootId)) {
        const connectInfoName = AppStore.connectListMap[0].connectInfo.name;
        const { uuid, name: dbname } = (
          AppStore.connectListMap[0]?.connectedDatabase as ConnectedDatabase[]
        ).filter((item) => item.isConnect)[0];
        router.replace({
          path: '/home',
          query: {
            rootId: AppStore.connectListMap[0].id,
            connectInfoName,
            uuid,
            dbname,
            time: Date.now(),
          },
        });
      }
    });
    EventBus.listen(EventTypeName.UPDATE_CONNECTION_LIST, async (connectData) => {
      connectData && Object.assign(tempConnectInfo, connectData);
      const { connectionid, ...connectInfo } = connectData;
      const isRepeatIndex = AppStore.connectListMap.findIndex((item) => item.id == connectData.id);
      const connectListMapItem = {
        id: connectData.id,
        connectInfo,
        connectedDatabase: [
          {
            rootId: connectData.id,
            connectInfoName: connectInfo.name,
            name: connectData.dataName,
            uuid: connectionid,
            isConnect: true,
            connectTime: Date.now(),
          },
        ],
      };
      isRepeatIndex > -1
        ? AppStore.connectListMap.splice(isRepeatIndex, 1, connectListMapItem)
        : AppStore.connectListMap.unshift(connectListMapItem);
      await fetchRoot(connectInfo, connectionid);
      refreshConnectListMap();
    });
    EventBus.listen(EventTypeName.REFRESH_ASIDER, async (mode, options) => {
      refresh(mode, options);
    });
    EventBus.listen(EventTypeName.UPDATE_CONNECTINFO, async (newConnectInfo) => {
      updateConnectInfo(connectionList.value, newConnectInfo);
      refreshConnectListMap();
    });
    connectionList.value = await getAllConnectList();
    refreshConnectListMap(); // If both connectListMap and currentTerminalInfo have persistence processing, then this line of code may not require
  });
  onUnmounted(() => {
    EventBus.unListen(EventTypeName.GET_CONNECTION_LIST);
    EventBus.unListen(EventTypeName.UPDATE_CONNECTION_LIST);
    EventBus.unListen(EventTypeName.REFRESH_ASIDER);
    EventBus.unListen(EventTypeName.UPDATE_CONNECTINFO);
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
  .el-tree-node__label:hover {
    background-color: var(--el-tree-node-hover-bg-color);
  }
  .el-tree--highlight-current
    .el-tree-node.is-current
    > .el-tree-node__content
    .el-tree-node__label,
  .el-tree--highlight-current .el-tree-node.is-current > .el-tree-node__content {
    background-color: transparent !important;
    color: var(--el-color-primary);
    font-weight: bold;
    i,
    .icon {
      color: var(--el-color-primary);
    }
  }
</style>
<style lang="scss" scoped>
  @import './sidebar.scss';
</style>
