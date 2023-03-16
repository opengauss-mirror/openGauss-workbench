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
        </div>
      </div>
    </template>
  </div>
</template>

<script lang="ts" setup>
  import { computed } from 'vue';
  import { useI18n } from 'vue-i18n';

  const { t } = useI18n();
  const props = withDefaults(
    defineProps<{
      type: 'sql' | 'debug' | 'debugChild';
      status: Record<string, boolean>;
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
    breakPointStep: false,
    singleStep: false,
  */
  const emit = defineEmits<{
    (e: 'execute'): void;
    (e: 'stopRun'): void;
    (e: 'clear'): void;
    (e: 'startDebug'): void;
    (e: 'stopDebug'): void;
    (e: 'breakPointStep'): void;
    (e: 'singleStep'): void;
    (e: 'stepIn'): void;
    (e: 'stepOut'): void;
    (e: 'format'): void;
  }>();

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

  const handleBreakPointStep = () => {
    emit('breakPointStep');
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

  const displayButton = computed(() => {
    return {
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
      breakPointStep: {
        name: t('functionBar.breakPointStep'),
        show: ['debug', 'debugChild'].includes(props.type),
        enabled: props.isGlobalEnable && props.status.breakPointStep,
        on: handleBreakPointStep,
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
