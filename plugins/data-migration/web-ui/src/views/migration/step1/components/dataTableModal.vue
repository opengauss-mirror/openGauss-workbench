<template>
  <el-dialog v-model="winConVisible" :title="$t('step1.index.5q091ixigdc1')" draggable :before-close="closeModal"
  :z-index="1000" >
    <div >
      <el-form>
        <el-form-item :label="$t('step1.index.5q091ixigjo1')" label-position="right" style="margin-bottom: 0">
          <span style="font-size: 16px;">
            {{ tempdbname }}
          </span>
        </el-form-item>
        <el-form-item label-position="right">
          <div>
            <el-text v-if="selectedTbllength > 0" type="success">{{ $t('step1.index.5q091ixih2h0') }}
              {{ selectedTbllength }}
            </el-text>
            <el-text v-else type="default">{{$t('step1.index.defaultSeleMsg')}}</el-text>
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
        :row-key="(row) => row"
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
       <span class="dialog-footer">
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
      </span>
    </template>
  </el-dialog>

</template>

<script lang="ts" setup>
import {computed, nextTick, ref, watch} from 'vue'
import {defineEmits} from 'vue'
import {getdataTbl} from '@/api/detail'
import {Search} from "@element-plus/icons-vue";

const selectedTblCurrent = ref([])
const selectedTbllength = ref<number>(0)
const winConVisible = ref<boolean>(false)
const tableData = ref([])
const searchTblNam = ref<string>('')
const tableRef = ref()

const currentPage = ref<number>(1)
const pageSize = ref<number>(50)
const totalNum = ref<number>(0)
const pageSizeOptions = ref([50, 100, 200, 500])

const pageSizeChange = (e) => {
  pageSize.value = e
  fetchTblList()
}
const handlePageChange = (pageNum) => {
  currentPage.value = pageNum
  fetchTblList()
}

const emits = defineEmits(['close', 'data-selected'])
const handleSubmit = () => {
  emits('data-selected', selectedTblCurrent.value)
  closeModal()
}

const closeModal = () => {
  winConVisible.value = false
  emits('close')
}

const props = defineProps({
  seleDBMsg: {
    type: Object,
    required: true
  }
})
const selecTbl = ref([])
const tempdbname = ref('')
const useInFo = new FormData
const getTblSelec = async () => {
  const {dbName, url, username, password, seletedTbl} = props.seleDBMsg
  tempdbname.value = dbName
  if (seletedTbl) {
    selectedTblCurrent.value = seletedTbl
    selecTbl.value = seletedTbl
    handleSelectionChange(seletedTbl)
    selectedTblCurrent.value.forEach(item => {
      const row = filteredData.value.find(row => row === item)
      if (row) {
        tableRef.value.toggleRowSelection(row, true)
      }
    })
    isInitializing.value = false
  }
  useInFo.append('url', url)
  useInFo.append('username', username)
  useInFo.append('password', password)
}

async function fetchTblList() {
  await getdataTbl(useInFo, tempdbname.value, pageSize.value, currentPage.value)
    .then(response => {
      if (Number(response.code) === 200) {
        tableData.value = Object.keys(response.rows).map(key => response.rows[key])
        totalNum.value = response.total
      }
    })
    .catch(error => {
      console.error('Error fetching data:', error)
    })
}
const isInitializing = ref(true)
const handleSelectionChange = (selection) => {
  if (isInitializing.value) return
  selection.forEach(item => {
    const bool = selectedTblCurrent.value.some(key => key === item)
    if (!bool) {
      selectedTblCurrent.value.push(item)
    }
  })
  let tempselectedTblCurrent = {...selectedTblCurrent.value}
  Object.keys(tempselectedTblCurrent).forEach(item => {
    const boolCurr = filteredData.value.some(key => key === tempselectedTblCurrent[item])
    const boolSele = selection.some(key => key === tempselectedTblCurrent[item])
    if (boolCurr && boolSele === false) {
      selectedTblCurrent.value = selectedTblCurrent.value.filter(key => key !== tempselectedTblCurrent[item])
    }
  })
  selectedTbllength.value = selectedTblCurrent.value.length
}

const filteredData = computed(() => {
  if (searchTblNam.value.length > 0) {
    return Object.values(tableData.value).filter(item =>
      item.includes(searchTblNam.value)
    )
  } else {
    return Object.values(tableData.value)
  }
})

watch(() => filteredData.value, async (newValue) => {
  if (isInitializing.value) return
  if (newValue) {
    await nextTick()
    newValue.forEach(key => {
      selecTbl.value.forEach(item => {
        if (item === key) {
          tableRef.value.toggleRowSelection(key, true)
        }
      })
    })

  }
})

const init = () => {
  isInitializing.value = true
  selectedTblCurrent.value = []
  tableData.value = []
  selectedTbllength.value = 0
  currentPage.value = 1
  pageSize.value = 50
  getTblSelec()
  fetchTblList()
  winConVisible.value = true
}

init()

watch(() => props.seleDBMsg, (newValue) => {
  if (newValue) {
    init()
  }
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
  :deep(el-table__row) {
    height: 40px
  }

  :deep(el-table-column) {
    padding: 9px 5px 9px 16px;
  }
}

</style>
