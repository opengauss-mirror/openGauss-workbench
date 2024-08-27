<template>
  <div class="table-toolbar">
    <div class="toolbar-left">
      <template v-for="(item, key) in commonButton" :key="key">
        <div
          :class="['tool-item-wrapper', item.className]"
          v-if="item.position == 'left' && item.show"
        >
          <EllipsisWithElTooltip v-if="item.type == 'text'" :toolTipProps="{ placement: 'top' }">
            {{ item.content }}
            <template #content>
              <div class="ellipsis-with-tooltip-content">{{ item.content }}</div>
            </template>
          </EllipsisWithElTooltip>
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
          <div v-if="item.type == 'pageSize'">
            <el-input-number
              :disabled="!item.enabled"
              :model-value="myPage.pageSize"
              :min="1"
              :max="10000"
              :precision="0"
              :controls="false"
              size="small"
              style="width: 50px"
              @change="handleChangePageSize"
            />
            / {{ $t('page.page') }}
          </div>
          <div class="total" v-if="item.type == 'dataSize'">{{ $t('page.dataSize') + ': ' + myPage.dataSize }}</div>
        </div>
      </template>
    </div>
    <div class="toolbar-right">
      <template v-for="(item, key) in commonButton" :key="key">
        <div class="tool-item-wrapper" v-if="item.position == 'right' && item.show">
          <div v-if="item.type == 'globalSize'">
            {{ $t('page.globalPageSize') }}: <el-input-number
              :disabled="!item.enabled"
              v-model="UserStore.globalPageSize"
              :min="1"
              :max="10000"
              :precision="0"
              :controls="false"
              size="small"
              style="width: 50px"
            />
            / {{ $t('page.page') }}
          </div>
          <div v-if="item.type == 'divider'" class="divider"></div>
        </div>
      </template>
    </div>
  </div>
</template>

<script lang="ts" setup>
  import { useI18n } from 'vue-i18n';
  import EllipsisWithElTooltip from '@/components/EllipsisWithEltooltip/index.vue';
  import { useUserStore } from '@/store/modules/user';

  interface Page {
    pageNum?: number;
    pageSize?: number;
    pageTotal?: number;
    dataSize?: number;
  }
  const { t } = useI18n();
  const UserStore = useUserStore();
  const props = withDefaults(
    defineProps<{
      page?: Page;
      sql?: string;
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
    (e: 'exportCurrentPage', params?: any): void;
    (e: 'exportAllPage', params?: any): void;
    (e: 'update:page', value: Page): void;
    (e: 'getData', value?: any): void;
  }>();

  const myPage = computed({
    get: () => props.page,
    set: (val) => {
      emit('update:page', val);
    },
  });

  const getData = () => {
    emit('getData', {
      pageNum: myPage.value.pageNum,
      pageSize: myPage.value.pageSize,
      sql: props.sql,
    });
  }

  const commonButton = computed(() => {
    return [
      {
        name: '',
        tips: t('table.data.exportCurrentPage'),
        type: 'button',
        position: 'left',
        show: true,
        enabled: true,
        on: () => emit('exportCurrentPage', {
          pageNum: myPage.value.pageNum,
          pageSize: myPage.value.pageSize,
          sql: props.sql,
        }),
        icon: 'icon-export',
        color: '#409eff',
        disabledColor: '#409eff40',
      },
      {
        name: '',
        tips: t('table.data.exportAllPage'),
        type: 'button',
        position: 'left',
        show: true,
        enabled: true,
        on: () => emit('exportAllPage', {
          sql: props.sql,
        }),
        icon: 'icon-daochusuoyou',
        color: '#409eff',
        disabledColor: '#409eff40',
      },
      {
        type: 'divider',
        position: 'left',
        show: true,
      },
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
            sql: props.sql,
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
            sql: props.sql,
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
            sql: props.sql,
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
            sql: props.sql,
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
      {
        type: 'divider',
        position: 'left',
        show: true,
      },
      {
        name: '',
        type: 'text',
        position: 'left',
        className: 'text',
        show: true,
        content: props.sql,
      },
      {
        type: 'divider',
        position: 'right',
        show: true,
      },
      {
        name: '',
        type: 'globalSize',
        position: 'right',
        show: true,
        enabled: true,
      },
    ];
  });

  const handleInputPageNum = (value) => {
    const result =  typeof value == 'number' ? value : value.replace(/[^0-9]/g, '');
    const pageNum = result == 0 ? myPage.value.pageNum : Math.min(myPage.value.pageTotal, Number(result));
    myPage.value.pageNum = pageNum;
  };
  const handleChangePageNum = (pageNum) => {
    const FinalPageNum = Math.min(myPage.value.pageTotal, pageNum);
    emit('getData', {
      pageNum: FinalPageNum,
      pageSize: myPage.value.pageSize,
      sql: props.sql,
    });
  };
  const handleChangePageSize = (pageSize) => {
    emit('getData', {
      pageNum: 1,
      pageSize,
      sql: props.sql,
    });
  };
</script>

<style lang="scss" scoped>
  @import url('@/styles/table-toolbar.scss');
  .table-toolbar {
    overflow: hidden;
    min-width: 600px;
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
</style>
