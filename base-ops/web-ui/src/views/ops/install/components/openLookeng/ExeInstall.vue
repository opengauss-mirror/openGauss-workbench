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
      <div class="install-doing mb">
        <div class="ft-m ft-b mb-lg">{{ $t('simpleInstall.index.5mpn813guxc0') }}</div>
        <div class="flex-col-start full-w">
          <div class="progress-c mb">
            <a-progress :color="data.state === 1 ? 'green' : 'red'" :stroke-width="12" :percent="data.installProgress">
            </a-progress>
          </div>
        </div>
      </div>
      <div id="xterm" class="xterm"></div>
    </div>
  </div>
</template>
<script lang="ts" setup>
import { KeyValue } from '@/types/global';
import { ref, reactive, onMounted, inject } from 'vue';
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
  installProgress: 0,
})

onMounted(() => {
  loadingFunc.setBackBtnShow(false)
  loadingFunc.setNextBtnShow(false)
  const term = getTermObj()
  initTerm(term)
  autoProgress()
})

const progressInterval = ref<any>(null)

const autoProgress = () => {
  progressInterval.value = setInterval(() => {
    data.installProgress = (Number(data.installProgress) + 0.1).toFixed(2)
    if (Number(data.installProgress) === 1) {
      exeResult.value = exeResultEnum.SUCESS
      clearInterval(progressInterval.value)
    }
  }, 600)
}


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
}

const goOps = () => {
  window.$wujie?.props.methods.jump({
    name: 'Static-pluginBase-opsMonitorDailyOps'
  })
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