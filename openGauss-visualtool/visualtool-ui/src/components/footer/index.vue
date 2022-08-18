<template>
  <a-layout-footer class="footer">©{{ new Date().getFullYear() }} {{$t('footer.index.5msicm67hz00')}}</a-layout-footer>
</template>

<script lang="ts" setup></script>

<style lang="less" scoped>
  .footer {
    display: flex;
    align-items: center;
    justify-content: center;
    height: 40px;
    color: var(--color-text-2);
    text-align: center;
    background: var(--color-bg-2);
  }
</style>
