<template>
  <div class="visual-config-container tab-content d-a-form">
    <div class="vc-item" v-for="(k, kKey) in config.list" :key="`k${kKey}`">
      <div class="delete" @click="operateList('delete', kKey)">-</div>
      <a-form-item :label="$t('modeling.components.VisualConfig.5mpu5vnytdc0')">
        <a-select v-model="k.x" :placeholder="$t('modeling.components.VisualConfig.5mpu5vnytu40')" @change="save">
          <a-option  class="dianayako_select-option-disabled"   v-for="(item, key) in selectData.x" :key="`x${key}`" :value="item">{{ item }}</a-option>
        </a-select>
      </a-form-item>
      <a-form-item :label="$t('modeling.components.VisualConfig.5mpu5vnytxk0')">
        <a-select v-model="k.y" :placeholder="$t('modeling.components.VisualConfig.5mpu5vnytzw0')" @change="save">
          <a-option  class="dianayako_select-option-disabled"   v-for="(item, key) in selectData.y" :key="`y${key}`" :value="item">{{ item }}</a-option>
        </a-select>
      </a-form-item>
      <a-form-item :label="$t('modeling.components.VisualConfig.5mpu5vnyu200')">
        <a-select v-model="k.type" :placeholder="$t('modeling.components.VisualConfig.5mpu5vnyu4g0')" @change="save">
          <a-option  class="dianayako_select-option-disabled"   v-for="(item, key) in selectData.type" :key="`type${key}`" :value="item">{{ item }}</a-option>
        </a-select>
      </a-form-item>
    </div>
    <a-button @click="operateList('add')" type="primary">{{$t('modeling.components.VisualConfig.5mpu5vnyu6k0')}}</a-button>
  </div>
</template>
<script setup lang="ts">
import { reactive } from 'vue'
import { useI18n } from 'vue-i18n'
const { t } = useI18n()
const emits = defineEmits(['saveData'])
const save = () => emits('saveData', config.list)
const selectData = {
  x: [t('modeling.components.VisualConfig.5mpu5vnyu8g0')],
  y: [t('modeling.components.VisualConfig.5mpu5vnyuak0'), t('modeling.components.VisualConfig.5mpu5vnyuck0'), t('modeling.components.VisualConfig.5mpu5vnyuek0')],
  type: [t('modeling.components.VisualConfig.5mpu5vnyugo0'), t('modeling.components.VisualConfig.5mpu5vnyuig0')]
}
const modal = { x: '', y: '', type: '' }
const config = reactive({
  list: [] as { x: string, y: string, type: string }[]
})
const init = (list: { x: string, y: string, type: string }[]) => {
  config.list = list
}
const operateList = (type: string, key?: number) => {
  if (type === 'add') config.list.push(JSON.parse(JSON.stringify(modal)))
  else if (type === 'delete' && (key || key === 0)) config.list.splice(key, 1)
  save()
}
defineExpose({ init })
</script>
<style scoped lang="less">
  .visual-config-container {
    .vc-item {
      position: relative;
      border: 1px solid rgb(var(--primary-5));
      padding: 10px;
      box-sizing: border-box;
      margin-bottom: 10px;
      border-radius: 4px;
      .delete {
        position: absolute;
        right: 0;
        top: 50%;
        width: 20px;
        text-align: center;
        height: 20px;
        line-height: 15px;
        border-radius: 2px;
        transform: translate(50%, -50%);
        background-color: #fff;
        border: 1px solid transparent;
        cursor: pointer;
        &:hover {
          border: 1px solid rgb(var(--primary-5));
        }
      }
    }
  }
</style>
