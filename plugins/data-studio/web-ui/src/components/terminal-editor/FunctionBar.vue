<template>
  <div class="functions-wrapper">
    <template v-for="(item, key) in displayButton" :key="key">
      <div class="function-item-wrapper" v-if="item.show">
        <div
          :class="['function-item', { disabled: !item.enabled }]"
          @click="item.enabled && item.on()"
        >
          <font-icon :icon="item.enabled ? item.icon : item.disabledIcon" />
          <span class="funcion-name">{{ item.name }}</span>
          <div
            class="suffix-icon"
            v-if="key == 'coverageRate' && props.options?.coverageRate?.warningText"
            :title="
              props.options.coverageRate.isI18n
                ? $t('message.noOpenCoverageRate')
                : props.options.coverageRate.warningText
            "
          >
            <font-icon icon="Warning"></font-icon>
          </div>
        </div>
      </div>
    </template>
    <div>
      <input
        v-if="displayButton.importFile?.show"
        type="file"
        id="uploadInput"
        ref="uploadInput"
        accept=".sql"
        style="display: none"
      />
    </div>
  </div>
</template>

<script lang="ts" setup>
  import { ElMessage } from 'element-plus';
  import { useI18n } from 'vue-i18n';

  const { t } = useI18n();
  const props = withDefaults(
    defineProps<{
      type: 'sql' | 'debug' | 'debugChild';
      status: Record<string, boolean>;
      options?: Record<string, any>;
      editorReadOnly?: boolean;
      isGlobalEnable?: boolean;
    }>(),
    {
      isGlobalEnable: true,
    },
  );
  /* props.status such as:
    execute: true,
    stop: false,
    startDebug: false,
    stopDebug: false,
    continueStep: false,
    singleStep: false,
  */
  const emit = defineEmits<{
    (e: 'compile'): void;
    (e: 'execute'): void;
    (e: 'stopRun'): void;
    (e: 'clear'): void;
    (e: 'startDebug'): void;
    (e: 'stopDebug'): void;
    (e: 'continueStep'): void;
    (e: 'singleStep'): void;
    (e: 'stepIn'): void;
    (e: 'stepOut'): void;
    (e: 'format'): void;
    (e: 'coverageRate'): void;
    (e: 'importFile', value: string): void;
    (e: 'exportFile'): void;
    (e: 'showHistory'): void;
  }>();

  const platform = inject<Ref<Platform>>('platform');  
  const uploadInput = ref<HTMLInputElement>(null);

  const handleCompile = () => {
    emit('compile');
  };

  const handleExecute = () => {
    emit('execute');
  };

  const handleStop = () => {
    emit('stopRun');
  };

  const handleClear = () => {
    emit('clear');
  };

  const handleStartDebug = () => {
    emit('startDebug');
  };

  const handleStopDebug = () => {
    emit('stopDebug');
  };

  const handleContinueStep = () => {
    emit('continueStep');
  };

  const handleSingleStep = () => {
    emit('singleStep');
  };
  const handleStepInto = () => {
    emit('stepIn');
  };
  const handleStepOut = () => {
    emit('stepOut');
  };

  const handleFormat = () => {
    emit('format');
  };
  const handleCoverageRate = () => {
    emit('coverageRate');
  };
  const handleImportFile = () => {
    const uploadInputElement = uploadInput.value;
    uploadInputElement.click();
    uploadInputElement.onchange = (event: Event) => {
      const target = event.target as HTMLInputElement;
      const file = target.files[0];
      if (file && !/\.sql$/i.test(file.name)) {
        ElMessage.error(t('message.importSqlSuffixName'));
        target.value = '';
        return;
      }
      if (file && /\.sql$/i.test(file.name)) {
        const reader = new FileReader();
        reader.readAsText(file);
        reader.onload = () => {
          const result = reader.result as string;
          emit('importFile', result);
          target.value = '';
        };
      }
    };
  };
  const handleExportFile = () => {
    emit('exportFile');
  };
  const displayButton = computed(() => {
    return {
      compile: {
        name: t('functionBar.compile'),
        show: ['debug'].includes(props.type),
        enabled: props.isGlobalEnable && props.status.compile,
        on: handleCompile,
        icon: 'compile',
        disabledIcon: 'compile-disabled',
      },
      execute: {
        name: t('functionBar.execute'),
        show: ['sql', 'debug'].includes(props.type),
        enabled: props.isGlobalEnable && props.status.execute,
        on: handleExecute,
        icon: 'run',
        disabledIcon: 'run-disabled',
      },
      stopRun: {
        name: t('functionBar.stopRun'),
        show: ['sql'].includes(props.type),
        enabled: props.isGlobalEnable && props.status.stopRun,
        on: handleStop,
        icon: 'pause',
        disabledIcon: 'pause-disabled',
      },
      clear: {
        name: t('functionBar.clear'),
        show: ['sql'].includes(props.type),
        enabled: props.status.clear,
        on: handleClear,
        icon: 'delete',
        disabledIcon: 'delete-disabled',
      },
      startDebug: {
        name: t('functionBar.startDebug'),
        show: ['debug'].includes(props.type),
        enabled: props.isGlobalEnable && props.status.startDebug,
        on: handleStartDebug,
        icon: 'debugopen',
        disabledIcon: 'debugopen-disabled',
      },
      stopDebug: {
        name: t('functionBar.stopDebug'),
        show: ['debug'].includes(props.type),
        enabled: props.isGlobalEnable && props.status.stopDebug,
        on: handleStopDebug,
        icon: 'debugclose',
        disabledIcon: 'debugclose-disabled',
      },
      continueStep: {
        name: t('functionBar.continueStep'),
        show: ['debug', 'debugChild'].includes(props.type),
        enabled: props.isGlobalEnable && props.status.continueStep,
        on: handleContinueStep,
        icon: 'debugstep',
        disabledIcon: 'debugstep-disabled',
      },
      singleStep: {
        name: t('functionBar.singleStep'),
        show: ['debug', 'debugChild'].includes(props.type),
        enabled: props.isGlobalEnable && props.status.singleStep,
        on: handleSingleStep,
        icon: 'debugsinglestep',
        disabledIcon: 'debugsinglestep-disabled',
      },
      stepIn: {
        name: t('functionBar.stepIn'),
        show: ['debug', 'debugChild'].includes(props.type),
        enabled: props.isGlobalEnable && props.status.stepIn,
        on: handleStepInto,
        icon: 'debugstepinto',
        disabledIcon: 'debugstepinto-disabled',
      },
      stepOut: {
        name: t('functionBar.stepOut'),
        show: ['debug', 'debugChild'].includes(props.type),
        enabled: props.isGlobalEnable && props.status.stepOut,
        on: handleStepOut,
        icon: 'debugstepout',
        disabledIcon: 'debugstepout-disabled',
      },
      format: {
        name: t('functionBar.format'),
        show: ['sql'].includes(props.type),
        enabled: !props.editorReadOnly,
        on: handleFormat,
        icon: 'formatter',
        disabledIcon: 'formatter-disabled',
      },
      coverageRate: {
        name: t('functionBar.coverageRate'),
        show: ['debug'].includes(props.type),
        enabled: props.isGlobalEnable && props.status.coverageRate,
        on: handleCoverageRate,
        icon: 'report',
        disabledIcon: 'report-disabled',
      },
      importFile: {
        name: t('button.import'),
        show: ['sql'].includes(props.type),
        enabled: true,
        on: handleImportFile,
        icon: 'import',
        disabledIcon: 'import',
      },
      exportFile: {
        name: t('button.export'),
        show: ['sql'].includes(props.type),
        enabled: true,
        on: handleExportFile,
        icon: 'daochu',
        disabledIcon: 'daochu',
      },
    };
  });
</script>

<style lang="scss" scoped>
  .functions-wrapper {
    flex-shrink: 0;
    padding: 5px 0;
    background: var(--el-bg-color-bar);
    .function-item-wrapper {
      display: inline-block;
      padding: 0 8px;
      &:nth-child(n + 2) {
        border-left: 1px solid #d4cfcf;
      }
    }
    .function-item {
      cursor: pointer;
      position: relative;
      user-select: none;
      .font-icon {
        font-size: 15px;
        margin-right: 4px;
      }
      .suffix-icon {
        display: inline-block;
        vertical-align: middle;
        .font-icon {
          font-size: 20px;
        }
      }
      &:hover {
        .funcion-name {
          background: var(--el-fill-color-light);
        }
      }
      &.disabled {
        cursor: not-allowed;
        color: #b5b8bd;
        &:hover {
          .funcion-name {
            background: inherit;
          }
        }
      }
    }
  }
</style>
