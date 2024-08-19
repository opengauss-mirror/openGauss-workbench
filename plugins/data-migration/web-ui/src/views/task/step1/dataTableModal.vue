<template>
  <div class="modal" :style="{ top: posY + 'px', left: posX + 'px' }">
    <div class="modal-content"  @mousedown="startDrag">
      <div class="header">
        <div style="display: grid; grid-template-columns: auto auto;">
          <h2>{{$t('step1.index.5q091ixigjo1') + tempdbname }}</h2><br>
          <p v-if = "selectedTbl.length > 0" style="color: red">{{$t('step1.index.5q091ixigjo2') + selectedTbl.length }}</p>
        </div>
        <input type="text"
              v-model="inputValue"
              :placeholder="$t('step1.index.5q091ixigjo5')">
      </div>
      <span class="close" @click="closeModal">&times;</span>
      <div v-if="search" class="scrollable">
        <div class="checkbox-list">
          <ul>
            <li>
              <label>
                <input type="checkbox" v-model="selectAll" @change="selectAllItems"> {{$t('step1.index.5q091ixigjo6') }}
              </label>
            </li>
            <li v-for="( item, index) in data" :key="index" >
              <label>
                <input type="checkbox" v-model="selectedTbl" :value="item"> {{ item }}
              </label>
            </li>
          </ul>
        </div>

      </div>
      <div v-else class="scrollable">
        <div class="checkbox-list">
          <ul>
            <li v-for="(item, index) in sorteedTbl" :key="index" >
              <label>
                <input type="checkbox" v-model="selectedTbl" :value="item"
                > {{ item }}
              </label>
            </li>
          </ul>
        </div>
      </div>
      <div v-if="search" style = 'display: flex; justify-content: center; gap: 40px;' class="footer">
        <button class="add-sub-btn" type="submit" @click='submitSelection'>{{$t('step1.index.5q091ixigjo3')}}</button>
        <button  @click = 'closeModal'>{{$t('step1.index.5q091ixigjo4')}}</button>
      </div>
    </div>
  </div>

</template>

<script setup>
import { computed, ref, watch } from 'vue'
import { defineEmits } from 'vue'
import { getdataTbl } from '@/api/detail'

const selectedTbl = ref([])
const winCon = ref(false)
const search = ref(true)
const inputValue = ref('')
const data = ref()
const selectAll = ref(false)

const props = defineProps({
  seleDBMsg: {
    type: Object,
    required: true
  }
})

winCon.value = true

watch(() => props.seleDBMsg, async (newValue) => {
  if (newValue) {
    await fetchTblList()
  }
})

let piecePreTbl = ''

const tempdbname = ref('')
async function fetchTblList () {
  const formData = new FormData()
  const { dbName, url, username, password, seletedTbl } = props.seleDBMsg
  tempdbname.value = dbName
  const c = computed(() => `${url}/${dbName}`)
  let preseletedTbl = ''
  let tempAllSele = false
  if (seletedTbl) {
    preseletedTbl = JSON.stringify(seletedTbl)
    if (preseletedTbl && (preseletedTbl === '"全部"' || preseletedTbl === '全部')) {
      tempAllSele = true
    } else if (preseletedTbl) {
      piecePreTbl = preseletedTbl.split(',')
    }
  }
  formData.append('url', c.value)
  formData.append('username', username)
  formData.append('password', password)
  try {
    await getdataTbl(formData, dbName)
      .then(response => {
        data.value = response.data
        if (tempAllSele) {
          data.value.forEach(item => {
            selectedTbl.value.push(item)
          })
          selectAll.value = true
        } else {
          if (piecePreTbl.length > 0) {
            data.value.forEach(item => {
              let found = false
              for (let i = 0; i < piecePreTbl.length; i++) {
                let str = piecePreTbl[i].toString().replace(/[\[\]"]/g, '')
                if (str === item) {
                  found = true
                  break
                }
              }
              if (found) {
                selectedTbl.value.push(item)
              }
            })
          }
        }
        winCon.value = true
      })
      .catch(error => {
        console.error('Error fetching data:', error)
      })
  } catch (error) {
    console.error('Error fetching data:', error)
  }
}

// eslint-disable-next-line vue/return-in-computed-property
const sorteedTbl = computed(() => {
  if (inputValue.value.length > 0) {
    let filteredData
    filteredData = data.value.filter(item => {
      if (typeof item === 'string') {
        return item.toLowerCase().includes(inputValue.value)
      }
      return false
    })
    return filteredData
  }
})

watch(inputValue, (newValue) => {
  search.value = newValue.trim() === ''
})

watch(selectedTbl, (val) => {
  selectAll.value = val.length === data.value.length
})

const selectAllItems = () => {
  if (selectAll.value) {
    selectedTbl.value = [...data.value]
  } else {
    selectedTbl.value = []
  }
}

const emits = defineEmits(['close', 'data-selected'])

const closeModal = () => {
  winCon.value = false
  emits('close')
}

const submitSelection = () => {
  let dataToSend = {
    selectedValue: selectedTbl.value,
    selecTbl: tempdbname.value
  }
  if (selectedTbl.value.length === data.value.length && data.value.length !== 0) {
    dataToSend.selectedValue = ''
    emits('data-selected', dataToSend)
  } else {
    emits('data-selected', dataToSend)
  }
  closeModal()
}

const posX = ref(80)
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

fetchTblList()

</script>

<style>
.modal {
  position: absolute;
  cursor: move;
  z-index: 100;
  width: 100%;
  height: 800px;
  display: flex;
  justify-content: center;
  align-items: center;
}

.modal-content {
  cursor: move;
  background-color: white;
  padding: 20px;
  border-radius: 5px;
  width: 100%;
  height: 90%;
  max-width: 800px;
  position: relative;
  overflow-y: auto;
  display: flex;
  flex-direction: column;
  box-shadow: 0 0 10px rgba(0, 0, 0, 0.5)
}
.header {
  cursor: move;
  background-color: #f0f0f0;
  padding: 10px;
  position: static;
  top: 0;
  z-index: 102;
}
.header h2 {
  font-size: xx-large;
}
.header p {
  font-size: x-large;
}
.scrollable {
  cursor: move;
  height: 60%;
  flex: 1;
  overflow-y: auto;
}

.checkbox-list {
  padding: 10px;
}

.checkbox-list label {
  font-size: large;
}

.footer {
  background-color: #f0f0f0;
  padding: 10px;
  position: sticky;
  bottom: 0;
  z-index: 102;
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

</style>
