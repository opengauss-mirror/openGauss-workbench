<template>
  <div class="common-oper-c mb">
    <div class="flex-col-start">
      <div class="ft-lg title">{{ $t('components.CommonOper.5mpimo1efn00') }}</div>
      <div class="content-c">
        <div v-for="(item, index) in data.operationList" :key="index">
          <div class="oper-item" @click="$router.push({ name: item.routeName })">
            {{ item.name }}
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">

import { onMounted, reactive } from 'vue'
import { KeyValue } from '@/types/global'
import { useI18n } from 'vue-i18n'
const { t } = useI18n()
const data: {
  operationList: KeyValue
} = reactive({
  operationList: []
})

onMounted(() => {
  const operArr = getOperList()
  data.operationList = []
  data.operationList = [...operArr]
})

const getOperList = () => {
  return [{
    name: t('components.CommonOper.5mpimo1egl80'),
    routeName: 'ResourcePhysical'
  }, {
    name: t('components.CommonOper.else1'),
    routeName: 'ResourceAz'
  }, {
    name: t('components.CommonOper.5mpimo1egv80'),
    routeName: 'Static-pluginBase-opsModelingDataflow'
  }, {
    name: t('components.CommonOper.5mpimo1eh8o0'),
    routeName: 'Static-pluginBase-opsMonitorLogCenter'
  }, {
    name: t('components.CommonOper.5mpimo1ehfw0'),
    routeName: 'Static-pluginBase-opsMonitorBasic'
  }]
}
</script>

<style lang="less" scoped>
.common-oper-c {
  background-color: var(--color-bg-2);
  padding: 15px;
  border-radius: 2px;
  .title {
    font-size: 16px;
    color: var(--color-text-1);
    margin-bottom: 5px;
  }
  .content-c {
    display: flex;
    flex-wrap: wrap;

    .oper-item {
      cursor: pointer;
      padding: 13px;
      min-width: 94px;
      font-size: 14px;
      color: var(--color-text-2);
      background-color: var(--color-fill-2);
      border-radius: 2px;
      margin-right: 16px;
      margin-top: 8px;
      display: flex;
      justify-content: center;
    }
  }
}
</style>
