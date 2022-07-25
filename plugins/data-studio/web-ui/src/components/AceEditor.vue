<!-- this component line number start with 0. -->
<template>
  <div class="ace-editor" :id="id" ref="aceRef" :style="{ height: $props.height }"></div>
</template>

<script lang="ts" setup>
  import { reactive, ref, onMounted, nextTick, watch, markRaw, onUnmounted } from 'vue';
  import ace from 'ace-builds';
  import 'ace-builds/src-noconflict/theme-chrome';
  import 'ace-builds/src-noconflict/theme-monokai';
  import 'ace-builds/src-noconflict/mode-sql';
  import 'ace-builds/src-noconflict/snippets/sql';
  import 'ace-builds/src-noconflict/ext-language_tools';
  import { format as sqlFormatter } from 'sql-formatter';
  import type { FormatOptions } from 'sql-formatter';
  import { uuid } from '@/utils';
  import { isDark } from '@/hooks/dark';

  const props = withDefaults(
    defineProps<{
      modelValue?: string;
      value?: string;
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

  watch(
    () => props.modelValue,
    (newProps) => {
      aceEditor.setValue(newProps, 1);
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
      }),
    );
    aceEditor.setOptions({
      enableBasicAutocompletion: true,
      enableSnippets: true,
      enableLiveAutocompletion: true,
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
    aceEditor.setHighlightActiveLine(false);
    aceEditor.setHighlightGutterLine(false);

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
      e.stop();
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
      if (e.clientX > 15 + target.getBoundingClientRect().left) {
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

  onMounted(() => {
    initEditor();
    window.addEventListener('resize', resize);
  });
  onUnmounted(() => {
    window.removeEventListener('resize', resize);
    if (aceEditor) {
      aceEditor.destroy();
      aceEditor.container.remove();
    }
  });
  defineExpose({
    getValue,
    setValue,
    getSelectionValue,
    getAllLineDecorations,
    addBreakPoint,
    removeBreakPoint,
    removeCurrentBreakPoint,
    removeAllDiasbledBreakPoint,
    setCurrentBreakPoint,
    formatCode,
  });
</script>

<style scoped lang="scss">
  @mixin common-dot {
    content: '';
    position: absolute;
    width: 12px;
    height: 12px;
    left: 2px;
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
    }
  }
  :deep(.current-hightline) {
    position: absolute;
    background-color: rgba(255, 255, 0, 0.7);
    z-index: 20;
  }
</style>
<style lang="scss">
  .ace-editor.ace-chrome {
    border: 1px solid #ccc;
    .ace_gutter {
      background-color: #fff;
      border-right: 1px solid #ccc;
    }
  }
</style>
