<!-- this component line number start with 1. -->
<template>
  <div class="terminal" :id="id">
    <div class="editor" :style="{ height: editorHeight }">
      <FunctionBar
        :type="sqlData.barType"
        :status="sqlData.barStatus"
        :editorReadOnly="sqlData.readOnly"
        :isGlobalEnable="isGlobalEnable"
        @execute="handleExecute"
        @stopRun="handleStop"
        @clear="handleClear"
        @startDebug="handleStartDebug"
        @stopDebug="handleStopDebug"
        @breakPointStep="handleBreakPointStep"
        @singleStep="handleSingleStep"
        @stepIn="handleStepIn"
        @stepOut="handleStepOut(false)"
        @format="handleFormat"
      />
      <div class="monaco-wrapper" ref="monacoWrapper">
        <AceEditor
          ref="editorRef"
          :value="sqlData.sqlText"
          :height="monacoHeight"
          :readOnly="!isGlobalEnable || sqlData.readOnly"
          :openDebug="['debug', 'debugChild'].includes(props.editorType)"
          @addBreakPoint="(line) => handleBreakPoint(line, 'addBreakPoint')"
          @removeBreakPoint="(line) => handleBreakPoint(line, 'deleteBreakPoint')"
          @enableBreakPoint="(line) => handleBreakPoint(line, 'enableBreakPoint')"
          @disableBreakPoint="(line) => handleBreakPoint(line, 'disableBreakPoint')"
          style="width: 100%; margin-top: 4px; border: 1px solid #ddd"
        />
        <DebugPane
          v-if="debug.isDebugging"
          :stackList="debug.stackList"
          :breakPointList="debug.breakPointList"
          :variableList="debug.variableList"
          :variableHighLight="debug.variableHighLight"
          @changeBreakPoint="changeDebugPanePoint"
        />
      </div>
    </div>
    <div v-if="showResult" class="result">
      <ResultTabs :msgData="msgData" v-model:tabList="tabList" v-model:tabValue="tabValue" />
    </div>
    <EnterParamsDialog
      v-model="enterParams.showParams"
      :data="enterParams.data"
      @confirm="paramsConfirm"
      @cancel="paramsCancel"
    />
  </div>
</template>

<script lang="ts" setup name="TerminalEditor">
  import FunctionBar from '@/components/FunctionBar.vue';
  import AceEditor from '@/components/AceEditor.vue';
  import DebugPane from './DebugPane.vue';
  import ResultTabs from '@/components/ResultTabs.vue';
  import EnterParamsDialog from './EnterParamsDialog.vue';
  import { ElMessageBox } from 'element-plus';
  import { computed, onActivated, onBeforeUnmount, onMounted, reactive, ref, watch } from 'vue';
  import createTemplate from './createTemplate';
  import { useRoute, useRouter } from 'vue-router';
  import WebSocketClass from '@/utils/websocket';
  import { dateFormat, formatTableV2Data, formatTableData, loadingInstance, uuid } from '@/utils';
  import { useAppStore } from '@/store/modules/app';
  import { useUserStore } from '@/store/modules/user';
  import { useTagsViewStore } from '@/store/modules/tagsView';
  import { useI18n } from 'vue-i18n';
  import EventBus, { EventTypeName } from '@/utils/event-bus';

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
      execute: true,
      startDebug: false,
      stopDebug: false,
      breakPointStep: false,
      singleStep: false,
      stepIn: false,
      stepOut: false,
    },
    debugChild: {
      breakPointStep: true,
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
  const monacoHeight = ref('295px');
  const editorHeight = ref<string>('100%');
  const editorRef = ref();
  const ws = reactive({
    name: '',
    webUser: '',
    rootId: '',
    parentId: '',
    connectionName: '',
    uuid: '',
    sessionId: '',
    dbname: '',
    schema: '',
    instance: null,
    rootWindowName: '',
  });
  const tagId = TagsViewStore.getViewByRoute(route)?.id;
  const commonWsParams = computed(() => {
    return {
      webUser: ws.webUser,
      uuid: ws.uuid,
      windowName: ws.sessionId,
    };
  });
  const isGlobalEnable = computed(() => {
    return AppStore.connectedDatabase.findIndex((item) => item.uuid == ws.uuid) > -1;
  });
  const isStepIntoChild = computed(() => {
    return TagsViewStore.getViewByRoute(route)?.isStepIntoChild;
  });
  const loading = ref(null);
  const refreshCounter = reactive({
    counter: null,
    times: 0,
  });
  const isSaving = ref(false);
  const saveFileTitle = ref('');
  const alreadyCloseWindow = ref(false);

  // set editor height
  watch(
    showResult,
    (val) => {
      if (!TagsViewStore.getViewByRoute(route)) return;
      editorHeight.value = val ? '380px' : '100%';
      setTimeout(() => {
        let padding =
          parseFloat(getComputedStyle(monacoWrapper.value).paddingTop) +
          parseFloat(getComputedStyle(monacoWrapper.value).paddingBottom);
        monacoHeight.value =
          monacoWrapper.value.getBoundingClientRect().height - padding - 4 + 'px';
      }, 500);
    },
    {
      immediate: true,
    },
  );

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
    ws.instance.send({
      operation: 'inputParam',
      ...commonWsParams.value,
      sql: editorRef.value.getValue(),
      inputParams: enterParams.data.map((item) => ({ [item.type]: item.value })),
      breakPoints: editorRef.value.getAllLineDecorations(),
    });
    getButtonStatus();
  };
  const paramsCancel = () => {
    handleStopDebug();
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
      if (!value) {
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
    if (!isGlobalEnable.value) return;
    let res: Message = JSON.parse(data);
    if (res.code == '200') {
      if (
        props.editorType == 'debug' &&
        res.type != 'operateStatus' &&
        res.type != 'newWindow' &&
        !isStepIntoChild.value
      ) {
        getButtonStatus();
      }
      const result = res.data?.result;
      if (res.type == 'text') {
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
      if (res.type == 'button') {
        getButtonStatus();
      }
      if (res.type == 'operateStatus') {
        if (props.editorType == 'sql') {
          sqlData.barStatus = {
            execute: result.startRun,
            stopRun: result.stopRun,
            clear: result.startRun,
          };
          sqlData.readOnly = !result?.startRun;
        } else {
          sqlData.barStatus = result;
          sqlData.readOnly = !result?.execute;
        }
        // can use 'startDebug' button = not isDebugging
        if (props.editorType == 'debug') {
          debug.isDebugging = !(result.startDebug || result.execute);
        }
      }
      if (res.type == 'paramWindow') {
        const arr = result.map((item) => {
          return {
            name: Object.keys(item)[0],
            type: Object.values(item)[0],
            value: null,
          };
        });
        enterParams.data = arr;
        enterParams.showParams = true;
      }
      if (res.type == 'table') {
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
      if (res.type == 'breakPoint') {
        debug.breakPointList = formatTableData(res.data.column, result);
      }
      if (res.type == 'variable') {
        debug.variableList = formatTableData(res.data.column, result);
      }
      if (res.type == 'stack') {
        debug.stackList = formatTableData(res.data.column, result);
      }
      // get file's content
      if (res.type == 'view') {
        if (isSaving.value) {
          clearInterval(refreshCounter.counter);
          loading.value.close();
          TagsViewStore.delCurrentView(route);
          const title = saveFileTitle.value;
          router.push({
            // eslint-disable-next-line prettier/prettier
            path: `/debug/${encodeURIComponent(route.query.connectInfoId as string)}_${route.query.schema}_fun_pro_${title}`,
            query: {
              title: `${title}@${route.query.connectInfoName}`,
              funcname: title,
              dbname: route.query.dbname,
              connectInfoName: route.query.connectInfoName,
              terminalNum: TagsViewStore.maxTerminalNum + 1,
              schema: route.query.schema,
              uuid: ws.uuid,
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
      if (res.type == 'refresh') {
        if (['createTerminal', 'createDebug'].includes(route.name as string)) {
          saveFileTitle.value = res.data.result;
          if (isSaving.value) {
            loading.value = loadingInstance();
            refreshCounter.counter = setInterval(() => {
              refreshCounter.times++;
              if (refreshCounter.times > 6) {
                clearInterval(refreshCounter.counter);
                loading.value.close();
                isSaving.value = false;
                EventBus.notify(EventTypeName.CLOSE_SELECTED_TAB, route);
              } else {
                ws.instance.send({
                  operation: 'funcProcedure',
                  fullName: saveFileTitle.value,
                  schema: route.query.schema,
                  ...commonWsParams.value,
                });
              }
            }, 6000);
          }
        }
      }
      if (res.type == 'variableHighLight') {
        debug.variableHighLight = result;
      }
      if (res.type == 'newWindow') {
        setStepIntoChildStatus(tagId, true);
        const path = encodeURIComponent(route.query.dbname + '_' + result);
        router.push({
          path: `/debugChild/${path}`,
          query: {
            title: `${result}@${ws.connectionName}`,
            funcname: result,
            dbname: route.query.dbname,
            connectInfoName: ws.connectionName,
            uuid: ws.uuid,
            schema: route.query.schema,
            parentTagId: TagsViewStore.getViewByRoute(route)?.id,
            rootTagId:
              route.name == 'debug'
                ? TagsViewStore.getViewByRoute(route)?.id
                : route.query.rootTagId,
            rootWindowName: ws.sessionId,
            time: Date.now(),
          },
        });
      }
      if (res.type == 'closeWindow') {
        const parentView = TagsViewStore.getViewById(route.query.parentTagId);
        if (parentView?.fullPath) {
          alreadyCloseWindow.value = true;
          loading.value = loadingInstance();
          TagsViewStore.delCurrentView(route);
          router.push(parentView.fullPath);
          loading.value.close();
        }
      }
    } else if (res.code == '500' && res.type == 'ignoreWindow' && isSaving.value) {
      return;
    } else {
      loading.value?.close();
      ElMessageBox.alert(res.msg, t('common.error'));
      props.editorType == 'sql' && getButtonStatus();
    }
  };

  const getButtonStatus = () => {
    ws.instance.send({
      operation: 'operateStatus',
      ...commonWsParams.value,
    });
  };

  const changeServerLanguage = (language) => {
    ws.instance.send({
      operation: 'changeLanguage',
      language,
    });
  };

  const handleExecute = () => {
    tabList.value = [];
    tabValue.value = 'home';
    if (props.editorType == 'sql') {
      changeSqlBarStatus('running');
      ws.instance.send({
        operation: 'startRun',
        ...commonWsParams.value,
        sql: editorRef.value.getSelectionValue() || editorRef.value.getValue(),
      });
    }
    if (props.editorType == 'debug') {
      isSaving.value = true;
      ws.instance.send({
        operation: 'execute',
        ...commonWsParams.value,
        sql: editorRef.value.getValue(),
        isDebug: false,
      });
      getButtonStatus();
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
    ws.instance.send({
      operation: 'startDebug',
      ...commonWsParams.value,
      sql: editorRef.value.getValue(),
      breakPoints: editorRef.value.getAllLineDecorations(),
      isDebug: true,
    });
  };

  const handleStopDebug = () => {
    ws.instance.send({
      operation: 'stopDebug',
      ...commonWsParams.value,
    });
    debug.isDebugging = false;
    TagsViewStore.closeAllChildViews(tagId, router);
  };

  const handleBreakPointStep = () => {
    ws.instance.send({
      operation: 'breakPointStep',
      ...commonWsParams.value,
      oldWindowName: ws.rootWindowName,
      sql: editorRef.value.getValue(),
    });
  };

  const handleSingleStep = () => {
    ws.instance.send({
      operation: 'singleStep',
      ...commonWsParams.value,
      oldWindowName: ws.rootWindowName,
      sql: editorRef.value.getValue(),
    });
  };

  const handleStepIn = () => {
    ws.instance.send({
      operation: 'stepIn',
      ...commonWsParams.value,
      oldWindowName: ws.rootWindowName,
      sql: editorRef.value.getValue(),
    });
  };

  const handleStepOut = (isUnMount?: boolean) => {
    ws.instance.send({
      operation: 'stepOut',
      ...commonWsParams.value,
      oldWindowName: ws.rootWindowName,
      isCloseWindow: isUnMount,
      sql: editorRef.value.getValue(),
    });
  };

  const handleFormat = () => {
    editorRef.value.formatCode();
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
        oldWindowName: ws.rootWindowName,
      });
  };

  const getStepIntoChildStatus = (tag) => {
    return ['number', 'string'].includes(typeof tag)
      ? !!TagsViewStore.getViewById(tag)?.isStepIntoChild
      : !!TagsViewStore.getViewByRoute(tag)?.isStepIntoChild;
  };
  const setStepIntoChildStatus = (tag, status) => {
    ['number', 'string'].includes(typeof tag)
      ? TagsViewStore.updateStepIntoChildStatusById(tag, status)
      : TagsViewStore.updateStepIntoChildStatusByRoute(tag, status);
    if (['debug', 'debugChild'].includes(props.editorType)) {
      Object.assign(sqlData.barStatus, {
        breakPointStep: !status,
        singleStep: !status,
        stepIn: !status,
        stepOut: !status,
      });
    }
  };
  onActivated(() => {
    if (['debug'].includes(route.name as string)) {
      const status = getStepIntoChildStatus(tagId);
      status ? setStepIntoChildStatus(tagId, true) : getButtonStatus();
    }
  });

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

    Object.assign(ws, {
      name: route.query.connectInfoName,
      webUser: UserStore.userId,
      rootId: route.query.rootId,
      parentId: route.query.parentTagId,
      connectionName: route.query.connectInfoName,
      uuid: route.query.uuid,
      sessionId: route.query.connectInfoName + '_' + route.query.time,
      dbname: route.query.dbname,
      schema: route.query.schema,
      rootWindowName: route.query.rootWindowName,
    });
    ws.instance = new WebSocketClass(ws.name, ws.sessionId, onWebSocketMessage);

    ws.instance.send({
      operation: 'connection',
      language: AppStore.language,
      ...commonWsParams.value,
    });

    if (['debug', 'debugChild'].includes(route.name as string)) {
      const { funcname } = route.query;
      loading.value = loadingInstance();
      try {
        ws.instance.send({
          operation: 'funcProcedure',
          fullName: funcname,
          schema: ws.schema,
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
    ['debug'].includes(props.editorType) && handleStopDebug();
    ['debugChild'].includes(props.editorType) && !alreadyCloseWindow.value && handleStepOut(true);
    if (['debugChild'].includes(props.editorType)) {
      TagsViewStore.updateStepIntoChildStatusById(ws.parentId, false);
    } else {
      ws.instance.send({
        operation: 'close',
        windowName: ws.sessionId,
      });
    }
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
    display: flex;
    flex-direction: column;
    padding: 10px;
  }
  .monaco-wrapper {
    flex: 1;
    padding: 0px;
    overflow: hidden;
    display: flex;
  }

  .result {
    flex: 1;
    overflow: hidden;
  }
</style>
