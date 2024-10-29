<template>
  <div class="app-container" id="batchInstall">
    <div class="main-bd">
      <div class="upgrade-container">
        <svg-icon icon-class="developing" class="icon-size mb"></svg-icon>
        <div class="content ft-lg">{{ $t('upgrade.index.5mpn9zse2vc0') }}</div>
      </div>
    </div>
  </div>
</template>

<script lang="ts" setup>
</script>

<style lang="less" scoped>
.app-container {
  .main-bd {
    position: relative;

    .upgrade-container {
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
  }
}
</style>
