<!-- this component line number start with 1. -->
<template>
  <div class="terminal" :id="id">
    <Splitpanes class="default-theme" horizontal :dbl-click-splitter="false">
      <Pane>
        <div class="editor">
          <FunctionBar
            :type="sqlData.barType"
            :status="sqlData.barStatus"
            :editorReadOnly="sqlData.readOnly"
            :isGlobalEnable="isGlobalEnable"
            @compile="handleCompile"
            @execute="handleExecute(false)"
            @stopRun="handleStop"
            @clear="handleClear"
            @startDebug="handleStartDebug"
            @stopDebug="handleStopDebug"
            @continueStep="handleContinueStep"
            @singleStep="handleSingleStep"
            @stepIn="handleStepIn"
            @stepOut="handleStepOut(false)"
            @format="handleFormat"
            @coverageRate="showCoverageRateDialog = true"
            @importFile="handleImportFile"
            @exportFile="handleExportFile"
          />
          <div class="monaco-wrapper" ref="monacoWrapper">
            <Splitpanes class="default-theme" :dbl-click-splitter="false">
              <Pane>
                <AceEditor
                  ref="editorRef"
                  :value="sqlData.sqlText"
                  :height="monacoHeight"
                  :readOnly="!isGlobalEnable || sqlData.readOnly || ws.isInPackage"
                  :openDebug="['debug', 'debugChild'].includes(props.editorType)"
                  @addBreakPoint="(line) => handleBreakPoint(line, 'addBreakPoint')"
                  @removeBreakPoint="(line) => handleBreakPoint(line, 'deleteBreakPoint')"
                  @enableBreakPoint="(line) => handleBreakPoint(line, 'enableBreakPoint')"
                  @disableBreakPoint="(line) => handleBreakPoint(line, 'disableBreakPoint')"
                  style="width: 100%; margin-top: 4px; border: 1px solid #ddd"
                />
              </Pane>
              <Pane v-if="debug.isDebugging" min-size="30" max-size="70" size="30">
                <DebugPane
                  :stackList="debug.stackList"
                  :breakPointList="debug.breakPointList"
                  :variableList="debug.variableList"
                  :variableHighLight="debug.variableHighLight"
                  @changeBreakPoint="changeDebugPanePoint"
                />
              </Pane>
            </Splitpanes>
          </div>
        </div>
      </Pane>
      <Pane min-size="20" max-size="70" size="35" v-if="showResult">
        <div class="result">
          <ResultTabs
            :msgData="msgData"
            v-model:tabList="tabList"
            v-model:tabValue="tabValue"
            :type="props.editorType == 'sql' ? 'sql' : 'debug'"
          />
        </div>
      </Pane>
    </Splitpanes>
    <EnterParamsDialog
      v-model="enterParams.showParams"
      :data="enterParams.data"
      @confirm="paramsConfirm"
      @cancel="paramsCancel"
    />
    <CoverageRateDialog
      v-model="showCoverageRateDialog"
      :connectionName="ws.connectionName"
      :uuid="ws.uuid"
      :oid="ws.oid"
      :fileName="ws.fileName"
    />
    <ImportFileTipsDialog
      v-model="showImportFileTipsDialog"
      @operation="handleImportFileComfirm"
      @close="handleImportFileClose"
    />
  </div>
</template>

<script lang="ts" setup name="TerminalEditor">
  import FunctionBar from '@/components/FunctionBar.vue';
  import AceEditor from '@/components/AceEditor.vue';
  import DebugPane from './DebugPane.vue';
  import ResultTabs from './ResultTabs.vue';
  import EnterParamsDialog from './EnterParamsDialog.vue';
  import CoverageRateDialog from './CoverageRateDialog.vue';
  import ImportFileTipsDialog from './ImportFileTipsDialog.vue';
  import { ElMessage, ElMessageBox } from 'element-plus';
  import createTemplate from './createTemplate';
  import { useRoute, useRouter } from 'vue-router';
  import WebSocketClass from '@/utils/websocket';
  import {
    dateFormat,
    formatTableV2Data,
    formatTableData,
    loadingInstance,
    uuid,
    downLoadMyBlobType,
  } from '@/utils';
  import { useAppStore } from '@/store/modules/app';
  import { useUserStore } from '@/store/modules/user';
  import { useTagsViewStore } from '@/store/modules/tagsView';
  import { useI18n } from 'vue-i18n';
  import { changeRunningTagStatus } from '@/hooks/tagRunning';

  const route = useRoute();
  const router = useRouter();
  const AppStore = useAppStore();
  const UserStore = useUserStore();
  const TagsViewStore = useTagsViewStore();
  const { t } = useI18n();
  const props = withDefaults(
    defineProps<{
      editorType: 'sql' | 'debug' | 'debugChild';
      initValue?: string;
    }>(),
    {
      editorType: 'sql',
      initValue: '',
    },
  );
  const barStatusMap = {
    sql: {
      execute: true,
      stopRun: false,
      clear: true,
    },
    debug: {
      compile: true,
      execute: false,
      startDebug: false,
      stopDebug: false,
      continueStep: false,
      singleStep: false,
      stepIn: false,
      stepOut: false,
      coverageRate: false,
    },
    debugChild: {
      continueStep: true,
      singleStep: true,
      stepIn: true,
      stepOut: true,
    },
  };
  const sqlData = reactive<{
    sqlText: string;
    readOnly: boolean;
    barStatus: Record<string, boolean>;
    barType: 'sql' | 'debug' | 'debugChild';
  }>({
    sqlText: props.initValue,
    readOnly: false,
    barStatus: barStatusMap[props.editorType],
    barType: props.editorType,
  });

  const showResult = ref(false);
  const id = 'terminal_' + uuid();
  const monacoWrapper = ref<HTMLElement>();
  const monacoHeight = ref('calc(100% - 4px)');
  const editorRef = ref();
  const ws = reactive({
    name: '',
    webUser: '',
    rootId: '',
    parentTagId: '',
    connectionName: '',
    uuid: '',
    sessionId: '',
    dbname: '',
    schema: '',
    fileName: '',
    oid: '',
    instance: null,
    parentWindowName: '',
    rootWindowName: '',
    isPackage: false,
    isInPackage: false,
  });
  const tagId = TagsViewStore.getViewByRoute(route)?.id;
  const commonWsParams = computed(() => {
    return {
      rootWindowName: ws.rootWindowName,
      windowName: ws.sessionId,
      uuid: ws.uuid,
      oid: ws.oid,
      isInPackage: ws.isInPackage,
    };
  });
  const isGlobalEnable = computed(() => {
    return AppStore.connectedDatabase.findIndex((item) => item.uuid == ws.uuid) > -1;
  });
  const loading = ref(null);
  const refreshCounter = reactive({
    counter: null,
    times: 0,
  });
  const isSaving = ref(false);
  const saveFile = reactive({
    name: '',
    oid: '',
  });
  const alreadyCloseWindow = ref(false);
  const showCoverageRateDialog = ref(false);
  const showImportFileTipsDialog = ref(false);
  let importFileData = '';
  const preInputParams = ref([]);

  watch(
    () => AppStore.language,
    (value) => {
      changeServerLanguage(value);
    },
  );

  interface messageType {
    id: number;
    label: string;
    text: string;
  }
  const msgData = ref<Array<messageType>>([]);

  interface TabType {
    id: number;
    name: string; //tag's name
    columns?: any[];
    data: any[];
  }
  const tabList = ref<Array<TabType>>([]);
  const tabValue = ref('home');

  // enterparams's dialog
  const enterParams = reactive({
    showParams: false,
    data: [],
  });
  const paramsConfirm = () => {
    preInputParams.value = enterParams.data.map((item) => ({ [item.type]: item.value }));
    ws.instance.send({
      operation: 'inputParam',
      ...commonWsParams.value,
      sql: editorRef.value.getValue(),
      inputParams: preInputParams.value,
      breakPoints: editorRef.value.getAllLineDecorations(),
    });
    getButtonStatus();
  };
  const paramsCancel = () => {
    handleStopDebug(true);
  };

  const debug = reactive({
    isDebugging: false,
    stackList: [],
    breakPointList: [],
    variableList: [],
    variableHighLight: undefined,
  });
  const changeDebugPanePoint = (value) => {
    if (value.line.length == 0) return;
    value.line.forEach((line) => {
      editorRef.value.addBreakPoint(line - 1, value.type ? 2 : 1);
      handleBreakPoint(line - 1, value.type ? 'enableBreakPoint' : 'disableBreakPoint');
    });
  };

  // watch 'stackList'，get CurrentBreakPoint
  watch(
    () => debug.stackList,
    (list) => {
      if (list.length) {
        setTimeout(() => {
          editorRef.value.setCurrentBreakPoint(parseInt(list[0].lineno) - 1);
        }, 500);
      }
    },
  );
  watch(
    () => debug.isDebugging,
    (value) => {
      if (value) {
        AppStore.isOpenSqlAssistant = false;
      } else {
        Object.assign(debug, {
          stackList: [],
          breakPointList: [],
          variableList: [],
        });
        editorRef.value.removeCurrentBreakPoint();
      }
    },
  );

  const changeSqlBarStatus = (type: 'init' | 'running') => {
    Object.assign(sqlData.barStatus, {
      execute: type === 'init',
      stopRun: type !== 'init',
      clear: type === 'init',
    });
  };

  interface Message {
    code: string;
    data?: any;
    msg: string;
    type: string;
  }
  const onWebSocketMessage = (data: string) => {
    if (!isGlobalEnable.value) return loading.value?.close();
    let res: Message = JSON.parse(data);
    if (res.code == '200') {
      if (['debug', 'debugChild'].includes(props.editorType) && res.type != 'OPERATE_STATUS') {
        getButtonStatus();
      }
      const result = res.data?.result;
      if (res.type == 'TEXT') {
        if (
          props.editorType == 'sql' &&
          ['End of execution', 'Close successfully'].includes(res.msg)
        ) {
          getButtonStatus();
        }
        showResult.value = true;
        const d = new Date();
        (result || res.msg) &&
          msgData.value.push({
            id: d.getTime(),
            label: `[${dateFormat(d, 'yyyy-MM-dd hh:mm:ss.S')}]`,
            text: `[SUCCESS] ${result || res.msg}`,
          });
      }
      if (res.type == 'BUTTON') {
        getButtonStatus();
      }
      if (res.type == 'OPERATE_STATUS') {
        if (props.editorType == 'sql') {
          sqlData.barStatus = {
            execute: result.startRun,
            stopRun: result.stopRun,
            clear: result.startRun,
          };
          sqlData.readOnly = !result?.startRun;
        } else {
          sqlData.barStatus = result;
          sqlData.readOnly = result?.singleStep || result?.stopDebug;
          changeRunningTagStatus(tagId, result?.singleStep);
        }
        // use 'singleStep' button = not isDebugging
        if (props.editorType == 'debug') {
          debug.isDebugging = result.singleStep;
        }
      }
      if (res.type == 'MESSAGE') {
        res.msg == 'success' && ElMessage.success(result);
      }
      if (res.type == 'CONFIRM') {
        ElMessageBox.confirm(result).then(() => {
          handleExecute(true);
        });
      }
      if (res.type == 'CREATE_COVERAGE_RATE') {
        ElMessageBox.confirm(result)
          .then(() => {
            handleCreateCoverageRate(true);
          })
          .catch(() => {
            handleCreateCoverageRate(false);
          });
      }
      if (res.type == 'PARAM_WINDOW') {
        const arr = result.map((item: { key: string | null; type: string }) => {
          return {
            name: item.key,
            type: item.type,
            value: null,
          };
        });
        enterParams.data = arr;
        enterParams.showParams = true;
      }
      if (res.type == 'TABLE') {
        const { columns, data } = formatTableV2Data(res.data.column, result, { showIndex: true });
        const name = `${t('resultTab.result')}${tabList.value.length + 1}`;
        tabList.value.push({
          id: Date.now(),
          name,
          columns,
          data,
        });
        tabValue.value = name;
        showResult.value = true;
      }
      if (res.type == 'BREAKPOINT') {
        debug.breakPointList = formatTableData(res.data.column, result);
      }
      if (res.type == 'VARIABLE') {
        debug.variableList = formatTableData(res.data.column, result);
      }
      if (res.type == 'STACK') {
        debug.stackList = formatTableData(res.data.column, result);
      }
      // get file's content
      if (res.type == 'VIEW') {
        if (isSaving.value) {
          clearInterval(refreshCounter.counter);
          loading.value.close();
          TagsViewStore.delCurrentView(route);
          const title = saveFile.name;
          router.push({
            path: `/debug/${encodeURIComponent(route.query.connectInfoId as string)}_${
              route.query.schema
            }_fun_pro_${title}`,
            query: {
              title: `${title}@${route.query.connectInfoName}`,
              fileName: title,
              dbname: route.query.dbname,
              connectInfoName: route.query.connectInfoName,
              terminalNum: TagsViewStore.maxTerminalNum + 1,
              schema: route.query.schema,
              uuid: ws.uuid,
              oid: saveFile.oid,
              time: Date.now(),
            },
          });
        } else {
          editorRef.value.setValue(result);
          loading.value.close();
        }
        if (route.name == 'debugChild') {
          ws.instance.send({
            operation: 'initStep',
            ...commonWsParams.value,
            oldWindowName: ws.rootWindowName,
            sql: result,
          });
        }
      }
      if (res.type == 'NEW_FILE') {
        saveFile.name = result.name;
        saveFile.oid = result.oid;
        const title = result.name;
        const oid = result.oid;
        TagsViewStore.delViewById(tagId);
        router.push({
          path: `/debug/${encodeURIComponent(route.query.connectInfoId as string)}_${
            route.query.schema
          }_fun_pro_${oid}`,
          query: {
            title: `${title}@${route.query.connectInfoName}`,
            fileName: title,
            dbname: route.query.dbname,
            connectInfoName: route.query.connectInfoName,
            terminalNum: TagsViewStore.maxTerminalNum + 1,
            schema: route.query.schema,
            uuid: ws.uuid,
            oid,
            time: Date.now(),
          },
        });
      }
      if (res.type == 'VARIABLE_HIGHLIGHT') {
        debug.variableHighLight = result;
      }
      if (res.type == 'NEW_WINDOW') {
        const { name, oid } = result;
        const path = `/debugChild/${encodeURIComponent(route.query.dbname + '_' + oid)}`;
        const visitedViews = TagsViewStore.visitedViews;
        const eventView = visitedViews.find((item) => item.path == path);
        if (eventView) {
          router.push(eventView.fullPath);
        } else {
          router.push({
            path,
            query: {
              title: `${name}@${ws.connectionName}`,
              fileName: name,
              dbname: route.query.dbname,
              connectInfoName: ws.connectionName,
              uuid: ws.uuid,
              schema: route.query.schema,
              parentTagId: tagId,
              rootTagId: route.name == 'debug' ? tagId : route.query.rootTagId,
              parentWindowName: ws.sessionId,
              rootWindowName: ws.rootWindowName,
              oid,
              time: Date.now(),
            },
          });
        }
      }
      if (res.type == 'SWITCH_WINDOW') {
        const targetView = TagsViewStore.getViewByOid(result);
        if (targetView?.fullPath) {
          loading.value = loadingInstance();
          router.push(targetView.fullPath);
          loading.value.close();
        }
      }
      if (res.type == 'CLOSE_WINDOW') {
        const parentView = TagsViewStore.getViewById(ws.parentTagId);
        if (parentView?.fullPath) {
          alreadyCloseWindow.value = true;
          loading.value = loadingInstance();
          TagsViewStore.delCurrentView(route);
          router.push(parentView.fullPath);
          loading.value.close();
        }
      }
      if (res.type == 'OTHER') {
        loading.value?.close();
      }
    } else if (res.code == '500' && res.type == 'IGNORE_WINDOW' && isSaving.value) {
      return;
    } else {
      loading.value?.close();
      ElMessageBox.alert(res.msg, t('common.error'));
      props.editorType == 'sql' && getButtonStatus();
    }
  };

  const getButtonStatus = () => {
    if (route.name != 'createDebug' || route.query.type == 'anonymous') {
      ws.instance.send({
        operation: 'operateStatus',
        ...commonWsParams.value,
      });
    }
  };

  const changeServerLanguage = (language) => {
    ws.instance.send({
      operation: 'changeLanguage',
      language,
    });
  };

  const handleCompile = () => {
    tabList.value = [];
    tabValue.value = 'home';
    ws.instance.send({
      operation: 'compile',
      ...commonWsParams.value,
      sql: editorRef.value.getValue(),
      schema: ws.schema,
      isPackage: ws.isPackage,
    });
    getButtonStatus();
  };

  const handleExecute = (isContinue) => {
    tabList.value = [];
    tabValue.value = 'home';
    if (props.editorType == 'sql') {
      changeSqlBarStatus('running');
      ws.instance.send({
        operation: 'startRun',
        ...commonWsParams.value,
        webUser: UserStore.userId,
        sql: editorRef.value.getSelectionValue() || editorRef.value.getValue(),
      });
    }
    if (props.editorType == 'debug' && route.query.type != 'anonymous') {
      ws.instance.send({
        operation: 'execute',
        ...commonWsParams.value,
        sql: editorRef.value.getValue(),
        isContinue,
      });
      getButtonStatus();
    }
    if (route.query.type == 'anonymous') {
      ws.instance.send({
        operation: 'startRun',
        ...commonWsParams.value,
        sql: editorRef.value.getSelectionValue() || editorRef.value.getValue(),
      });
    }
  };

  const handleStop = () => {
    ws.instance.send({
      operation: 'stopRun',
      ...commonWsParams.value,
      sql: editorRef.value.getValue(),
    });
  };

  const handleClear = () => {
    editorRef.value.setValue('');
  };

  const handleStartDebug = () => {
    editorRef.value.removeAllDiasbledBreakPoint(); // clear all disabled breakPoint
    showResult.value = false;
    tabList.value = [];
    debug.isDebugging = true;
    if (route.query.type == 'anonymous') {
      editorRef.value.formatCode();
      ws.instance.send({
        operation: 'anonymousStartDebug',
        ...commonWsParams.value,
        sql: editorRef.value.getValue(),
        oid: '0',
      });
    } else {
      ws.instance.send({
        operation: 'startDebug',
        ...commonWsParams.value,
        sql: editorRef.value.getValue(),
      });
    }
  };

  const handleStopDebug = (isToParent) => {
    if (route.query.type == 'anonymous') {
      ws.instance.send({
        operation: 'stopDebug',
        ...commonWsParams.value,
        oid: '0',
      });
    } else {
      ws.instance.send({
        operation: 'stopDebug',
        ...commonWsParams.value,
      });
    }
    debug.isDebugging = false;
    TagsViewStore.closeAllChildViews(tagId);
    const rootDebugView = TagsViewStore.getViewById(tagId);
    if (rootDebugView && isToParent) {
      router.push(rootDebugView.fullPath);
    }
  };

  const handleContinueStep = () => {
    ws.instance.send({
      operation: 'continueStep',
      ...commonWsParams.value,
      oldWindowName: ws.parentWindowName,
      sql: editorRef.value.getValue(),
    });
  };

  const handleSingleStep = () => {
    ws.instance.send({
      operation: 'singleStep',
      ...commonWsParams.value,
      oldWindowName: ws.parentWindowName,
      sql: editorRef.value.getValue(),
    });
  };

  const handleStepIn = () => {
    ws.instance.send({
      operation: 'stepIn',
      ...commonWsParams.value,
      oldWindowName: ws.parentWindowName,
      sql: editorRef.value.getValue(),
    });
  };

  const handleStepOut = (isUnMount?: boolean) => {
    ws.instance.send({
      operation: 'stepOut',
      ...commonWsParams.value,
      oldWindowName: ws.parentWindowName,
      isCloseWindow: isUnMount,
      sql: editorRef.value.getValue(),
    });
  };

  const handleFormat = () => {
    editorRef.value.formatCode();
  };

  const handleImportFile = (data: string) => {
    if (editorRef.value.getValue().trim()) {
      importFileData = data;
      showImportFileTipsDialog.value = true;
    } else {
      editorRef.value.setValue(data);
    }
  };
  const handleImportFileComfirm = (type: 'append' | 'overwrite') => {
    if (type === 'overwrite') editorRef.value.setValue(importFileData);
    if (type === 'append')
      editorRef.value.setValue(`${editorRef.value.getValue()}\n${importFileData}`);
  };
  const handleImportFileClose = () => {
    importFileData = '';
  };

  const handleExportFile = () => {
    const data = editorRef.value.getValue();
    downLoadMyBlobType(`${ws.connectionName}_${Date.now()}.sql`, data);
  };

  type BreakOperation =
    | 'addBreakPoint'
    | 'deleteBreakPoint'
    | 'enableBreakPoint'
    | 'disableBreakPoint';
  const handleBreakPoint = (line: number, operation: BreakOperation) => {
    debug.isDebugging &&
      ws.instance.send({
        operation: operation,
        ...commonWsParams.value,
        line: line + 1,
        oldWindowName: ws.parentWindowName,
      });
  };

  const handleCreateCoverageRate = (status) => {
    ws.instance.send({
      operation: 'createCoverageRate',
      ...commonWsParams.value,
      sql: editorRef.value.getValue(),
      inputParams: preInputParams.value,
      isCoverage: status,
    });
  };

  const loadTerminal = (txt) => {
    const oldSql = editorRef.value.getValue();
    editorRef.value.setValue(`${oldSql ? oldSql + '\n' : ''}${txt}`);
  };
  provide('loadTerminalFunc', loadTerminal);

  // is create? and return default tempalate
  onMounted(async () => {
    if (!TagsViewStore.getViewByRoute(route)) return;
    if (['createTerminal', 'createDebug'].includes(route.name as string)) {
      const defaultTemplate: string = await createTemplate();
      editorRef.value.setValue(defaultTemplate);
    }
    if (['debugChild'].includes(props.editorType)) {
      debug.isDebugging = true;
      sqlData.readOnly = true;
    }
    const sessionId = route.query.connectInfoName + '_' + route.query.time;
    Object.assign(ws, {
      name: route.query.connectInfoName,
      webUser: UserStore.userId,
      rootId: route.query.rootId,
      parentTagId: route.query.parentTagId,
      connectionName: route.query.connectInfoName,
      uuid: route.query.uuid,
      sessionId,
      dbname: route.query.dbname,
      schema: route.query.schema,
      fileName: route.query.fileName,
      oid: route.query.oid,
      parentWindowName: route.query.parentWindowName,
      rootWindowName: route.query.rootWindowName || sessionId,
      isPackage: route.query.isPackage === 'y',
      isInPackage: route.query.isInPackage === 'y',
    });
    ws.instance = new WebSocketClass(ws.name, ws.sessionId, onWebSocketMessage);

    ws.instance.send({
      operation: 'connection',
      language: AppStore.language,
      ...commonWsParams.value,
    });

    if (['debug', 'debugChild'].includes(route.name as string)) {
      loading.value = loadingInstance();
      try {
        if (ws.isPackage) {
          ws.instance.send({
            operation: 'showPackage',
            ...commonWsParams.value,
            schema: ws.schema,
          });
        } else {
          ws.instance.send({
            operation: 'funcProcedure',
            oldWindowName: route.name == 'debugChild' ? ws.parentWindowName : undefined,
            ...commonWsParams.value,
          });
        }
      } catch (error) {
        loading.value.close();
      }
    }

    if (route.query.type == 'anonymous') {
      loading.value = loadingInstance();
      try {
        ws.instance.send({
          operation: 'anonymousBlock',
          ...commonWsParams.value,
        });
      } catch (error) {
        loading.value.close();
      }
    }
  });
  onBeforeUnmount(() => {
    refreshCounter.counter = null;
    if (!ws.instance) return;
    ['debug'].includes(props.editorType) && handleStopDebug(false);
    ['debugChild'].includes(props.editorType) && !alreadyCloseWindow.value && handleStepOut(true);
    ws.instance.send({
      operation: 'close',
      windowName: ws.sessionId,
    });
    ws.instance.close();
    Object.assign(ws, {
      name: '',
      instance: null,
    });
  });
</script>

<style lang="scss" scoped>
  .terminal {
    height: 100%;
    display: flex;
    flex-direction: column;
  }
  .editor {
    flex-shrink: 0;
    height: 100%;
    display: flex;
    flex-direction: column;
    padding: 10px 10px 8px 0;
  }
  .monaco-wrapper {
    flex: 1;
    padding: 0px;
    overflow: hidden;
    display: flex;
  }

  .result {
    height: 100%;
    overflow: hidden;
  }
</style>
