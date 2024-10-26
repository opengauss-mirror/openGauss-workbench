<template>
  <div class="app-container" id="migrate">
    <div class="main-bd">
      <div class="upgrade-container">
        <svg-icon icon-class="developing" class="icon-size mb"></svg-icon>
        <div class="content ft-lg">{{ $t('protocol.index.5mpixm5ckcw0') }}</div>
      </div>
    </div>
  </div>
</template>

<script lang="ts" setup>
</script>

<style lang="less" scoped>
.upgrade-container {
  position: absolute;
  left: 0;
  top: 0;
  width: 100%;
  height: 100%;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;

  .icon-size {
    width: 300px;
    height: 300px;
  }

  .content {
    font-weight: bold;
    color: var(--color-neutral-4);
  }
}
</style>
