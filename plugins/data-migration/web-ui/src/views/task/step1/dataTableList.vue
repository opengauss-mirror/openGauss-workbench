<template>
  <div class="modal-list" :style="{ top: posY + 'px', left: posX + 'px' }">
    <div class="modal-content-list" @mousedown="startDrag">
      <span class="close" @click="closeModal">&times;</span>
      <div class="header-list">
        <div style="display: grid; grid-template-columns: auto auto">
          <h2>{{$t('step1.index.5q091ixigjo1') + tempdbname + " "+ $t('step1.index.5q091ixigro4') }}</h2><br>
        </div>
      </div>
      <div class="scrollable-list">
        <div class="checkbox-list-list">
          <ul>
            <li v-for="(item, index) in selectedTblList" :key="index">
              {{ item }}
            </li>
          </ul>
        </div>
      </div>
    </div>
  </div>

</template>

<script setup>

import { defineEmits, ref } from 'vue'

const selectedTblList = []
const winCon = ref(false)
const posX = ref(600)
const posY = ref(-600)
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
}

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

const props = defineProps({
  seleDBMsgaft: {
    type: Object,
    required: true
  }
})

const tempdbname = ref('')
async function fetchTblList () {
  let { dbName, TblList } = props.seleDBMsgaft
  tempdbname.value = dbName
  if (!dbName && !TblList) {
    alert('attention!!!')
  } else if (TblList && TblList === '全部') {
    if (dbName) {
      alert(dbName + '选择了全部表')
    }
    // eslint-disable-next-line no-dupe-else-if
  } else if (TblList && dbName) {
    let TblLists = TblList.split(',')
    for (let Tbl of TblLists) {
      let tempTbl = Tbl.split('.')
      if (tempTbl[1]) {
        selectedTblList.push(tempTbl[1])
      }
    }
    winCon.value = true
  }
}

const emits = defineEmits(['close'])
const closeModal = () => {
  winCon.value = false
  emits('close')
}

fetchTblList()

</script>

<style>
.modal-list {
  position: absolute;
  cursor: move;
  z-index: 100;
  width: 100%;
  height: 800px;
  display: flex;
  justify-content: center;
  align-items: center;
}

.modal-list {
  background-color: white;
  padding: 20px;
  border-radius: 5px;
  width: 100%;
  height: 90%;
  max-width: 800px;
  max-height: 800px;
  position: relative;
  display: flex;
  flex-direction: column;
  box-shadow: 0 0 10px rgba(0, 0, 0, 0.5);
  cursor: move;
}

.header-list {
  padding: 10px;
  position: relative;
  width: 670px;
  background-color: #eeeeee;
  top: 10px;
  z-index: 102;
  cursor: move;
}
.header-list h2 {
  font-size: xx-large;
}
.scrollable-list {
  cursor: move;
  height: 670px;
  width: 105%;
  flex: 1;
  overflow-y: auto;
}

.checkbox-list-list {
  padding: 10px;
}

.checkbox-list-list li {
  font-size: large;
}

.close {
  position: absolute;
  top: 0px;
  right: 0px;
  font-size: 30px;
  cursor: pointer;
  color: rgba(0, 0, 0, 0.5);
  transition: color 0.3s ease;
}

.close:hover {
  color: rgba(0, 0, 0, 0.8);
}
</style>
