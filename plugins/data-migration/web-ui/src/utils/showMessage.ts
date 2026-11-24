/*
 * Copyright (c) 2026 Huawei Technologies Co., Ltd.
 * openGauss-workbench is licensed under Mulan PSL v2.
 * You can use this software according to the terms and conditions of the Mulan PSL v2.
 * You may obtain a copy of Mulan PSL v2 at:
 *          http://license.coscl.org.cn/MulanPSL2
 * THIS SOFTWARE IS PROVIDED ON AN "AS IS" BASIS, WITHOUT WARRANTIES OF ANY KIND,
 * EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO NON-INFRINGEMENT,
 * MERCHANTABILITY OR FIT FOR A PARTICULAR PURPOSE.
 * See the Mulan PSL v2 for more details.
 */

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
