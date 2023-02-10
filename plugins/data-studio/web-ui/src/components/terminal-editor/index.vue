<!-- this component line number start with 1. -->
<template>
  <div class="terminal">
    <div class="editor" :style="{ height: editorHeight }">
      <FunctionBar
        :type="sqlData.barType"
        :status="sqlData.barStatus"
        :readOnly="sqlData.readOnly"
        @execute="handleExecute"
        @stop="handleStop"
        @clear="handleClear"
        @startDebug="handleStartDebug"
        @stopDebug="handleStopDebug"
        @breakPointStep="handleBreakPointStep"
        @singleStep="handleSingleStep"
        @format="handleFormat"
      />
      <div class="monaco-wrapper" ref="monacoWrapper">
        <AceEditor
          ref="editorRef"
          :value="sqlData.sqlText"
          :height="monacoHeight"
          :readOnly="sqlData.readOnly"
          :openDebug="props.editorType == 'debug'"
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
  import { onBeforeUnmount, onMounted, reactive, ref, watch } from 'vue';
  import createTemplate from './createTemplate';
  import { useRoute, useRouter } from 'vue-router';
  import WebSocketClass from '@/utils/websocket';
  import { dateFormat, formatTableV2Data, formatTableData, loadingInstance } from '@/utils';
  import { useUserStore } from '@/store/modules/user';
  import { useTagsViewStore } from '@/store/modules/tagsView';
  import { useI18n } from 'vue-i18n';
  import EventBus, { EventTypeName } from '@/utils/event-bus';

  const route = useRoute();
  const router = useRouter();
  const UserStore = useUserStore();
  const TagsViewStore = useTagsViewStore();
  const { t } = useI18n();
  const props = withDefaults(
    defineProps<{
      editorType: 'sql' | 'debug';
      initValue?: string;
    }>(),
    {
      editorType: 'sql',
      initValue: '',
    },
  );
  const sqlData = reactive<{
    sqlText: string;
    readOnly: boolean;
    barStatus: Record<string, boolean>;
    barType: 'sql' | 'debug';
  }>({
    sqlText: props.initValue,
    readOnly: false,
    barStatus:
      props.editorType == 'sql'
        ? {
            execute: true,
            stop: false,
            clear: true,
          }
        : {
            execute: true,
            startDebug: false,
            stopDebug: false,
            breakPointStep: false,
            singleStep: false,
          },
    barType: props.editorType,
  });

  const showResult = ref(false);
  const monacoWrapper = ref<HTMLElement>();
  const monacoHeight = ref('295px');
  const editorHeight = ref<string>('100%');
  const editorRef = ref();
  const ws = reactive({
    name: '',
    webUser: '',
    connectionName: '',
    sessionId: '',
    instance: null,
  });
  const loading = ref(null);
  const refreshCounter = reactive({
    counter: null,
    times: 0,
  });
  const isSaving = ref(false);
  const saveFileTitle = ref('');
  // set editor height
  watch(
    showResult,
    (val) => {
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
      webUser: ws.webUser,
      connectionName: ws.connectionName,
      windowName: ws.sessionId,
      sql: editorRef.value.getValue(),
      inputParams: enterParams.data.map((item) => ({ [item.type]: item.value })),
      breakPoints: editorRef.value.getAllLineDecorations(),
    });
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
          editorRef.value.setCurrentBreakPoint(parseInt(list[list.length - 1].lineno) - 1);
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
    sqlData.barStatus.execute = type === 'init';
    sqlData.barStatus.stop = type !== 'init';
  };

  interface Message {
    code: string;
    data?: any;
    msg: string;
    type: string;
  }
  const onWebSocketMessage = (data: string) => {
    let res: Message = JSON.parse(data);
    if (res.code == '200') {
      const result = res.data?.result;
      if (res.type == 'text') {
        if (
          props.editorType == 'sql' &&
          ['End of execution', 'Close successfully'].includes(res.msg)
        ) {
          changeSqlBarStatus('init');
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
      if (res.type == 'operateStatus') {
        sqlData.barStatus = result;
        sqlData.readOnly = !result?.execute;
        // can use 'startDebug' button = not isDebugging
        if (result.startDebug) {
          debug.isDebugging = false;
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
              time: Date.now(),
            },
          });
        } else {
          editorRef.value.setValue(result);
          loading.value.close();
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
                  webUser: ws.webUser,
                  connectionName: ws.connectionName,
                  windowName: ws.sessionId,
                });
              }
            }, 6000);
          }
        }
      }
      if (res.type == 'variableHighLight') {
        debug.variableHighLight = result;
      }
    } else if (res.code == '500' && res.type == 'ignoreWindow' && isSaving.value) {
      return;
    } else {
      loading.value && loading.value.close();
      ElMessageBox.alert(res.msg, t('common.error'));
      props.editorType == 'sql' && changeSqlBarStatus('init');
    }
  };

  const getButtonStatus = () => {
    props.editorType == 'debug' &&
      ws.instance.send({
        operation: 'operateStatus',
        webUser: ws.webUser,
        connectionName: ws.connectionName,
        windowName: ws.sessionId,
      });
  };

  const handleExecute = () => {
    tabList.value = [];
    if (props.editorType == 'sql') {
      changeSqlBarStatus('running');
      ws.instance.send({
        operation: 'startRun',
        webUser: ws.webUser,
        connectionName: ws.connectionName,
        windowName: ws.sessionId,
        sql: editorRef.value.getSelectionValue() || editorRef.value.getValue(),
      });
    }
    if (props.editorType == 'debug') {
      isSaving.value = true;
      ws.instance.send({
        operation: 'execute',
        webUser: ws.webUser,
        connectionName: ws.connectionName,
        windowName: ws.sessionId,
        sql: editorRef.value.getValue(),
        isDebug: false,
      });
      getButtonStatus();
    }
  };

  const handleStop = () => {
    ws.instance.send({
      operation: 'stopRun',
      webUser: ws.webUser,
      connectionName: ws.connectionName,
      windowName: ws.sessionId,
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
      webUser: ws.webUser,
      connectionName: ws.connectionName,
      windowName: ws.sessionId,
      sql: editorRef.value.getValue(),
      breakPoints: editorRef.value.getAllLineDecorations(),
      isDebug: true,
    });
    getButtonStatus();
  };

  const handleStopDebug = () => {
    ws.instance.send({
      operation: 'stopDebug',
      webUser: ws.webUser,
      connectionName: ws.connectionName,
      windowName: ws.sessionId,
    });
    getButtonStatus();
    debug.isDebugging = true;
  };

  const handleBreakPointStep = () => {
    ws.instance.send({
      operation: 'breakPointStep',
      webUser: ws.webUser,
      connectionName: ws.connectionName,
      windowName: ws.sessionId,
    });
    getButtonStatus();
  };

  const handleSingleStep = () => {
    ws.instance.send({
      operation: 'singleStep',
      webUser: ws.webUser,
      connectionName: ws.connectionName,
      windowName: ws.sessionId,
    });
    getButtonStatus();
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
        webUser: ws.webUser,
        connectionName: ws.connectionName,
        windowName: ws.sessionId,
        line: line + 1,
      });
  };

  // is create? and return default tempalate
  onMounted(async () => {
    if (['createTerminal', 'createDebug'].includes(route.name as string)) {
      const defaultTemplate: string = await createTemplate();
      editorRef.value.setValue(defaultTemplate);
    }
    ws.name = route.query.connectInfoName as string;
    ws.webUser = UserStore.userId;
    ws.connectionName = route.query.connectInfoName as string;
    ws.sessionId = (ws.name + '_' + route.query.time) as string;
    ws.instance = new WebSocketClass(ws.name, ws.sessionId, onWebSocketMessage);
    ws.instance.send({
      operation: 'connection',
      webUser: ws.webUser,
      connectionName: ws.connectionName,
      windowName: ws.sessionId,
    });
    if (route.name == 'debug') {
      const { funcname, schema } = route.query;
      loading.value = loadingInstance();
      try {
        ws.instance.send({
          operation: 'funcProcedure',
          fullName: funcname,
          schema,
          webUser: ws.webUser,
          connectionName: ws.connectionName,
          windowName: ws.sessionId,
        });
        getButtonStatus();
      } catch (error) {
        loading.value.close();
      }
    }
  });
  onBeforeUnmount(() => {
    refreshCounter.counter = null;
    props.editorType == 'debug' && handleStopDebug();
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
