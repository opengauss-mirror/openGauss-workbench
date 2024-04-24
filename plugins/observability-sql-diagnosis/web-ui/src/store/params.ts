///
/// Copyright (c) 2023 Huawei Technologies Co.,Ltd.
///

import { defineStore } from 'pinia';

export const useParamsStore = defineStore('params', {
  state() {
    return {
      isChartLinkage: false,
      chartId: '',
      dataIndex: null,
    }
  },
  actions: {
    setChartLinkage(isChartLinkage: boolean) {
      this.$state.isChartLinkage = isChartLinkage;
    },
    setDataIndex(dataIndex: number) {
      this.$state.dataIndex = dataIndex;
    },
    setChartId(chartId: string) {
      this.$state.chartId = chartId;
    }
  }
})