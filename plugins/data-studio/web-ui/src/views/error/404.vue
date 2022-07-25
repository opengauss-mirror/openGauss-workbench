<!-- Copyright(c) vue-admin-perfect(zouzhibin). -->
<template>
  <u-container-layout>
    <div class="error-page">
      <div class="img">
        <img :src="fourPng" style="width: 100%" />
      </div>
      <div>
        <div class="oops">{{ $t('common.sorry') }}!</div>
        <div class="text">{{ $t('common.pageNotFound') }}...</div>
        <div style="margin-bottom: 20px; font-size: 13px; color: grey">
          {{ $t('common.checkURL') }}
        </div>
        <el-button type="primary" @click="goBackHome">{{ $t('common.backHome') }}</el-button></div
      >
    </div>
  </u-container-layout>
</template>

<script lang="ts" setup>
  import fourPng from '@/assets/image/error/404s.png';
  import { useRouter } from 'vue-router';
  const router = useRouter();

  const goBackHome = () => {
    router.push({
      path: '/',
    });
  };
</script>

<style lang="scss" scoped>
  .error-page {
    height: 100%;
    display: flex;
    align-items: center;
    .img {
      width: 500px;
    }
    .oops {
      font-size: 32px;
      line-height: 40px;
      color: #1482f0;
      margin-bottom: 20px;
      -webkit-animation-fill-mode: forwards;
      animation-fill-mode: forwards;
    }
    .text {
      font-size: 20px;
      line-height: 24px;
      color: #222;
      margin-bottom: 10px;
      -webkit-animation-delay: 0.1s;
      animation-delay: 0.1s;
      -webkit-animation-fill-mode: forwards;
      animation-fill-mode: forwards;
    }
  }
</style>
