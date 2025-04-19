<template>
  <div class="module-c mb">
    <div class="flex-row mb">
      <div class="module-item mr">
        <div class="module-title">{{ $t('components.ModuleSummary.5mpim9s7gw80') }}</div>
        <div class="content">
          <div v-if="!isBaseopsInstalled">
            <el-tooltip :content="$t('components.ModuleSummary.else2')" placement="bottom" effect="light">
              <a-spin>
                <span class="number-size">--</span>
                <span class="number-unit">{{ $t('components.ModuleSummary.else1') }}</span>
              </a-spin>
            </el-tooltip>
          </div>
          <div v-else>
            <a-spin :loading="data.dataFlow.loading">
              <span class="number-size">{{ data.busiFlow.count }}</span>
              <span class="number-unit">{{ $t('components.ModuleSummary.else1') }}</span>
            </a-spin>
          </div>
          <svg-icon
            icon-class="ops-busi-flow"
            class="icon-size"
          ></svg-icon>
        </div>
      </div>
      <div class="module-item">
        <div class="module-title">{{ $t('components.ModuleSummary.5mpim9s7hqk0') }}</div>
        <div class="content">
          <div v-if="!isBaseopsInstalled">
            <el-tooltip :content="$t('components.ModuleSummary.else2')" placement="bottom" effect="light">
              <a-spin>
                <span class="number-size">--</span>
                <span class="number-unit">{{ $t('components.ModuleSummary.else1') }}</span>
              </a-spin>
            </el-tooltip>
          </div>
          <div v-else>
            <a-spin :loading="data.dataFlow.loading">
              <span class="number-size">{{ data.dataFlow.count }}</span>
              <span class="number-unit">{{ $t('components.ModuleSummary.else1') }}</span>
            </a-spin>
          </div>
          <svg-icon
            icon-class="ops-data-flow"
            class="icon-size"
          ></svg-icon>
        </div>
      </div>
    </div>
    <div class="flex-row">
      <div class="module-item mr">
        <div class="module-title">{{ $t('components.ModuleSummary.5mpim9s7hxk0') }}</div>
        <div class="content">
          <div v-if="!isBaseopsInstalled">
            <el-tooltip :content="$t('components.ModuleSummary.else2')" placement="bottom" effect="light">
              <a-spin>
                <span class="number-size">--</span>
                <span class="number-unit">{{ $t('components.ModuleSummary.else1') }}</span>
              </a-spin>
            </el-tooltip>
          </div>
          <div v-else>
            <a-spin :loading="data.dataFlow.loading">
              <span class="number-size">{{ data.dataModel.count }}</span>
              <span class="number-unit">{{ $t('components.ModuleSummary.else1') }}</span>
            </a-spin>
          </div>
          <svg-icon
            icon-class="ops-data-model"
            class="icon-size"
          ></svg-icon>
        </div>
      </div>
      <div class="module-item">
        <div class="module-title">{{ $t('components.ModuleSummary.5mpim9s7i7o0') }}</div>
        <div class="content">
          <a-spin
            n
            :loading="data.installedPlugin.loading"
          >
            <span class="number-size">{{ data.installedPlugin.count }}</span>
            <span class="number-unit">{{ $t('components.ModuleSummary.else1') }}</span>
          </a-spin>
          <svg-icon
            icon-class="ops-has-install-plugin"
            class="icon-size"
          ></svg-icon>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import dataFlowImg from '@/assets/images/ops/data-flow.png'
import dataModelingImg from '@/assets/images/ops/data-modeling.png'
import pluginImg from '@/assets/images/ops/plugin.png'
import busiFlowImg from '@/assets/images/ops/busi-flow.png'
import { onMounted, reactive, ref } from 'vue'
import { getDataFlowCount, getBusiFlowCount, getDataModelCount, getPluginCount, isBaseOpsStart  } from '@/api/ops'
import { KeyValue } from '@/types/global'
import { Message } from '@arco-design/web-vue'
import {unloadPluginsInfo} from "@/api/plugin";

reactive([dataFlowImg, dataModelingImg, pluginImg, busiFlowImg])

const data = reactive({
  busiFlow: {
    loading: false,
    count: 0
  },
  dataFlow: {
    loading: false,
    count: 0
  },
  dataModel: {
    loading: false,
    count: 0
  },
  installedPlugin: {
    loading: false,
    count: 0
  }
})

onMounted(() => {
  getPluginInstall()
  getInstallPluginCount()
})

const isBaseopsInstalled = ref<boolean>(true)
const getPluginInstall = () => {
  isBaseOpsStart().then((res: KeyValue) => {
    if (Number(res.code) === 200) {
      isBaseopsInstalled.value = res.data
    }
  }) .catch((error) => {
    console.log(error)
  }) .finally(() => {
    if (isBaseopsInstalled.value) {
      getBusiFlow()
      getDataFlow()
      getDataModel()
    } else {
      data.busiFlow.loading = true
      data.dataFlow.loading = true
      data.dataModel.loading = true
    }
  })
}

const getBusiFlow = () => {
  data.busiFlow.loading = true
  getBusiFlowCount().then((res: KeyValue) => {
    if (Number(res.code) === 200) {
      data.busiFlow.count = res.count
    } else {
      Message.error('Failed to obtain the number of business flows')
    }
  }).finally(() => {
    data.busiFlow.loading = false
  })
}

const getDataFlow = () => {
  data.dataFlow.loading = true
  getDataFlowCount().then((res: KeyValue) => {
    if (Number(res.code) === 200) {
      data.dataFlow.count = res.count
    } else {
      Message.error('Failed to get the number of data streams')
    }
  }).finally(() => {
    data.dataFlow.loading = false
  })
}

const getDataModel = () => {
  data.dataModel.loading = true
  getDataModelCount().then((res: KeyValue) => {
    if (Number(res.code) === 200) {
      data.dataModel.count = res.count
    } else {
      Message.error('Failed to get the number of data models')
    }
  }).finally(() => {
    data.dataModel.loading = false
  })
}

const getInstallPluginCount = () => {
  data.installedPlugin.loading = true
  getPluginCount().then((res: KeyValue) => {
    if (Number(res.code) === 200) {
      data.installedPlugin.count = res.data
    } else {
      Message.error('Failed to get the number of plugins')
    }
  }).finally(() => {
    data.installedPlugin.loading = false
  })
}

</script>

<style lang="less" scoped>
.module-c {
  .icon-size {
    width: 40px;
    height: 40px;
  }

  .module-item {
    background-color: var(--color-bg-2);
    width: 50%;
    padding: 15px;
    display: flex;
    flex-direction: column;
    justify-content: flex-start;
    border-radius: 2px;

    .module-title {
      font-size: 16px;
      color: var(--color-text-1);
      margin-bottom: 5px;
    }

    .content {
      display: flex;
      justify-content: space-between;
      align-items: flex-end;

      .number-size {
        font-size: 28px;
        color: var(--color-text-1);
      }

      .number-unit {
        font-size: 14px;
        color: var(--color-text-1);
      }
    }
  }
}
</style>
