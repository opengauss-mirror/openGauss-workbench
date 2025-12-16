<template>
  <div class="app-container" id="databaseManage">
    <div class="main-bd">
      <div class="upgrade-container">
        <div class="flex-between mb-s">
          <div>
            <div class="flex-row cond-btns">
              <el-button type="primary" @click="handleAdd('create')">
                <el-icon><Plus /></el-icon>
                {{ $t('database.index.5oxhr0qz15w0') }}
              </el-button>
              <el-upload
                class="mr-s"
                action=""
                :before-upload="beforeUpload"
                :show-file-list="false"
                accept=".csv"
              >
                <el-button type="primary">
                  <el-icon><Upload /></el-icon>
                  {{ $t('manage.PluginInstall.clickToUpload') }}
                </el-button>
              </el-upload>
              <el-button :loading="list.downloadLoading" type="default" @click="downloadTemp">
                <el-icon><Download /></el-icon>
                {{ $t('database.index.5oxhr0qz2bs0') }}
              </el-button>
              <el-popconfirm
                :title="$t('database.index.batchDeleteConfirm', { count: selectedRows.length })"
                @confirm="handleBatchDelete"
              >
                <template #reference>
                  <el-button type="danger" >
                    <el-icon><Delete /></el-icon>
                    {{ $t('physical.index.5mphf11rr590') }}
                  </el-button>
                </template>
              </el-popconfirm>
            </div>
          </div>
          <div>
            <el-form :model="filter" inline :rules="searchFormRules" ref="searchFormRef">
              <el-form-item prop="name" :label="$t('database.index.else1')">
                <el-input v-model.trim="filter.name" :placeholder="$t('database.index.5oxhr0qz2s00')" maxlength="255"
                          clearable style="width: 200px;" />
              </el-form-item>
              <el-form-item prop="ip" :label="$t('database.index.elseIp')">
                <el-input v-model.trim="filter.ip" :placeholder="$t('database.index.elseIpPlaceholder')" maxlength="255"
                          clearable style="width: 200px;"></el-input>
              </el-form-item>
              <el-form-item prop="type" :label="$t('database.index.else3')">
                <el-select v-model="filter.type" :placeholder="$t('database.index.else3Placeholder')"
                           clearable style="width: 200px;">
                  <el-option :value="JDBCType.MySQL" label="MySQL" />
                  <el-option :value="JDBCType.openGauss" label="openGauss" />
                  <el-option :value="JDBCType.PostgreSQL" label="PostgreSQL" />
                  <el-option :value="JDBCType.Elasticsearch" label="Elasticsearch" />
                  <el-option :value="JDBCType.Milvus" label="Milvus" />
                </el-select>
              </el-form-item>
              <el-form-item>
                <el-button type="default" @click="handleSearch()">
                  <el-icon><Search /></el-icon>
                  {{ $t('database.index.5oxhr0qz30g0') }}
                </el-button>
              </el-form-item>
            </el-form>
          </div>
        </div>
        <div class="table-pagination-container">
          <div class="table-wrapper">
            <el-table :data="list.data" v-loading="list.loading"  row-key="clusterId"
                      @expand="handleExpand" :expand-row-keys="expandable.expandedRowKeys" style="height: 95%"
                      @selection-change="handleSelectionChange"
            >
              <template #empty>
                <div>
                  <empty-page></empty-page>
                </div>
              </template>
              <el-table-column type="selection" width="55" />
              <el-table-column type="expand">
                <template #default="{ row }">
                  <div v-if="row.nodes?.length > 0">
                    <JdbcNodeTable
                      :jdbc-data="row"
                      @valid-res="(val) => { row.state = val }"
                    />
                  </div>
                </template>
              </el-table-column>
              <el-table-column :label="$t('database.index.5oxhr0qz48w0')" prop="name" min-width="300" show-overflow-tooltip/>
              <el-table-column :label="$t('database.index.5oxhr0qz4fs0')" prop="dbType" min-width="150">
                <template #default="{ row }">
                  <el-text>{{ JDBCType.normalize(row.dbType) }}</el-text>
                </template>
              </el-table-column>
              <el-table-column :label="$t('database.index.version')" prop="version" min-width="200">
                <template #default="{ row }">
                  {{ row.versionNum ? row.versionNum: $t('database.JdbcNodeTable.else4') }}
                </template>
              </el-table-column>
              <el-table-column
                :label="$t('database.index.clusterType')"
                prop="clusterType"
                min-width="100"
              >
                <template #default="{ row }">
                  <el-text v-if="row.deployType === 'SINGLE_NODE'">{{ $t('database.index.singleNode') }}</el-text>
                  <el-text v-else>{{ $t('database.index.multiNode') }}</el-text>
                </template>
              </el-table-column>
              <el-table-column
                :label="$t('database.index.5oxhr0qz4zk0')"
                prop="updateTime"
                min-width="300"
              />
              <el-table-column
                :label="$t('database.index.5oxhr0qz58o0')"
                fixed="right"
                min-width="150"
              >
                <template #default="{ row }">
                  <div class="flex-row">
                    <el-link class="mr" @click="handleAdd('update', row)" type="primary">
                      {{ $t('database.index.5oxhr0qz37o0') }}
                    </el-link>
                    <el-popconfirm
                      :title="$t('database.index.5oxhr0qz3f40')"
                      @confirm="handleDel(row)"
                    >
                      <template #reference>
                        <el-link type="danger">{{ $t('database.index.5oxhr0qz40k0') }}</el-link>
                      </template>
                    </el-popconfirm>
                  </div>
                </template>
              </el-table-column>
            </el-table>
          </div>
          <div class="pagination-wrapper">
            <el-pagination
              v-model:current-page="filter.pageNum"
              v-model:page-size="filter.pageSize"
              :page-sizes="[10, 20, 50, 100]"
              :total="list.page.total"
              :hide-on-single-page="false"
              layout="total, sizes, prev, pager, next, jumper"
              @size-change="pageSizeChange"
              @current-change="currentPage"
              class="pagination-fixed"
            />
          </div>
        </div>
        <add-jdbc ref="addJdbcRef" @finish="getListData"></add-jdbc>
        <host-import-dlg ref="hostImportDlgRef"></host-import-dlg>
      </div>
    </div>
  </div>
</template>

<script lang="ts" setup>
import { KeyValue } from '@/types/global'
import { onMounted, reactive, computed, ref, h } from 'vue'
import { Message, Modal } from '@arco-design/web-vue'
import {jdbcPage, delJdbc, uploadFileJdbc, downloadTemplate, uploadRealJdbc, batchDelJdbc} from '@/api/ops'
import AddJdbc from './AddJdbc.vue'
import HostImportDlg from './HostImportDlg.vue'
import JdbcNodeTable from './JdbcNodeTable.vue'
import { useI18n } from 'vue-i18n'
import {JDBCType} from "@/types/jdbc";
import { Plus,Download, Upload, Search, Delete } from '@element-plus/icons-vue'
import {ElMessageBox, FormInstance} from "element-plus";
import showMessage from "@/hooks/showMessage";
import EmptyPage from "@/components/emptyPage"
const { t } = useI18n()

const expandable = reactive({
  expandedRowKeys: [] as (string | number)[]
})

const handleExpand = (row: any, expandedRows: any[]) => {
  const rowKey = row.clusterId
  if (expandable.expandedRowKeys.includes(rowKey)) {
    const index = expandable.expandedRowKeys.indexOf(rowKey)
    expandable.expandedRowKeys.splice(index, 1)
  } else {
    expandable.expandedRowKeys.push(rowKey)
  }
}

const filter = reactive({
  name: '',
  ip: '',
  type: '',
  pageNum: 1,
  pageSize: 10
})

const searchFormRef = ref<FormInstance>()

const valiSearchIp = (rule: any, value: string, callback: (error?: Error) => void) => {
  if (!value) {
    callback()
    return
  }
  if (value.length > 255) {
    callback(new Error(t('database.index.ipMaxLength')))
    return
  }
  const chineseRegex = /[\u4e00-\u9fa5]/
  if (chineseRegex.test(value)) {
    callback(new Error(t('database.index.noChinese')))
    return
  }
  callback()
}

const searchFormRules = computed(<FormRules>() => ({
  name: [
    { max: 255, message: t('database.index.nameMaxLength'), trigger: 'blur' }
  ],
  ip: [
    { max: 255, message: t('database.index.ipMaxLength'), trigger: 'blur' },
    { validator: valiSearchIp, trigger: 'blur' }
  ]
}))


const list = reactive<KeyValue>({
  data: [],
  page: {
    total: 0,
    'show-total': true,
    'show-jumper': true,
    'show-page-size': true
  },
  loading: false,
  downloadLoading: false,
  rowSelection: {
    type: 'checkbox',
    showCheckedAll: true
  },
  selectedKeys: []
})

onMounted(() => {
  getListData()
})

const handleSearch = async () => {
  if (!searchFormRef.value) return

  try {
    await searchFormRef.value.validate()
    getListData()
  } catch (error) {
    console.log('valid error', error)
  }
}

const getListData = () => {
  list.loading = true
  jdbcPage(filter).then((res: KeyValue) => {
    if (Number(res.code) === 200) {
      list.data = []
      expandable.expandedRowKeys = []
      res.rows.forEach((item: KeyValue) => {
        item.state = -1
        item.loading = false
        list.data.push(item)
        expandable.expandedRowKeys.push(item.clusterId)
      })
      list.page.total = res.total
    }
  }).finally(() => {
    list.loading = false
  })
}

const beforeUpload = (file: File) => {
  ElMessageBox.confirm(
    `${t('database.index.else2')} ${file.name}`,
    t('database.index.5oxhr0qz5g40'),
    {
      confirmButtonText: t('components.FusionSearch.confirm'),
      cancelButtonText: t('components.FusionSearch.cancel'),
      type: 'warning'
    }
  ).then(() => {
    handleUpload(file)
  }).catch(() => {
  })
  return false
}

const hostImportDlgRef = ref<null | InstanceType<typeof HostImportDlg>>(null)
// import analysis
const handleUpload = (fileObj: any) => {
  const index1 = fileObj.name.lastIndexOf('.')
  const index2 = fileObj.name.length
  const type = fileObj.name.substring(index1, index2)
  let flag = true
  const file6 = '.csv'
  if (file6 !== type) {
    flag = false
  }
  if (!flag) {
    showMessage('error', 'Only.csv files can be uploaded. Upload the files again')
    return
  } else {
    const data = new FormData()
    data.append('file', fileObj)
    list.loading = true
    uploadFileJdbc(data).then((res: KeyValue) => {
      if (Number(res.code) === 200) {
        if (res.data.succNum === res.data.total) {
          handleRealUpload(fileObj)
        }
        if (res.data.failNum > 0 && res.data.failDetail.length) {
          hostImportDlgRef.value?.open(res.data.failDetail)
        }
      }
    }).finally(() => {
      list.loading = false
    })
  }
}

// real import
const handleRealUpload = (fileObj: any) => {
  const data = new FormData()
  data.append('file', fileObj)
  list.loading = true
  uploadRealJdbc(data).then((res: KeyValue) => {
    if (Number(res.code) === 200) {
      showMessage('success', 'Import successfully')
      getListData()
    }
  }).finally(() => {
    list.loading = false
  })
}

const addJdbcRef = ref<null | InstanceType<typeof AddJdbc>>(null)
const handleAdd = (type: string, data?: KeyValue) => {
  addJdbcRef.value?.open(type, data)
}

const handleDel = (record: KeyValue) => {
  delJdbc(record.clusterId).then((res: KeyValue) => {
    if (Number(res.code) === 200) {
      Message.success({
        content: 'delete success'
      })
      getListData()
    }
  })
}

const downloadTemp = () => {
  list.downloadLoading = true
  downloadTemplate().then((res: any) => {
    if (res) {
      const blob = new Blob([res], {
        type: 'text/plain'
      })
      const a = document.createElement('a')
      const URL = window.URL || window.webkitURL
      const herf = URL.createObjectURL(blob)
      a.href = herf
      a.download = 'jdbc-template.csv'
      document.body.appendChild(a)
      a.click()
      document.body.removeChild(a)
      window.URL.revokeObjectURL(herf)
    } else {
      Message.error('Download failed, please try again')
    }
  }).finally(() => {
    list.downloadLoading = false
  })
}

const selectedRows = ref<any[]>([])
const handleSelectionChange = (selection: any[]) => {
  selectedRows.value = selection
}

const handleBatchDelete = () => {
  if (selectedRows.value.length === 0) {
    showMessage('warning', t('database.index.deleWarning'))
    return
  }
  const ids = selectedRows.value.map(item => item.clusterId)
  batchDelJdbc(ids) .then((res: KeyValue) => {
    if(Number(res.code) === 200) {
      showMessage('success', t('database.index.deleSuccess'))
    }
  }) .catch((error: any) => {
    console.log(error)
  }) .finally(() => {
    getListData()
  })
}

const currentPage = (e: number) => {
  filter.pageNum = e
  getListData()
}

const pageSizeChange = (e: number) => {
  filter.pageSize = e
  getListData()
}
</script>

<style lang="less" scoped>
:deep(.arco-table-container .arco-table-content-scroll-x) {
  overflow: scroll;
}

.app-container {
  .main-bd {
    .upgrade-container {
      padding: 20px;
      box-sizing: border-box;
      height: calc(100vh - 76px - 40px);
      display: flex;
      flex-direction: column;
      overflow: hidden;

      .top-label {
        width: 200px;
        text-align: right;
      }

      .cond-btns {
        button {
          margin-right: 10px;
        }
      }

      .table-pagination-container {
        flex: 1;
        display: flex;
        flex-direction: column;
        min-height: 0;
        position: relative;
      }

      .table-wrapper {
        flex: 1;
        overflow: auto;
        min-height: 200px;

        :deep(.el-table) {
          width: 100%;

          .el-table__body {
            width: 100% !important;
          }

          .el-table__header-wrapper,
          .el-table__body-wrapper {
            width: 100% !important;
          }

          .el-table__fixed,
          .el-table__fixed-right {
            height: auto !important;
          }
        }
      }

      .pagination-wrapper {
        flex-shrink: 0;
        background: var(--o-bg-color-base);
        position: sticky;
        bottom: 0;
        left: 0;
        right: 0;
        :deep(.el-pagination) {
          justify-content: flex-start;
          @media (max-width: 768px) {
            flex-wrap: wrap;
            gap: 8px;
            .el-pagination__sizes,
            .el-pagination__jump {
              margin-left: 0 !important;
            }
          }
        }
      }
    }
  }
}

.status-container {
  display: inline-flex;
  align-items: center;
  gap: 2px;
  height: 24px;
  line-height: 1;
}

@media (max-width: 1200px) {
  .app-container .main-bd .upgrade-container {
    padding: 16px;
  }
}

@media (max-width: 768px) {
  .app-container .main-bd .upgrade-container {
    padding: 12px;
    height: calc(100vh - 60px - 30px);
  }

  .flex-between.mb-s {
    flex-direction: column;
    gap: 12px;
  }

  .cond-btns {
    flex-wrap: wrap;
    gap: 8px;

    button {
      margin-right: 0 !important;
    }
  }
}

</style>
