<template>
  <div class="login-container" id="login">
    <div class="logo">
      <img alt="logo" src="@/assets/images/login-logo.png" />
      <div class="logo-text">{{$t('login.index.5mpi5tn6irc0')}}</div>
    </div>
    <div class="content">
      <div class="content-inner">
        <LoginForm />
      </div>
    </div>
  </div>
</template>

<script lang="ts" setup>
  import LoginForm from './components/login-form.vue'
</script>

<style lang="less" scoped>
  .login-container {
    display: flex;
    height: 100vh;
    background: #040516 url('~@/assets/images/bg.jpg') no-repeat center center;
    .content {
      position: relative;
      display: flex;
      flex: 1;
      align-items: center;
      justify-content: center;
      .content-inner {
        padding-left: 500px;
      }
    }
  }

  .logo {
    position: fixed;
    top: 75px;
    left: 75px;
    z-index: 1;
    display: inline-flex;
    align-items: center;

    &-text {
      margin-right: 4px;
      margin-left: 10px;
      color: var(--color-fill-1);
      font-size: 20px;
    }
  }
</style>
