<template>
  <div class="table-toolbar">
    <div class="toolbar-left">
      <template v-for="(item, key) in commonButton" :key="key">
        <div class="tool-item-wrapper" v-if="item.position == 'left' && item.show">
          <div
            v-if="item.type == 'button'"
            :class="['tool-item', { disabled: !item.enabled }]"
            @click="item.enabled && item.on()"
          >
            <span
              :class="['iconfont', item.icon]"
              :style="{
                color: item.enabled ? item.color : item.disabledColor,
              }"
            ></span>
            <span class="tool-name">{{ item.name }}</span>
          </div>
          <div v-if="item.type == 'divider'" class="divider"></div>
          <el-input
            v-if="item.type == 'pageNum'"
            :disabled="!item.enabled"
            :model-value="pageNum"
            @input="handlePageNumInput"
            @change="handlePageNumChange"
            size="small"
            style="width: 50px"
            input-style="text-align: center;"
          />
          <el-select
            v-if="item.type == 'pageSize'"
            :model-value="pageSize"
            @change="changePageSize"
            :disabled="!item.enabled"
            size="small"
            style="width: 90px"
          >
            <el-option :label="`100/${t('common.page')}`" :value="100" />
            <el-option :label="`300/${t('common.page')}`" :value="300" />
            <el-option :label="`500/${t('common.page')}`" :value="500" />
          </el-select>
        </div>
      </template>
    </div>
  </div>
</template>

<script lang="ts" setup>
  import { useI18n } from 'vue-i18n';

  const { t } = useI18n();
  const props = withDefaults(
    defineProps<{
      type: 'common' | 'table';
      status: Record<string, boolean>;
      pageNum?: number;
      pageSize?: number;
      pageTotal?: number;
      sort?: boolean;
      filter?: boolean;
    }>(),
    {},
  );
  const emit = defineEmits<{
    (e: 'firstPage'): void;
    (e: 'lastPage'): void;
    (e: 'previousPage'): void;
    (e: 'nextPage'): void;
    (e: 'changePageNum', value: number): void;
    (e: 'update:pageNum', value: number): void;
    (e: 'update:pageSize', value: number): void;
    (e: 'update:pageTotal', value: number): void;
    (e: 'update:sort', value: boolean): void;
    (e: 'update:filter', value: boolean): void;
  }>();

  const commonButton = computed(() => {
    return [
      {
        name: '',
        type: 'button',
        position: 'left',
        show: ['table'].includes(props.type),
        enabled: props.pageNum > 1 && props.status.firstPage,
        on: () => emit('firstPage'),
        icon: 'icon-page-first-line',
        color: '#409eff',
        disabledColor: '#409eff40',
      },
      {
        name: '',
        type: 'button',
        position: 'left',
        show: ['table'].includes(props.type),
        enabled: props.pageNum > 1 && props.status.previousPage,
        on: () => emit('previousPage'),
        icon: 'icon-page-previous',
        color: '#409eff',
        disabledColor: '#409eff40',
      },
      {
        name: '',
        type: 'pageNum',
        position: 'left',
        show: ['table'].includes(props.type),
        enabled: props.status.pageNum,
      },
      {
        name: '',
        type: 'button',
        position: 'left',
        show: ['table'].includes(props.type),
        enabled: props.pageTotal && props.pageNum < props.pageTotal && props.status.nextPage,
        on: () => emit('nextPage'),
        icon: 'icon-page-next',
        color: '#409eff',
        disabledColor: '#409eff40',
      },
      {
        name: '',
        type: 'button',
        position: 'left',
        show: ['table'].includes(props.type),
        enabled: props.pageTotal && props.pageNum < props.pageTotal && props.status.lastPage,
        on: () => emit('lastPage'),
        icon: 'icon-page-last-line',
        color: '#409eff',
        disabledColor: '#409eff40',
      },
      {
        name: '',
        type: 'pageSize',
        position: 'left',
        show: ['table'].includes(props.type),
        enabled: props.status.pageSize,
      },
    ];
  });

  // Trigger in input. Maybe many times
  const handlePageNumInput = (value) => {
    const result = value.replace(/[^0-9]/g, '');
    const pageNum = result == 0 ? props.pageNum : Math.min(props.pageTotal, Number(result));
    emit('update:pageNum', pageNum);
  };

  // Trigger in onBlur
  const handlePageNumChange = (pageNum) => {
    handlePageNumInput(pageNum);
    emit('changePageNum', pageNum);
  };
  const changePageSize = (value) => {
    emit('update:pageSize', value);
  };
</script>

<style lang="scss" scoped src="@/styles/table-toolbar.scss"></style>
