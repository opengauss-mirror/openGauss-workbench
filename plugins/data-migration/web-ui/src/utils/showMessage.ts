import { ElMessage } from 'element-plus'
import {
  IconSuccess,
  IconAlarm,
  IconError,
  IconRemind
} from '@computing/opendesign-icons'

interface IconMap {
  info: any;
  success: any;
  warning: any;
  error: any;
}

type Status = keyof IconMap;

const icons = {
  info: IconRemind,
  success: IconSuccess,
  warning: IconAlarm,
  error: IconError
}

const showMessage = (type: Status, msg = '', duration?: number): void => {
  ElMessage({
    type,
    showClose: true,
    message: msg,
    icon: icons[type],
    customClass: `o-message--${type}`,
    duration: duration ?? 3000
  })
}

export default showMessage
