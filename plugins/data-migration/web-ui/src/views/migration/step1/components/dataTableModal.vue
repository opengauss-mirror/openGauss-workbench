<template>
  <el-dialog v-model="winConVisible" :title="$t('step1.index.5q091ixigdc1')" draggable :before-close="closeModal"
             :z-index="1000" >
    <div >
      <el-form>
        <el-form-item :label="$t('step1.index.5q091ixigjo1')" label-position="right" style="margin-bottom: 0"
                      v-if="useInFo.get('dbType') === JDBCType.MySQL">
          <span style="font-size: 16px;">
            {{ currentTitleName }}
          </span>
        </el-form-item>
        <el-form-item :label="$t('step1.index.5q091ixiemk0')" label-position="right" style="margin-bottom: 8px" v-else>
          <span style="font-size: 16px;">
            {{ currentTitleName }}
          </span>
        </el-form-item>
        <el-form-item label-position="right">
          <div>
            <el-text v-if="selectedCount > 0" type="success">{{ $t('step1.index.5q091ixih2h0') }}
              {{ selectedCount }}
            </el-text>
            <el-text v-else-if="useInFo.get('dbType') === JDBCType.MySQL" type="info">{{$t('step1.index.defaultSeleMsg')}}</el-text>
          </div>
          <el-input
            v-model.trim="searchTblNam"
            :placeholder="$t('step1.index.5q091ixih2i0')"
            clearable
            :suffix-icon="Search"
            maxlength="100"
          />
        </el-form-item>
      </el-form>
      <el-table
        :data="filteredData"
        style="width: 90%"
        border
        ref="tableRef"
        :row-key="getRowKey"
        @selection-change="handleSelectionChange"
        class="select-table"
      >
        <!-- 多选框 -->
        <el-table-column
          type="selection"
          :reserve-selection="true"
          width="55"
          style="height: 40px;padding:9px 15px; "
        />
        <el-table-column :label="$t('step1.index.5q091ixih5i0')">
          <template #default="{ row }">
            {{ row }}
          </template>
        </el-table-column>
      </el-table>
    </div>
    <template #footer>
      <div class="dialog-footer">
        <div style="justify-content: center" v-if="searchTblNam.length <= 0">
          <el-pagination
            v-model:current-page="currentPage"
            v-model:page-size="pageSize"
            :page-sizes="pageSizeOptions"
            layout="total, sizes, prev, pager, next, jumper"
            :total="totalNum"
            @size-change="pageSizeChange"
            @current-change="handlePageChange"
          />
        </div>
        <div style='display: flex; justify-content: center; gap: 40px;'>
          <el-button type="primary" @click="handleSubmit">{{ $t('step1.index.5q091ixigjo3') }}</el-button>
          <el-button @click="closeModal">{{ $t('step1.index.5q091ixigjo4') }}</el-button>
        </div>
      </div>
    </template>
  </el-dialog>
</template>

<script lang="ts" setup>
import {computed, nextTick, onUnmounted, ref, watch} from 'vue'
import {getdataTbl} from '@/api/detail'
import {Search} from "@element-plus/icons-vue";
import {KeyValue} from "@/types/global";
import {JDBCType} from "@/types/jdbc";

const winConVisible = ref<boolean>(false)
const tableData = ref<string[]>([])
const selectedTables = ref<string[]>([])
const selectedCount = computed(() => selectedTables.value.length)

const searchTblNam = ref<string>('')
const tableRef = ref()

const currentPage = ref<number>(1)
const pageSize = ref<number>(50)
const totalNum = ref<number>(0)
const pageSizeOptions = ref([50, 100, 200, 500])

const isLoading = ref(false)

const pageSizeChange = async (e: number) => {
  pageSize.value = e
  currentPage.value = 1
  await fetchTblList()
}
const handlePageChange = (pageNum: number) => {
  currentPage.value = pageNum
  fetchTblList()
}

const emits = defineEmits(['close', 'data-selected'])

const handleSubmit = () => {
  emits('data-selected', selectedTables.value)
  closeModal()
}

const closeModal = () => {
  if (tableRef.value) {
    tableRef.value.clearSelection()
  }
  selectedTables.value = []
  winConVisible.value = false
  emits('close')
}

const props = defineProps({
  seleDBMsg: {
    type: Object,
    required: true
  }
})

const currentTitleName = ref('')
const useInFo = new FormData

const fetchTblList = async () => {
  try {
    isLoading.value = true
    const response = await getdataTbl(
      useInFo.get('dbType'),
      useInFo.get('nodeId'),
      currentTitleName.value,
      pageSize.value,
      currentPage.value
    ) as KeyValue
    if (response.code === 200) {
      tableData.value = Object.values(response.data.records)
      totalNum.value = response.data.total
      await nextTick()
      if (tableRef.value) {
        await restoreSelectionState()
      }
    }
  } catch (error) {
    console.error('Error fetching data:', error)
  } finally {
    isLoading.value = false
  }
}

const isRestoring = ref(false)

const restoreSelectionState = async () => {
  await nextTick()
  if (!tableRef.value) return
  const currentSelected = [...selectedTables.value]
  isRestoring.value = true
  try {
    tableRef.value.clearSelection()
    await nextTick()
    filteredData.value.forEach(row => {
      if (selectedTables.value.includes(row)) {
        tableRef.value.toggleRowSelection(row, true)
      }
    })
  } catch (error) {
    console.error('恢复选择状态时出错:', error)
  } finally {
    setTimeout(() => {
      isRestoring.value = false
    }, 0)
  }
}

const getRowKey = (row: string): string => {
  return row
}

const isInitializing = ref(true)
const handleSelectionChange = (selection: string[]) => {
  if (isInitializing.value || isLoading.value || isRestoring.value) {
    return
  }

  if (searchTblNam.value.length > 0) {
    const currentDisplayedSet = new Set(filteredData.value)
    const notDisplayed = selectedTables.value.filter(table =>
      !currentDisplayedSet.has(table)
    )
    selectedTables.value = [...notDisplayed, ...selection]
  } else {
    const currentPageTables = new Set(tableData.value)
    const otherPagesSelected = selectedTables.value.filter(table => !currentPageTables.has(table))
    selectedTables.value = [...otherPagesSelected, ...selection]
  }
}

watch(searchTblNam, async (newVal) => {
  if (!isInitializing.value && !isLoading.value) {
    await nextTick()
    if (newVal === '' && tableRef.value) {
      restoreSelectionState()
    }
  }
})

const filteredData = computed(() => {
  if (searchTblNam.value.length > 0) {
    return tableData.value.filter(item =>
      item.includes(searchTblNam.value)
    )
  } else {
    return tableData.value
  }
})

const initializeSelection = async () => {
  const { dbName, seletedTbl, nodeId, dbType } = props.seleDBMsg
  currentTitleName.value = dbName
  if (seletedTbl && Array.isArray(seletedTbl) && seletedTbl.length > 0) {
    selectedTables.value = seletedTbl.filter(item => typeof item === 'string')
  } else {
    selectedTables.value = []
  }

  useInFo.set('nodeId', nodeId)
  useInFo.set('dbType', dbType)
}

const resetAllState = () => {
  selectedTables.value = []
  tableData.value = []
  currentPage.value = 1
  pageSize.value = 50
  totalNum.value = 0
  searchTblNam.value = ''
  isLoading.value = false
}

const init = async () => {
  isInitializing.value = true
  resetAllState()
  await initializeSelection()
  winConVisible.value = true
  await nextTick()
  await fetchTblList()
  setTimeout(() => {
    isInitializing.value = false
  }, 300)
}

onUnmounted(() => {
  resetAllState()
})

watch(filteredData, () => {
  if (!isInitializing.value && !isLoading.value) {
    restoreSelectionState()
  }
})

watch(winConVisible, async (newVal) => {
  if (newVal && tableData.value.length > 0) {
    await nextTick()
    setTimeout(() => {
      restoreSelectionState()
    }, 200)
  }
})

defineExpose({
  init
})

</script>

<style scoped>
.header p {
  font-size: x-large;
}

.footer {
  padding: 10px;
  position: sticky;
  bottom: 0;
  z-index: 102;
  overflow-x: auto;
  justify-content: center;
}

.select-table {
  :deep(.el-table__row) {
    height: 40px
  }

  :deep(.el-table-column) {
    padding: 9px 5px 9px 16px;
  }
}
</style>
