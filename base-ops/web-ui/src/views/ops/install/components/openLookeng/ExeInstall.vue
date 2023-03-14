<template>
  <div class="exe-install-c">
    <div class="flex-col full-w full-h" v-if="exeResult === exeResultEnum.SUCESS">
      <svg-icon icon-class="ops-install-success" class="icon-size mb"></svg-icon>
      <div class="mb-lg">安装成功</div>
      <div class="flex-row">
        <a-button type="outline" class="mr" @click="goHome">
          返回首页
        </a-button>
        <a-button type="primary" @click="goOps">
          集群运维
        </a-button>
      </div>
    </div>
    <div class="flex-col" v-else>
      <a-steps :current="2" status="wait" class="mb full-w" type="arrow">
        <a-step description="上传安装包及脚本">
          上传
          <template #icon>
            <icon-loading/>
          </template>
        </a-step>
        <a-step description="安装Zookeeper">安装Zookeeper</a-step>
        <a-step description="安装ShardingProxy">安装ShardingProxy</a-step>
        <a-step description="安装OpenLookEng">安装OpenLookEng</a-step>
        <a-step description="启动所有服务">启动服务</a-step>
      </a-steps>
      <div class="install-doing mb">
        <div class="progress-top full-w">
          <div class="progress-c mr-s">
            <a-progress :color="data.state === 1 ? 'green' : 'red'" :stroke-width="12" :percent="data.installProgress">
            </a-progress>
          </div>
          <a-spin class="mr"/>
          <a-space class="flex-row">
            <a-button type="primary">下载日志</a-button>
            <a-button type="primary">自定义控制台</a-button>
          </a-space>
        </div>
      </div>
      <div id="xterm" class="xterm"></div>
    </div>
  </div>
</template>
<script lang="ts" setup>
import { KeyValue } from '@/types/global'
import { ref, reactive, onMounted, inject } from 'vue'
import 'xterm/css/xterm.css'
import { Terminal } from 'xterm'

enum exeResultEnum {
  UN_INSTALL = Number(-1),
  SUCESS = Number(1),
  FAIL = Number(0)
}

const exeResult = ref<number>(exeResultEnum.UN_INSTALL)

const loadingFunc = inject<any>('loading')

const data = reactive<KeyValue>({
  state: -1, // -1 un install  0 installing  1 success  2 fail
  installProgress: 0
})

onMounted(() => {
  const term = getTermObj()
  initTerm(term)
})

const getTermObj = (): Terminal => {
  return new Terminal({
    fontSize: 14,
    rows: 28,
    cols: 100,
    cursorBlink: true,
    convertEol: true,
    disableStdin: false,
    cursorStyle: 'underline',
    theme: {
      background: 'black'
    }
  })
}

const initTerm = (term: Terminal, ws?: WebSocket | undefined) => {
  term.open(document.getElementById('xterm') as HTMLElement)
  term.clear()
  term.focus()
  term.write('\r\n\x1b[33m$\x1b[0m ')
}

const goHome = () => {
  window.$wujie?.props.methods.jump({
    name: 'Dashboard'
  })
  loadingFunc.initData()
}

const goOps = () => {
  window.$wujie?.props.methods.jump({
    name: 'Static-pluginBase-opsMonitorDailyOps'
  })
  loadingFunc.initData()
}

</script>

<style lang="less" scoped>
.exe-install-c {
  padding: 20px;
  height: 100%;
  overflow-y: auto;

  .install-doing {
    width: 100%;
    display: flex;
    flex-direction: column;
    justify-content: center;
    align-items: center;

    .progress-top {
      display: flex;
      justify-content: space-between;
      align-items: center;
    }

    .progress-c {
      width: 100%;
      display: flex;
      border-radius: 3px;
      padding: 5px;
      border: 1px solid rgb(var(--primary-6));
    }
  }

  .xterm {
    width: 100%;
    height: 80%;
  }

  .icon-size {
    width: 100px;
    height: 100px;
  }
}
</style>
