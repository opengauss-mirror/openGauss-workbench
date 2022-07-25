<!--
Copyright ruoyi.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
-->
<template>
  <div class="m-container-layout">
    <div class="m-container-layout-inner">
      <slot></slot>
    </div>
  </div>
</template>

<script lang="ts" setup></script>

<style lang="scss" scoped>
  .m-container-layout {
    width: 100%;
    height: 100%;
    display: flex;
    padding: 20px;
    box-sizing: border-box;
    .m-container-layout-inner {
      flex: 1;
      display: flex;
      flex-direction: column;
      box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
      background: white;
      padding: 20px;
      width: 100%;
      height: 100%;
      box-sizing: border-box;
    }
  }
</style>
