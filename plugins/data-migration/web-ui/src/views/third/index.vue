<template>
  <div class="app-container common-layout" id="migrationThird">
    <div class="search-con">
      <el-form :model="form" class="input-wrapper">
        <el-form-item field="kafkaIp">
          <el-input v-model="form.kafkaIp" maxlength="50" allow-clear :placeholder="$t('third.index.5q08thptsw88')"
            class="o-style-search" @change="getList" :prefix-icon="IconSearch" @search="getList"></el-input>
          <div class="inputMaxWarning" v-if="(form.kafkaIp + '').length >= 50">{{ t('components.FusionSearch.inputMaxWarnText') }}</div>
        </el-form-item>
      </el-form>
      <div class="button-wrapper">
        <el-button class="o-button--icon" :icon="IconRefresh" @click="resetQuery"></el-button>
      </div>
    </div>
    <div class="table-con">
      <el-table v-loading="loading" row-key="id" :data="tableData" :bordered="false">
        <template #empty>
          <div class="o-table-empty mt24">
            <el-icon class="o-empty-icon">
              <IconEmpty />
            </el-icon>
            <div class="o-empty-label">
              {{ $t('third.index.noData') }}
            </div>
          </div>
        </template>
        <el-table-column :label="$t('third.index.5q08thptsw81')" prop="kafkaIp">
          <template #default="{ row }">
            {{ row.kafkaIp || '--' }}
          </template>
        </el-table-column>
        <el-table-column :label="$t('third.index.5q08thptsw84')" prop="kafkaPort">
          <template #default="{ row }">
            {{ row.kafkaPort || '--' }}
          </template>
        </el-table-column>
        <el-table-column :label="$t('third.index.5q08thptsw85')" prop="zookeeperPort">
          <template #default="{ row }">
            {{ row.zookeeperPort || '--' }}
          </template>
        </el-table-column>
        <el-table-column :label="$t('third.index.5q08thptsw86')" prop="schemaRegistryPort">
          <template #default="{ row }">
            {{ row.schemaRegistryPort || '--' }}
          </template>
        </el-table-column>
        <el-table-column :label="$t('third.index.5q08thptsw89')" prop="installDir">
          <template #default="{ row }">
            {{ row.installDir || '--' }}
          </template>
        </el-table-column>
        <el-table-column :label="$t('third.index.5q08thptsw87')" prop="bindPortalHost">
          <template #default="{ row }">
            {{ row.bindPortalHost || '--' }}
          </template>
        </el-table-column>
      </el-table>
      <el-pagination :total="total" @change="getList" :layout="layout" v-model:current-page="pagination.pageNum"
        v-model:page-size="pagination.pageSize"></el-pagination>
    </div>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref, onMounted } from 'vue'
import { listKafkaInstance } from '@/api/task'
import { PageType } from '@/types/global'
import { useI18n } from 'vue-i18n'
import { IconSearch, IconRefresh, IconEmpty } from '@computing/opendesign-icons'

const { t } = useI18n()

const resetQuery = () => {
  form.kafkaIp = undefined
  getList()
}

const form = reactive({
  softwareName: undefined,
  kafkaIp: undefined,
  kafkaPort: undefined,
  zkIp: undefined,
  zookeeperPort: undefined,
  schemaRegistryIp: undefined,
  schemaRegistryPort: undefined,
  installDir: undefined,
  bindPortalHost: undefined
})
const layout = 'total, sizes, prev, pager, next, jumper'
const total = ref(0)
const loading = ref(false)
const pagination = reactive({
  pageNum: 1,
  pageSize: 10
})

const tableData = ref<any[]>([])

const getList = async () => {
  try {
    loading.value = true;
    const res:PageType = await listKafkaInstance({
      kafkaIp: form.kafkaIp,
      ...pagination
    })
    loading.value = false
    tableData.value = res.rows
    total.value = res.total
  } catch (error) {
    loading.value = false
  }
}

onMounted(() => {
  getList()
  window.$wujie?.bus.$on('data-migration-update', () => {
    getList()
  })
})


</script>

<style lang="less" scoped>
.app-container {
  min-height: calc(100vh - 114px);
  display: flex;
  flex-direction: column;
  padding: 24px 28px 20px;

  .search-con {
    display: flex;

    .input-wrapper {
      flex-grow: 1
    }

    .button-wrapper {
      margin-left: 8px;
    }
  }

  .table-con {
    display: flex;
    flex-grow: 1;
    flex-direction: column;
    justify-content: space-between;
    height: 100%;
  }
}
</style>
