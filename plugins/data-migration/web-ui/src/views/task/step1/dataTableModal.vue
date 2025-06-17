<template>
  <div class="modal" :style="{ top: posY + 'px', left: posX + 'px' }">
    <div class="modal-content"  @mousedown="startDrag">
      <div class="header" >
        <div style="display: grid; grid-template-columns: auto auto;">
          <h2>{{$t('step1.index.5q091ixigjo1')}} {{ tempdbname }}</h2><br>
          <p v-if="selectedTbllength > 0" style="color: red">{{$t('step1.index.5q091ixih2h0')}} {{selectedTbllength}}</p>
          <p> {{selectedTbllength.value}}</p>
        </div>
        <span class="close" @click="closeModal">&times;</span>
        <a-input
          v-model="searchTblNam"
          :placeholder= "$t('step1.index.5q091ixih2i0')"
          style="margin-bottom: 10px;"
        />
      </div>
      <div class="content">
        <a-table
          row-key="name"
          showCheckedAll="true"
          v-model:selected-keys="selectedTblCurrent"
          sticky-header
          :data="formattedData"
          :loading="isLoading"
          :pagination="false"
          :row-selection="rowSelection"
          @selection-change="handleSelected"
          @select-all="handleSelectAllChange"
          v-if="searchTblNam.length <= 0"
          style="overflow-y: auto"
        >
          <template #columns>
            <a-table-column :title="$t('step1.index.5q091ixih5i0')" data-index="name" :width="100" ellipsis tooltip></a-table-column>
          </template>
        </a-table>
        <a-table
          row-key="name"
          v-model:selected-keys="searchTblCurrent"
          sticky-header
          :data="filteredItems"
          :pagination="false"
          :row-selection="rowSelection"
          :loading="isLoading"
          @selection-change="handleSearchSelected"
          v-else
        >
          <template #columns>
            <a-table-column :title="$t('step1.index.5q091ixih5i0')" data-index="name" :width="100" ellipsis tooltip></a-table-column>
          </template>
        </a-table>
      </div>
      <div class="footer">
        <div style="justify-content: right">
          <a-pagination
            class="custom-pagination"
            show-total
            show-jumper
            show-page-size
            defaultPageSize = 50
            :total="totalNum"
            :current="currentPage"
            :page-size="pageSize"
            :page-size-options="pageSizeOptions"
            layout="->, total, sizes, prev, pager, next, jumper"
            @change="handlePageChange"
            @page-size-change="pageSizeChange"
          />
        </div>
        <div style = 'display: flex; justify-content: center; gap: 40px;' >
          <button class="primary" type="submit" @click='handleSubmit'>{{$t('step1.index.5q091ixigjo3')}}</button>
          <button  @click = 'closeModal'>{{$t('step1.index.5q091ixigjo4')}}</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import {computed, reactive, ref, watch} from 'vue'
import { defineEmits } from 'vue'
import { getdataTbl } from '@/api/detail'

const selectedTblCurrent = ref([])
const searchTblCurrent = ref([])
const selectedTbl = ref([])
const winCon = ref(false)
const isLoading = ref(false)
const data = reactive( {
  value: [],
  totalNum: 0
})
const formattedData = computed(() => {
  return data.value.map(item => ({ name: item }))
})
const currentPage = ref(1)
const pageSize = ref(50)
const totalNum = ref(0)
const pageSizeOptions = ref([50, 100, 200, 500])
const rowSelection = reactive({
  type: 'checkbox',
  showCheckedAll: true,
  onlyCurrent: false
})
const searchTblNam = ref('');
const selectAll = ref(false)
const filteredItems = computed(() => {
  isLoading.value = true
  const query = searchTblNam.value.toLowerCase()
  const filtered = data.value.filter(item => item.toLowerCase().includes(query))
  totalNum.value = searchTblNam.value.length > 0? filtered.length: data.totalNum
  isLoading.value = false
  return filtered.map(item => ({ name: item }))
})

const searchBeforPage = ref(0)
const pageSizeChange = (e) => {
  pageSize.value = e
  fetchTblList()
}
const handlePageChange = (pageNum) => {
  if (searchTblNam.value.length > 0) {
    searchBeforPage.value = currentPage.value
  }
  currentPage.value = pageNum
  fetchTblList()
}
const selectedTbllength = ref(0)
const handleSelected = (keys) => {
  selectedTblCurrent.value = keys
  searchTblCurrent.value = [...selectedTblCurrent.value]
  selectedTbllength.value =  Object.keys(selectedTblCurrent.value).length + notFoundTbl.value
}
const handleSearchSelected = (keys) => {
  if (searchTblNam.value.length > 0) {
    let tempSearchLength = 0
    if (keys.length === 0) {
      filteredItems.value.forEach(item => {
        if (selectedTblCurrent.value.includes(item)) {
          tempSearchLength = tempSearchLength - 1
          selectedTblCurrent.value = [...selectedTblCurrent.value.filter(pics => pics !== item)]
        }
      })
    } else {
      keys.forEach(item => {
        if (!selectedTblCurrent.value.includes(item)) {
          tempSearchLength = tempSearchLength + 1
        }
      })
      searchTblCurrent.value = keys
      selectedTbllength.value =  Object.keys(selectedTblCurrent.value).length + tempSearchLength + notFoundTbl.value
    }
  }
}

const handleSelectAllChange = (checked) => {
  if (checked) {
    selectedTblCurrent.value = filteredItems.value
  } else {
    selectedTblCurrent.value = selectedTblCurrent.value.filter(item => !filteredItems.value.includes(item))
  }
  selectAll.value = checked
}
const emits = defineEmits(['close', 'data-selected'])
const handleSubmit = () => {
  if (searchTblNam.value.length > 0 ) {
    searchTblCurrent.value.forEach(item => {
      if (!selectedTblCurrent.value.includes(item)) {
        selectedTblCurrent.value = selectedTblCurrent.value.concat(item)
      }
    })
  }
  Object.entries(selectedTblCurrent.value).forEach(([key, array]) => {
    selectedTbl.value.push(array)
  })
  piecePreTbl.forEach(item => {
    if(!selectedTbl.value.includes(item) && item !== '') {
      selectedTbl.value.push(item)
    }
  })
  let dataToSend = {
    selectedValue: selectedTbl.value,
    selecTbl: tempdbname.value
  }
  if (selectedTbl.value.length === data.totalNum && data.totalNum !== 0) {
    dataToSend.selectedValue.unshift('')
    emits('data-selected', dataToSend)
  } else {
    emits('data-selected', dataToSend)
  }
  closeModal()
}

const closeModal = () => {
  winCon.value = false
  emits('close')
}

const props = defineProps({
  seleDBMsg: {
    type: Object,
    required: true
  }
})

winCon.value = true

watch(() => props.seleDBMsg,  (newValue) => {
  if (newValue) {
    init()
  }
})

watch(
  () => searchTblNam.value.length,
  (newValue) => {
    if (newValue === 0) {
      totalNum.value = data.totalNum
      currentPage.value = searchBeforPage.value
      searchBeforPage.value = 0
      searchTblCurrent.value.forEach(item => {
        if (!selectedTblCurrent.value.includes(item)) {
          selectedTblCurrent.value = selectedTblCurrent.value.concat(item)
        }
      })
    }
  }
)

let piecePreTbl = []
const tempdbname = ref('')
const useInFo = new FormData
const tableAllSele = ref(false)
const getTblSelec = async() => {
  const { dbName, url, username, password, seletedTbl } = props.seleDBMsg
  tempdbname.value = dbName
  let preseletedTbl = ''
  tableAllSele.value = false
  if (seletedTbl) {
    preseletedTbl = JSON.stringify(seletedTbl)
    if (preseletedTbl && (preseletedTbl === '"全部"' || preseletedTbl === '全部')) {
      tableAllSele.value = true
    } else if (preseletedTbl) {
      piecePreTbl = JSON.parse(preseletedTbl)
    }
  }
  useInFo.append('url', url)
  useInFo.append('username', username)
  useInFo.append('password', password)
}

const notFoundTbl = ref(0)

async function fetchTblList () {
  isLoading.value = false
  await getdataTbl(useInFo,tempdbname.value, pageSize.value, searchBeforPage.value!==0? searchBeforPage.value: currentPage.value)
    .then(response => {
      if (Number(response.code) === 200){
        data.value = response.rows
        totalNum.value = response.total
        data.totalNum = response.total
        if (piecePreTbl.length > 0) {
          const setPreTbl = new Set(piecePreTbl)
          const listC = data.value.filter(value => setPreTbl.has(value))
          piecePreTbl = piecePreTbl.filter(value => !listC.includes(value))
          listC.forEach(item => {
            if (!Object.values(selectedTblCurrent.value).includes(item)) {
              selectedTblCurrent.value.push(item)
            }
          })
        }
      }
      notFoundTbl.value = piecePreTbl.filter(char => char !== '').length
      searchTblCurrent.value = [...selectedTblCurrent.value]
      selectedTbllength.value =  Object.keys(selectedTblCurrent.value).length + notFoundTbl.value
    })
    .catch(error => {
      console.error('Error fetching data:', error)
    })
}

const posX = ref(300)
const posY = ref(80)
let dragging = false
let mouseX = 0
let mouseY = 0

const startDrag = (event) => {
  dragging = true
  mouseX = event.clientX
  mouseY = event.clientY
  document.addEventListener('mousemove', drag)
  document.addEventListener('mouseup', stopDrag)
}

const stopDrag = () => {
  dragging = false
  document.removeEventListener('mousemove', drag)
  document.removeEventListener('mouseup', stopDrag)
};

const drag = (event) => {
  if (dragging) {
    const deltaX = event.clientX - mouseX
    const deltaY = event.clientY - mouseY
    posX.value += deltaX
    posY.value += deltaY
    mouseX = event.clientX
    mouseY = event.clientY
  }
}

const init = () => {
  selectedTblCurrent.value = []
  selectedTbl.value = []
  selectedTbllength.value = 0
  searchBeforPage.value = 0
  notFoundTbl.value = 0
  getTblSelec()
  fetchTblList()
  winCon.value = true
}

init()
</script>

<style scoped>
.modal {
  position: absolute;
  cursor: move;
  z-index: 100;
  width: 100%;
  height: 80%;
  min-height: 500px;
  display: flex;
  justify-content: center;
  align-items: center;
  overflow: hidden;
}

.modal-content {
  cursor: move;
  background-color: white;
  padding: 20px;
  border-radius: 5px;
  width: 100%;
  height: 100%;
  max-width: 40%;
  position: relative;
  display: flex;
  flex-direction: column;
  box-shadow: 0 0 10px rgba(0, 0, 0, 0.5)
}
.header {
  cursor: move;
  background-color: #f0f0f0;
  padding: 10px;
  position: sticky;
  top: 0;
  z-index: 102;
}
.header h2 {
  font-size: xx-large;
}
.header p {
  font-size: x-large;
}
.content {
  flex: 1;
  cursor: move;
  background-color: white;
  border-radius: 5px;
  width: 100%;
  position: relative;
  display: flex;
  flex-direction: column;
  overflow-y: auto;
}
.footer {
  background-color: #f0f0f0;
  padding: 10px;
  position: sticky;
  bottom: 0;
  z-index: 102;
  overflow-x: auto;
}

.close {
  position: absolute;
  top: 20px;
  right: 20px;
  font-size: 30px;
  cursor: pointer;
  color: rgba(0, 0, 0, 0.5);
  transition: color 0.3s ease;
}

.close:hover {
  color: rgba(0, 0, 0, 0.8);
}

:deep(.arco-table .arco-table-cell) {
  padding: 0px 16px;
}

:deep(.arco-table .arco-table-td) {
  font-size: 16px;
}

:deep(.arco-table-container .arco-table-content-scroll-x) {
  overflow-y: auto;
}
:deep(.arco-input-wrapper .arco-input) {
  background-color: white;
}
</style>
