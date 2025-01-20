<template>
  <div class="table-toolbar">
    <div class="toolbar-left">
      <template v-for="(item, key) in commonButton" :key="key">
        <div
          :class="['tool-item-wrapper', item.className]"
          v-if="item.position == 'left' && item.show"
        >
          <div
            v-if="item.type == 'button'"
            :class="['tool-item', { disabled: !item.enabled }]"
            :title="item.tips"
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
          <el-input-number
            v-if="item.type == 'pageNum'"
            :disabled="!item.enabled"
            :model-value="page.pageNum"
            :min="1"
            :max="page.pageTotal"
            :precision="0"
            :controls="false"
            size="small"
            style="width: 50px"
            @change="handleChangePageNum"
          />
          <el-select
            v-if="item.type == 'pageSize'"
            :disabled="!item.enabled"
            :model-value="myPage.pageSize"
            @change="handleChangePageSize"
            size="small"
            style="width: 90px"
          >
            <el-option :label="`10/${t('common.page')}`" :value="10" />
            <el-option :label="`30/${t('common.page')}`" :value="30" />
            <el-option :label="`50/${t('common.page')}`" :value="50" />
          </el-select>
          <div class="total" v-if="item.type == 'dataSize'">{{
            $t('page.dataSize') + ': ' + myPage.dataSize
          }}</div>
        </div>
      </template>
    </div>
  </div>
</template>

<script lang="ts" setup>
  import { useI18n } from 'vue-i18n';

  interface Page {
    pageNum?: number;
    pageSize?: number;
    pageTotal?: number;
    dataSize?: number;
  }
  const { t } = useI18n();
  const props = withDefaults(
    defineProps<{
      page?: Page;
    }>(),
    {
      page: () => ({
        pageNum: 1,
        pageSize: 500,
        pageTotal: 0,
      }),
    },
  );
  const emit = defineEmits<{
    (e: 'update:page', value: Page): void;
    (e: 'getData', value?: any): void;
  }>();

  const myPage = computed({
    get: () => props.page,
    set: (val) => {
      emit('update:page', val);
    },
  });

  const commonButton = computed(() => {
    return [
      {
        name: '',
        type: 'button',
        position: 'left',
        show: true,
        enabled: myPage.value.pageNum > 1,
        on: () => {
          emit('getData', {
            pageNum: 1,
            pageSize: myPage.value.pageSize,
          });
        },
        icon: 'icon-page-first-line',
        color: '#409eff',
        disabledColor: '#409eff40',
      },
      {
        name: '',
        type: 'button',
        position: 'left',
        show: true,
        enabled: myPage.value.pageNum > 1,
        on: () => {
          emit('getData', {
            pageNum: myPage.value.pageNum - 1,
            pageSize: myPage.value.pageSize,
          });
        },
        icon: 'icon-page-previous',
        color: '#409eff',
        disabledColor: '#409eff40',
      },
      {
        name: '',
        type: 'pageNum',
        position: 'left',
        show: true,
        enabled: true,
      },
      {
        name: '',
        type: 'button',
        position: 'left',
        show: true,
        enabled: myPage.value.pageTotal && myPage.value.pageNum < myPage.value.pageTotal,
        on: () => {
          emit('getData', {
            pageNum: myPage.value.pageNum + 1,
            pageSize: myPage.value.pageSize,
          });
        },
        icon: 'icon-page-next',
        color: '#409eff',
        disabledColor: '#409eff40',
      },
      {
        name: '',
        type: 'button',
        position: 'left',
        show: true,
        enabled: myPage.value.pageTotal && myPage.value.pageNum < myPage.value.pageTotal,
        on: () => {
          emit('getData', {
            pageNum: myPage.value.pageTotal,
            pageSize: myPage.value.pageSize,
          });
        },
        icon: 'icon-page-last-line',
        color: '#409eff',
        disabledColor: '#409eff40',
      },
      {
        name: '',
        type: 'pageSize',
        position: 'left',
        show: true,
        enabled: true,
      },
      {
        name: '',
        type: 'dataSize',
        position: 'left',
        show: true,
      },
    ];
  });
  const handleChangePageNum = (pageNum) => {
    const FinalPageNum = Math.min(myPage.value.pageTotal, pageNum);
    emit('getData', {
      pageNum: FinalPageNum,
      pageSize: myPage.value.pageSize,
    });
  };
  const handleChangePageSize = (pageSize) => {
    emit('getData', {
      pageNum: 1,
      pageSize,
    });
  };
</script>

<style lang="scss" scoped>
  @import url('@/styles/table-toolbar.scss');
  .table-toolbar {
    overflow: hidden;
  }
  .toolbar-left {
    flex: 1;
    flex-wrap: nowrap !important;
    overflow: hidden;
  }
  .toolbar-right {
    flex-wrap: nowrap;
    flex-shrink: 0;
  }
  .tool-item-wrapper.text {
    flex: 1;
    overflow: hidden;
  }
  .text-content {
    width: 100%;
  }
  .ellipsis-with-tooltip-content {
    max-width: 500px;
    max-height: 700px;
    overflow: auto;
  }
  .total {
    white-space: nowrap;
  }
</style>
