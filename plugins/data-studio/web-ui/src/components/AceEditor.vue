<!-- this component line number start with 0. -->
<template>
  <div class="ace-wrapper" :style="{ width: $props.width, height: $props.height }">
    <div class="ace-editor" :id="id" ref="aceRef" style="height: 100%"></div>
    <ContextMenu
      v-model:show="contextMenuVisible"
      :offset="contextMenuOffset"
      :list="contextMenuList"
    />
  </div>
</template>

<script lang="ts" setup>
  import ace from 'ace-builds';
  import { Ace } from 'ace-builds';
  import 'ace-builds/src-noconflict/theme-chrome';
  import 'ace-builds/src-noconflict/theme-monokai';
  import 'ace-builds/src-noconflict/mode-sql';
  import 'ace-builds/src-noconflict/snippets/sql';
  import 'ace-builds/src-noconflict/ext-language_tools';
  import { format as sqlFormatter } from 'sql-formatter';
  import type { FormatOptions } from 'sql-formatter';
  import { uuid, copyToClickBoard } from '@/utils';
  import { isDark } from '@/hooks/dark';
  import ContextMenu from './ContextMenu/index.vue';
  import { useI18n } from 'vue-i18n';

  interface Annotations {
    row: number;
    text: string;
    column?: number;
    type?: string;
  }

  const { t } = useI18n();
  const props = withDefaults(
    defineProps<{
      modelValue?: string;
      type?: 'form' | 'page'; // in form-item or page
      value?: string;
      width?: string;
      height?: string;
      readOnly?: boolean;
      minLines?: number;
      maxLines?: number;
      fontSize?: number;
      tabSize?: number;
      openDebug?: boolean;
      theme?: string; // 'light' | 'dark'
    }>(),
    {
      modelValue: '',
      type: 'form',
      value: '',
      height: '100%',
      readOnly: true,
      minLines: 10,
      fontSize: 14,
      tabSize: 2,
      openDebug: false,
      theme: 'light',
    },
  );

  const myEmit = defineEmits<{
    (event: 'update:modelValue', value: string): void;
    (event: 'addBreakPoint', line: number): void;
    (event: 'removeBreakPoint', line: number): void;
    (event: 'enableBreakPoint', line: number): void;
    (event: 'disableBreakPoint', line: number): void;
    (event: 'snippet', text: string): void;
  }>();

  const id = 'monaco_' + uuid();
  const aceRef = ref();
  let aceEditor = null;
  const breakPointType = {
    0: 'breakpoints-fake',
    1: 'disable-breakpoints',
    2: 'breakpoints',
  };
  const currentPointData = {
    line: null,
    lineMarkerId: null,
  };

  const contextMenuVisible = ref(false);
  const contextMenuOffset = ref({
    left: 0,
    top: 0,
  });
  const contextMenuList = ref([]);
  const getContextMenuList = () => {
    const list = [
      {
        label: computed(() => t('button.copy')),
        click: () => handleCopy(),
      },
    ];
    if (getAllSelectionValue() && props.type == 'page') {
      list.push({
        label: computed(() => t('snippets.create')),
        click: () => myEmit('snippet', getAllSelectionValue()),
      });
    }
    contextMenuList.value = list;
  };

  const hideContextMenu = () => {
    contextMenuVisible.value = false;
  };

  const showContextMenu = (e) => {
    e.preventDefault();
    // Execute different custom commands based on the clicked location
    if (e.target.classList.contains('ace_text-input')) {
      // aceEditor.execCommand('customCommand');
      contextMenuOffset.value = {
        left: e.clientX,
        top: e.clientY,
      };
      getContextMenuList();
      contextMenuVisible.value = true;
      setEditorBlur();
    }
  };

  watch(
    () => props.modelValue,
    (value) => {
      const position = aceEditor.getCursorPosition();
      aceEditor.getSession().setValue(value);
      aceEditor.clearSelection();
      aceEditor.moveCursorToPosition(position);
    },
  );
  watch(
    () => props.readOnly,
    (value) => {
      aceEditor.setReadOnly(value);
    },
  );
  watch(
    () => props.theme,
    (value) => {
      changeTheme(value);
    },
  );
  watch(isDark, (value) => {
    changeTheme(value ? 'dark' : 'light');
  });

  const initEditor = () => {
    if (aceEditor) aceEditor.destroy();
    aceEditor = markRaw(
      ace.edit(aceRef.value, {
        value: props.value,
        fontSize: props.fontSize,
        minLines: 10,
        maxLines: props.maxLines,
        tabSize: props.tabSize,
        theme: isDark.value ? 'ace/theme/monokai' : 'ace/theme/chrome',
        mode: 'ace/mode/sql',
        showPrintMargin: false,
        readOnly: props.readOnly,
        highlightGutterLine: false,
        highlightActiveLine: false,
      }),
    );
    aceEditor.setOptions({
      enableBasicAutocompletion: true,
      enableSnippets: true,
      enableLiveAutocompletion: true,
      enableMultiselect: false,
    });
    aceEditor.on('change', (delta) => {
      aceEditor.renderer.updateBreakpoints();
      if (delta.action == 'remove') {
        const startRow = delta.start.row;
        const endRow = delta.end.row;
        for (let i = startRow + 1, j = 0; i <= endRow; i++, j++) {
          aceEditor.session.clearBreakpoint(i);
        }
        if (startRow < endRow) {
          const breakpoints = aceEditor.session.getBreakpoints();
          const diffRow = endRow - startRow;
          // Let the breakpoints go up.(the breakpoint will not move after delete multiline code)
          if (endRow < breakpoints.length) {
            for (let i = endRow; i < breakpoints.length; i++) {
              aceEditor.session.clearBreakpoint(i);
              breakpoints[i + 1] &&
                aceEditor.session.setBreakpoint(i - diffRow + 1, breakpoints[i + 1]);
            }
          }
        }
      }
      myEmit('update:modelValue', aceEditor.getValue());
    });

    if (aceRef.value && window && 'ResizeObserver' in window) {
      const observe = new ResizeObserver((_el) => {
        const { target }: any = _el[0];
        if (target && target.offsetWidth) {
          resize();
        }
      });
      observe.observe(aceRef.value);
    }

    props.openDebug && registerDebug();

    /* aceEditor.commands.addCommand({
      name: 'customCommand',
      bindKey: { win: 'Ctrl-C', mac: 'Command-C' },
      exec: function (editor) {},
    }); */
  };

  const changeTheme = (theme: string) => {
    aceEditor.setTheme(theme == 'light' ? 'ace/theme/chrome' : 'ace/theme/monokai');
  };

  const lastClick = reactive({
    func: null, //last time setTimeout's id
    time: 0, //last time's timeStamp, default 0
  });
  const registerDebug = () => {
    aceEditor.on('guttermousemove', function (e) {
      const target = e.domEvent.target;
      if (target.className.indexOf('ace_gutter-cell') == -1) {
        return;
      }
      const left = target.getBoundingClientRect().left;
      if (e.clientX > 15 + left) {
        return;
      }
      const line = e.getDocumentPosition().row;
      if (aceEditor.session.getBreakpoints()[line]) {
        return;
      }
    });
    aceEditor.on('guttermousedown', function (e) {
      e.stop();
    });
    aceEditor.on('gutterclick', function (e) {
      e.stop();
      const target = e.domEvent.target;
      if (target.className.indexOf('ace_gutter-cell') == -1) {
        return;
      }
      if (e.clientX > 32 + target.getBoundingClientRect().left) {
        return;
      }
      const line = e.getDocumentPosition().row;
      let nowClickTime = new Date().getTime();
      let diff = nowClickTime - lastClick.time;
      if (diff < 200) {
        clearTimeout(lastClick.func);
        if (hasBreakPoint(line, 1) || hasBreakPoint(line, 2)) {
          removeBreakPoint(line);
          myEmit('removeBreakPoint', line);
        } else {
          addBreakPoint(line, 2);
          myEmit('addBreakPoint', line);
        }
      } else {
        lastClick.time = nowClickTime;
        lastClick.func = setTimeout(() => {
          if (hasBreakPoint(line, 2)) {
            addBreakPoint(line, 1);
            myEmit('disableBreakPoint', line);
          } else if (hasBreakPoint(line, 1)) {
            addBreakPoint(line, 2);
            myEmit('enableBreakPoint', line);
          } else {
            return;
          }
        }, 200);
      }
    });

    aceEditor.on('gutterdblclick', function (e) {
      e.stop();
    });
  };

  const getValue = () => {
    return aceEditor.getValue();
  };

  const setValue = (value: string) => {
    nextTick(() => {
      aceEditor.setValue(value, 1);
    });
  };

  const resize = () => {
    aceEditor.resize(true);
  };

  const getSelectionValue = () => {
    return aceEditor.session.getTextRange(aceEditor.getSelectionRange());
  };

  const getAllSelectionValue = () => {
    return aceEditor.getSelectedText();
  };

  const getLineValue = (row) => {
    return aceEditor.session.getLine(row);
  };

  const getCursorPostion = (): Ace.Position => {
    // or: aceEditor.getCursorPosition()
    return aceEditor.selection.getCursor();
  };

  const getCursorRowValue = () => {
    return getLineValue(getCursorPostion().row);
  };

  const setEditorBlur = () => {
    return aceEditor.blur();
  };

  const setEditorFocus = () => {
    return aceEditor.focus();
  };

  const hasBreakPoint = (line: number, type: keyof typeof breakPointType) => {
    let breakpoints = aceEditor.session.getBreakpoints();
    return breakpoints[line] === breakPointType[type];
  };

  /**
   * addBreakPoint
   * @param line lineNumber
   * @param type breakPoint type，1 disable breakPoint，2 normal breakPoint
   */
  const addBreakPoint = (line: number, type: 1 | 2) => {
    aceEditor.session.setBreakpoint(line, breakPointType[type]);
  };

  const removeBreakPoint = (line?: number) => {
    line ? aceEditor.session.clearBreakpoint(line) : aceEditor.session.clearBreakpoints();
  };

  const addCurrentPoint = (line: number) => {
    aceEditor.session.addGutterDecoration(line, 'current-breakpoints');
    const Range = ace.require('ace/range').Range;
    currentPointData.line = line;
    currentPointData.lineMarkerId = aceEditor.session.addMarker(
      new Range(line, 0, line, 1),
      'current-hightline',
      'fullLine',
    );
  };
  const removeCurrentPoint = () => {
    aceEditor.session.removeMarker(currentPointData.lineMarkerId);
    aceEditor.session.removeGutterDecoration(currentPointData.line, 'current-breakpoints');
  };

  // shortcut: setCurrentBreakPoint
  const setCurrentBreakPoint = (line: number) => {
    removeCurrentPoint();
    addCurrentPoint(line);
  };
  // shortcut: removeCurrentBreakPoint
  const removeCurrentBreakPoint = () => {
    removeCurrentPoint();
  };

  // shortcut: get all line (enable)breakpoints, line number start with 1.
  const getAllLineDecorations = () => {
    let lines: number[] = [];
    let breakpoints = aceEditor.session.getBreakpoints();
    breakpoints.forEach((item, index) => {
      if (item === breakPointType[2]) {
        lines.push(index + 1);
      }
    });
    return lines;
  };

  // shortcut: clear all disable-breakPoint
  const removeAllDiasbledBreakPoint = () => {
    let breakpoints = aceEditor.session.getBreakpoints();
    breakpoints.forEach((item, index) => {
      if (item === breakPointType[1]) {
        removeBreakPoint(index);
      }
    });
  };

  const formatCode = () => {
    const code = aceEditor.getValue();
    const formatOptions: Partial<FormatOptions> = {
      language: 'postgresql',
    };
    aceEditor.setValue(sqlFormatter(code, formatOptions), 1);
    removeBreakPoint();
  };

  const setAnnotations = (annotations: Annotations[]) => {
    const dueAnnotations = annotations.map((item) => {
      return {
        column: undefined,
        type: 'error',
        ...item,
      };
    });
    aceEditor.getSession().setAnnotations(dueAnnotations);
  };

  const getAnnotations = () => {
    return aceEditor.getSession().getAnnotations();
  };

  const clearAnnotations = () => {
    aceEditor.getSession().clearAnnotations();
  };

  const handleCopy = () => {
    const str = getAllSelectionValue() || getValue();
    copyToClickBoard(str);
  };

  // Only fill in keywords, but the color will not change
  const setCompleteData = (data) => {
    const languageTools = ace.require('ace/ext/language_tools');
    languageTools.addCompleter({
      getCompletions: function (editor, session, pos, prefix, callback) {
        if (prefix.length == 0) {
          return callback(null, []);
        } else {
          return callback(null, data);
        }
      }
    });
  }

  const setHighlightRules = (newRulesData) => {
    const session = aceEditor.getSession();
    session.setMode('ace/mode/sql', function () {
      const rules = session.$mode.$highlightRules.getRules();
      newRulesData.forEach((newRuleItem) => {
        if (!rules.start.find((rule) => rule.regex == newRuleItem.regex)) {
          rules.start.unshift(newRuleItem);
        }
      });
      session.$mode.$tokenizer = null;
      session.bgTokenizer.setTokenizer(session.$mode.getTokenizer());
      session.bgTokenizer.start(0);
    });
  }

  onMounted(() => {
    initEditor();
    window.addEventListener('resize', resize);
    aceEditor.container.addEventListener('contextmenu', (e) => showContextMenu(e));
    aceEditor.on('mousewheel', hideContextMenu);
  });
  onUnmounted(() => {
    window.removeEventListener('resize', resize);
    aceEditor.container.removeEventListener('contextmenu', (e) => showContextMenu(e));
    aceEditor.off('mousewheel', hideContextMenu);
    if (aceEditor) {
      aceEditor.destroy();
      aceEditor.container.remove();
    }
  });
  defineExpose({
    getValue,
    setValue,
    setEditorFocus,
    getSelectionValue,
    getAllSelectionValue,
    getAllLineDecorations,
    addBreakPoint,
    removeBreakPoint,
    removeCurrentBreakPoint,
    removeAllDiasbledBreakPoint,
    setCurrentBreakPoint,
    formatCode,
    setAnnotations,
    getAnnotations,
    clearAnnotations,
    setCompleteData,
    setHighlightRules,
  });
</script>

<style scoped lang="scss">
  @mixin common-dot {
    content: '';
    position: absolute;
    width: 10px;
    height: 10px;
    left: 1px;
    top: 3px;
    border-radius: 50%;
  }
  :deep(.ace_gutter-cell.breakpoints) {
    &::before {
      @include common-dot;
      background: #ad4d2c;
    }
  }
  :deep(.ace_gutter-cell.disable-breakpoints) {
    &::before {
      @include common-dot;
      background: #a0a0a0;
    }
  }
  :deep(.ace_gutter-cell.breakpoints-fake) {
    &::before {
      @include common-dot;
      background: rgba(255, 0, 0, 0.2);
    }
  }
  :deep(.current-breakpoints) {
    &::after {
      @include common-dot;
      background-image: url('../assets/current-pointer3.png');
      background-repeat: no-repeat;
      background-size: contain;
      background-color: transparent;
      z-index: 9;
      &.ace_error::before {
        display: none;
      }
    }
  }
  :deep(.current-hightline) {
    position: absolute;
    background-color: rgba(255, 255, 0, 0.7);
    z-index: 20;
  }
  :deep(.ace_gutter-cell.ace_error) {
    background-size: 14px 14px;
    background-position: 11px center;
  }
</style>
<style lang="scss">
  .ace-wrapper {
    position: relative;
    border: 1px solid #ddd;
  }
  .ace-editor.ace-chrome {
    .ace_gutter {
      background-color: #fff;
      border-right: 1px solid #ccc;
    }
  }
</style>
