<template>
  <div class="modal-list" :style="{ top: posY + 'px', left: posX + 'px' }">
    <div class="modal-content-list" @mousedown="startDrag">
      <div class="header-list">
        <span class="close" @click="closeModal">&times;</span>
        <div style="display: grid; grid-template-columns: auto auto">
          <h2>{{$t('step1.index.5q091ixigjo1') + tempdbname + " "+ $t('step1.index.5q091ixigro4') }}</h2><br>
          <p style="color: red; font-size: x-large;">{{$t('step1.index.5q091ixih2h0')}} {{selectedTblList.length}}</p>
        </div>
      </div>
      <div class="scrollable-list">
        <a-list
          :dataSource="selectedTblList"
          bordered
          itemLayout="horizontal"
          :scrollbar="true"
          :max-height="tempListHeight"
        >
          <a-list-item v-for="(item, index) in selectedTblList" :key="index">
            <a-list-item-meta
              :title="item"
            />
          </a-list-item>
        </a-list>
      </div>
    </div>
  </div>

</template>

<script setup>

import { defineEmits, ref } from 'vue'

const selectedTblList = []
const winCon = ref(false)
const posX = ref(window.innerWidth / 3)
const posY = ref(window.innerHeight / 15)
const tempListHeight = ref((window.innerHeight) * 0.8 * 0.65)
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
async function fetchTblList() {
  let {dbName, TblList} = props.seleDBMsgaft
  tempdbname.value = dbName
  if (!dbName && !TblList) {
    alert('attention!!!')
  } else if (TblList && TblList === '全部') {
    if (dbName) {
      alert(dbName + '选择了全部表')
    }
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

<style scoped>
.modal-list {
  position: absolute;
  cursor: move;
  z-index: 100;
  width: 40%;
  height: 80%;
  max-height: 80%;
  min-height: 740px;
  display: flex;
  flex-direction: column;
  background-color: white;
  padding: 20px;
  border-radius: 5px;
  box-shadow: 0 0 10px rgba(0, 0, 0, 0.5);
  overflow: hidden;
}

.header-list {
  padding: 10px;
  position: sticky;
  top: 0;
  background-color: #eeeeee;
  z-index: 102;
  cursor: move;
  overflow-x: auto;
  width: 100%;
  box-sizing: border-box;
}

.header-list h2 {
  font-size: xx-large;
}

.scrollable-list {
  cursor: move;
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow-x: auto;
  overflow-y: auto;
  margin-top: 10px;
  padding: 0 10px;
  width: 100%;
  position: relative;
  min-height: 30%;
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

:deep(.arco-list-medium .arco-list-content-wrapper .arco-list-content  .arco-list-item ) {
  padding: 0px 20px;
}
</style>
