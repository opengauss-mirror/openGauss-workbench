<template>
  <div class="functions-wrapper">
    <div class="function-item-wrapper" v-if="barType.execute">
      <div
        :class="['function-item', { disabled: !barStatus.execute }]"
        @click="barStatus.execute && handleExecute()"
      >
        <font-icon :icon="'run' + (barStatus.execute ? '' : '-disabled')" />
        <span class="funcion-name">{{ $t('functionBar.execute') }}</span>
      </div>
    </div>
    <div class="function-item-wrapper" v-if="barType.stop">
      <div
        :class="['function-item', { disabled: !barStatus.stop }]"
        @click="barStatus.stop && handleStop()"
      >
        <font-icon :icon="'pause' + (barStatus.stop ? '' : '-disabled')" />
        <span class="funcion-name">{{ $t('functionBar.stop') }}</span>
      </div>
    </div>
    <div class="function-item-wrapper" v-if="barType.clear">
      <div
        :class="['function-item', { disabled: !barStatus.clear }]"
        @click="barStatus.clear && handleClear()"
      >
        <font-icon :icon="'delete' + (barStatus.clear ? '' : '-disabled')" />
        <span class="funcion-name">{{ $t('functionBar.clear') }}</span>
      </div>
    </div>
    <div class="function-item-wrapper" v-if="barType.startDebug">
      <div
        :class="['function-item', { disabled: !barStatus.startDebug }]"
        @click="barStatus.startDebug && handleStartDebug()"
      >
        <font-icon :icon="'debugopen' + (barStatus.startDebug ? '' : '-disabled')" />
        <span class="funcion-name">{{ $t('functionBar.startDebug') }}</span>
      </div>
    </div>
    <div class="function-item-wrapper" v-if="barType.stopDebug">
      <div
        :class="['function-item', { disabled: !barStatus.stopDebug }]"
        @click="barStatus.stopDebug && handleStopDebug()"
      >
        <font-icon :icon="'debugclose' + (barStatus.stopDebug ? '' : '-disabled')" />
        <span class="funcion-name">{{ $t('functionBar.stopDebug') }}</span>
      </div>
    </div>
    <div class="function-item-wrapper" v-if="barType.breakPointStep">
      <div
        :class="['function-item', { disabled: !barStatus.breakPointStep }]"
        @click="barStatus.breakPointStep && handleBreakPointStep()"
      >
        <font-icon :icon="'debugstep' + (barStatus.breakPointStep ? '' : '-disabled')" />
        <span class="funcion-name">{{ $t('functionBar.breakPointStep') }}</span>
      </div>
    </div>
    <div class="function-item-wrapper" v-if="barType.singleStep">
      <div
        :class="['function-item', { disabled: !barStatus.singleStep }]"
        @click="barStatus.singleStep && handleSingleStep()"
      >
        <font-icon :icon="'debugstepinto' + (barStatus.singleStep ? '' : '-disabled')" />
        <span class="funcion-name">{{ $t('functionBar.singleStep') }}</span>
      </div>
    </div>
    <div class="function-item-wrapper" v-if="barType.format">
      <div
        :class="['function-item', { disabled: props.readOnly }]"
        @click="!props.readOnly && handleFormat()"
      >
        <font-icon :icon="'formatter' + (!props.readOnly ? '' : '-disabled')" />
        <span class="funcion-name">{{ $t('functionBar.format') }}</span>
      </div>
    </div>
  </div>
</template>

<script lang="ts" setup>
  import { computed } from 'vue';

  const props = defineProps<{
    type: 'sql' | 'debug';
    status: Record<string, boolean>;
    readOnly?: boolean;
  }>();
  const emit = defineEmits<{
    (e: 'execute'): void;
    (e: 'stop'): void;
    (e: 'clear'): void;
    (e: 'startDebug'): void;
    (e: 'stopDebug'): void;
    (e: 'breakPointStep'): void;
    (e: 'singleStep'): void;
    (e: 'format'): void;
  }>();

  const barStatus = computed(() => {
    /* such as:
      execute: true,
      stop: false,
      startDebug: false,
      stopDebug: false,
      breakPointStep: false,
      singleStep: false,
    */
    return props.status;
  });
  const barType = computed(() => {
    return {
      execute: ['sql', 'debug'].includes(props.type),
      stop: ['sql'].includes(props.type),
      clear: ['sql'].includes(props.type),
      startDebug: ['debug'].includes(props.type),
      stopDebug: ['debug'].includes(props.type),
      breakPointStep: ['debug'].includes(props.type),
      singleStep: ['debug'].includes(props.type),
      format: ['sql'].includes(props.type),
    };
  });

  const handleExecute = () => {
    emit('execute');
  };

  const handleStop = () => {
    emit('stop');
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

  const handleFormat = () => {
    emit('format');
  };
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
      .funcion-name {
        font-size: 13px;
      }
      &:hover {
        .funcion-name {
          // background: #d8e5f3;
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
