<template>
  <el-popover
    placement="right-start"
    :width="500"
    trigger="click"
    :visible="visible"
    popper-style="padding: 0; border: 0"
  >
    <template #default>
      <div style="position: relative">
        <div class="show-info">
          <div class="title-row">
            <div class="title">{{ title }}</div>
            <svg-icon
              class="close"
              name="close"
              style="margin-left: 4px"
              @click="closeInfo"
            />
          </div>
          <div class="text">
            <slot name="context"></slot>
          </div>
        </div>
      </div>
    </template>
    <template #reference>
      <svg-icon
        class="question"
        name="question-circle"
        @click="showDetailInfo"
        v-click-outside="onClickOutside"
      />
    </template>
  </el-popover>
</template>

<script setup lang="ts">
import { ClickOutside as vClickOutside } from "element-plus";
const props = withDefaults(
  defineProps<{
    title: string
  }>(),
  {
    title: ''
  }
);
const visible = ref<boolean>(false);
const showDetailInfo = (event: any) => {
  visible.value = !visible.value;
};
const closeInfo = () => {
  visible.value = false;
};
const onClickOutside = () => {
  visible.value = false;
};
</script>
<style scoped lang="scss">
.show-info {
  top: -18px;
  left: 0px;
  .title-row {
    display: flex;
    flex-direction: row;
    align-items: center;
    height: 36px;
    flex-shrink: 0;
    border-radius: 2px 2px 0px 0px;
    border-bottom: 1px solid var(--unnamed, #d9d9d9);
    background: var(--fill, #f7f7f7);
    .title {
      font-size: 14px;
      font-weight: 500;
      line-height: 24px;
      flex-grow: 1;
      text-align: left;
      padding-left: 24px;
    }
    .close {
      width: 16px;
      height: 16px;
      flex-shrink: 0;
      margin-right: 10px;
    }
  }

  .text {
    font-size: 12px;
    font-style: normal;
    font-weight: 400;
    line-height: 29px;
    padding: 12px 24px;
  }
}
</style>
