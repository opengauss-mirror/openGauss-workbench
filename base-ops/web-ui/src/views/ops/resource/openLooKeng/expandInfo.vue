<template>
  <div class="expand-info">
    <div class="expand-item">
      <span class="title">{{ $t('components.openLooKeng.5mpiji1qpcc86') }}</span><br/>
      {{ $t('components.openLooKeng.5mpiji1qpcc87') }}: {{ record.dadPkgName }}<br/>
      {{ $t('components.openLooKeng.5mpiji1qpcc88') }}: {{ record.dadInstallIp }}<br/>
      {{ $t('components.openLooKeng.5mpiji1qpcc89') }}: {{ record.dadInstallUsername }}<br/>
      {{ $t('components.openLooKeng.5mpiji1qpcc90') }}: {{ record.dadInstallPath }}<br/>
      {{ $t('components.openLooKeng.5mpiji1qpcc91') }}: {{ record.dadUploadPath }}<br/>
      {{ $t('components.openLooKeng.5mpiji1qpcc92') }}: {{ record.dadPort }}
    </div>
    <div class="expand-item">
      <span class="title">{{ $t('components.openLooKeng.5mpiji1qpcc93') }}</span><br/>
      {{ $t('components.openLooKeng.5mpiji1qpcc87') }}: {{ record.zkPkgName }}<br/>
      {{ $t('components.openLooKeng.5mpiji1qpcc88') }}: {{ record.ssInstallIp }}<br/>
      {{ $t('components.openLooKeng.5mpiji1qpcc89') }}: {{ record.ssInstallUsername }}<br/>
      {{ $t('components.openLooKeng.5mpiji1qpcc90') }}: {{ record.ssInstallPath }}<br/>
      {{ $t('components.openLooKeng.5mpiji1qpcc91') }}: {{ record.ssUploadPath }}<br/>
      {{ $t('components.openLooKeng.5mpiji1qpcc92') }}: {{ record.zkPort }}
    </div>
    <div class="expand-item">
      <span class="title">{{ $t('components.openLooKeng.5mpiji1qpcc94') }}</span><br/>
      {{ $t('components.openLooKeng.5mpiji1qpcc87') }}: {{ record.ssPkgName }}<br/>
      {{ $t('components.openLooKeng.5mpiji1qpcc88') }}: {{ record.ssInstallIp }}<br/>
      {{ $t('components.openLooKeng.5mpiji1qpcc89') }}: {{ record.ssInstallUsername }}<br/>
      {{ $t('components.openLooKeng.5mpiji1qpcc90') }}: {{ record.ssInstallPath }}<br/>
      {{ $t('components.openLooKeng.5mpiji1qpcc91') }}: {{ record.ssUploadPath }}<br/>
      {{ $t('components.openLooKeng.5mpiji1qpcc92') }}: {{ record.ssPort }}
    </div>
    <div class="expand-item">
      <span class="title">{{ $t('components.openLooKeng.5mpiji1qpcc95') }}</span><br/>
      {{ $t('components.openLooKeng.5mpiji1qpcc87') }}: {{ record.olkPkgName }}<br/>
      {{ $t('components.openLooKeng.5mpiji1qpcc88') }}: {{ record.olkInstallIp }}<br/>
      {{ $t('components.openLooKeng.5mpiji1qpcc89') }}: {{ record.olkInstallUsername }}<br/>
      {{ $t('components.openLooKeng.5mpiji1qpcc90') }}: {{ record.olkInstallPath }}<br/>
      {{ $t('components.openLooKeng.5mpiji1qpcc91') }}: {{ record.olkUploadPath }}<br/>
      {{ $t('components.openLooKeng.5mpiji1qpcc92') }}: {{ record.olkPort }}
    </div>
  </div>
</template>
<script setup lang="ts">
import {useI18n} from 'vue-i18n'

const {t} = useI18n()
const props = defineProps({
  record: {
    type: Object,
    required: true
  }
})
</script>
<style lang="less" scoped>
.title {
  font-weight: 550;
}
.expand-info {
  margin-left: 80px;
  display: flex;
  justify-content: space-between;
  .expand-item {
    flex: 1;
    margin-right: 20px;
    text-align: left;
  }
}
</style>
