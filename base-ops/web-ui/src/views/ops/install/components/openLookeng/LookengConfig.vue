<template>
  <div class="lookeng-config-container">
    <div class="flex-col">
      <a-form :model="data.form" :rules="data.rules" auto-label-width :style="{ width: '850px' }" ref="formRef">
        <div class="ft-b mb">
          ShardingSphere配置
        </div>
        <a-form-item field="host" label="部署主机IP" validate-trigger="blur">
          <a-input v-model="data.form.host" placeholder="请输入部署主机IP" />
        </a-form-item>
        <a-form-item field="username" label="用户名" validate-trigger="blur">
          <a-input v-model="data.form.username" placeholder="请输入用户名" />
        </a-form-item>
        <a-form-item field="password" label="密码" validate-trigger="blur">
          <a-input-password v-model="data.form.password" placeholder="请输入密码" allow-clear />
        </a-form-item>
        <a-form-item field="installPath" label="安装路径" validate-trigger="blur">
          <a-input v-model="data.form.installPath" placeholder="请输入安装路径" />
        </a-form-item>
        <a-form-item field="port" label="绑定端口" validate-trigger="blur">
          <a-input v-model="data.form.port" placeholder="请输入绑定端口" />
        </a-form-item>
        <a-form-item label="配置文件" validate-trigger="blur">
          <a-upload list-type="picture-card" action="/" :default-file-list="data.form.fileList" :limit="1"
            image-preview />
        </a-form-item>
        <div class="ft-b mb">
          lookeng配置
        </div>
        <a-form-item field="host2" label="部署主机IP" validate-trigger="blur">
          <a-input v-model="data.form.host2" placeholder="请输入部署主机IP" />
        </a-form-item>
        <a-form-item field="username2" label="用户名" validate-trigger="blur">
          <a-input v-model="data.form.username2" placeholder="请输入用户名" />
        </a-form-item>
        <a-form-item field="password2" label="密码" validate-trigger="blur">
          <a-input-password v-model="data.form.password2" placeholder="请输入密码" allow-clear />
        </a-form-item>
        <a-form-item field="installPath2" label="安装路径" validate-trigger="blur">
          <a-input v-model="data.form.installPath2" placeholder="请输入安装路径" />
        </a-form-item>
        <a-form-item field="port2" label="绑定端口" validate-trigger="blur">
          <a-input v-model="data.form.port2" placeholder="请输入绑定端口" />
        </a-form-item>
        <a-form-item label="配置文件" validate-trigger="blur">
          <a-upload list-type="picture-card" action="/" :default-file-list="data.form.fileList2" :limit="1"
            image-preview />
        </a-form-item>
      </a-form>
    </div>
  </div>
</template>
<script lang="ts" setup>
import { KeyValue } from '@/types/global'
import { reactive } from 'vue'

const data = reactive<KeyValue>({
  form: {
    host: '192.168.0.0',
    username: 'shardUser',
    password: 'password',
    installPath: '/opt/software/shard',
    port: '1234',
    fileList: [],
    host2: '192.168.0.1',
    username2: 'lookengUser',
    password2: 'password',
    installPath2: '/opt/software/lookeng',
    port2: '2345',
    fileList2: []
  },
  rules: {}
})

const beforeConfirm = async (): Promise<boolean> => {
  return true
}

defineExpose({
  beforeConfirm
})

</script>
<style lang="less" scoped>
.lookeng-config-container {
  padding: 20px;
  height: 100%;
  overflow-y: auto;
}
</style>