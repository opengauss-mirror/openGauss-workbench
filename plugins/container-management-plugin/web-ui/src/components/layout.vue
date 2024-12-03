<template>
  <a-tabs type="card-gutter" hide-content :active-key="active" @tab-click="tabClick" :editable="tabList.length > 1"
    @delete="tabDelete">
    <a-tab-pane v-for="tab in tabList" :key="tab.fullPath" :title="tab?.meta?.title">
    </a-tab-pane>
  </a-tabs>
  <slot></slot>
</template>
<script setup>
import { watch, ref } from 'vue'
import { useRouter } from 'vue-router';

const router = useRouter();
const tabList = ref([]);
const active = ref('');
watch(() => router.currentRoute.value, (last) => {
  active.value = last.fullPath;
  // 获取标签数据
  const tabData = JSON.parse(sessionStorage.getItem('cm-page-tab') || '[]');
  tabList.value = tabData;
}, { immediate: true })

function tabClick(key) {
  router.push(key)
}
function tabDelete(key) {
  // 获取标签数据
  const tabData = JSON.parse(sessionStorage.getItem('cm-page-tab') || '[]');
  const index = tabData.findIndex(tab => tab.fullPath === key);
  tabData.splice(index, 1); // 移除标签
  tabList.value = tabData;  // 更新数据
  // 更新数据
  sessionStorage.setItem('cm-page-tab', JSON.stringify(tabData));
  if (key === active.value) router.go(-1); // 当前标签则回退路由
}
</script>
<style lang="less" scoped></style>