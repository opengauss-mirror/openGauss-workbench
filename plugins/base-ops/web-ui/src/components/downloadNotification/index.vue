
<template>
  <div v-if="notificationOverlay" class="notifition-overlay"></div>
</template>
<script setup lang="ts">
import { h, ref, reactive, onUnmounted } from 'vue'
import { ElNotification, ElProgress, ElMessage, ElMessageBox } from 'element-plus'
import SvgIcon from '@/components/svg-icon'
import { useI18n } from 'vue-i18n'

interface propsType {
  percentage: number,
  fileName?: string,
  descriptions?: string,
}
const props = defineProps({
  msg: {
    type: String,
    default: ''
  },
  iconClass: {
    type: String,
    default: 'rar-file'
  }
})

const { t } = useI18n()
const percentNum = ref(0)

const notifitionName = ref('')

// Save the overlay box in the lower right corner
const boxRefs: any = ref()
const notificationOverlay = ref(false)

const createOrUpdateNotification = (downloadWsId: string, percent: any, name?: string) => {
  if (isNaN(Number(percent))) {
    return
  }
  percentNum.value = Math.round(percent * 100)
  if (!notifitionName.value) {
    notifitionName.value = name
  }
  // Determine whether to create new here
  if (!boxRefs.value) {
    createNotification(downloadWsId, percent, name)
  }
}

// provide notification
const createNotification = async (downloadWsId: string, percent: any, name?: string) => {
  const notification = await ElNotification(
    {
      duration: 0,
      position: 'bottom-right',
      showClose: false,
      customClass: '--o-download-notification',
      message: () => h('div', null, [
        h('div', { class: 'notify-header' }, [
          h(
            SvgIcon, {
              'icon-class': props.iconClass
            }
          ),
          h('span', { style: 'margin-left: 14px;font-weight: 600;' }, t('operation.task.step1.index.onlineDownload'))
        ]),
        h('div', { class: 'download-notify-content' }, [
          h('div', { class: 'download-title' }, props.msg),
          h('div', { class: 'download-name' }, notifitionName.value),
          h(ElProgress, {
            percentage: percentNum.value
          })
        ])
      ])
    }
  )
  notificationOverlay.value = true
  boxRefs.value = notification
}

const closeNotifiCation = () => {
  let timer = setTimeout(() => {
    if (boxRefs.value) {
      boxRefs.value.close()
      boxRefs.value = null
    }
    notificationOverlay.value = false
    notifitionName.value = ''
    clearTimeout(timer)
  }, 1000)
}

onUnmounted(() => {
  closeNotifiCation()
})

defineExpose({ createNotification, createOrUpdateNotification, closeNotifiCation })
</script>
<style lang="less">
// Floating box style in the lower-right corner
.--o-download-notification {
  width: 320px;
  height: fit-content;
  padding: 0 !important;
  border: 0;
  border-radius: 4px;
  left: 50%;
  top: 50%;
  transform: translate(-50%, -50%);
  .el-notification__group {
    .el-notification__content {
      height: fit-content;
      min-height: 142px;
      color: var(--o-text-color-primary);
      .notify-header {
        line-height: 16px;
        padding: 12px 24px;
        background: var(--o-bg-color-light);
      }
      .download-notify-content {
        display: flex;
        flex-direction: column;
        padding: 12px 24px;
        .download-title {
          margin-bottom: 12px;
          color: var(--o-text-color-secondary);
        }
        .download-name {
          min-height: 16px;
          margin-bottom: 8px;
        }
        .el-progress {
          height: 16px;
          margin-bottom: 10px;
          .el-progress__text {
            color: var(--o-text-color-primary);
            min-width: 24px;
            width: fit-content;
          }
        }
      }

    }
  }
}
// Mask layer style
.notifition-overlay {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: rgba(0,0,0,0.5);
  z-index: 1000;
}
</style>
