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

import { markRaw } from 'vue'

import ChooseVersion from './ChooseVersion.vue'
import ChooseInstallWay from './ChooseInstallWay.vue'
import OfflineInstall from './OfflineInstall.vue'
import OnlineInstall from './OnlineInstall.vue'
import InstallStep from './InstallStep.vue'

type stringkey = Record<string, any>

const firstStep = markRaw(ChooseVersion)

const chooseStep: stringkey = {
  ChooseVersion: {
    children: {
      name: 'ChooseInstallWay',
      comp: markRaw(ChooseInstallWay)
    },
    childrenImport: {
      name: 'InstallStep',
      comp: markRaw(InstallStep)
    },
    parent: {
      name: 'ChooseVersion',
      comp: markRaw(ChooseVersion)
    }
  },
  ChooseInstallWay: {
    children: {
      OFF_LINE: {
        name: 'OfflineInstall',
        comp: markRaw(OfflineInstall)
      },
      ON_LINE: {
        name: 'OnlineInstall',
        comp: markRaw(OnlineInstall)
      }
    },
    parent: {
      name: 'ChooseVersion',
      comp: markRaw(ChooseVersion)
    }
  },
  OfflineInstall: {
    children: {
      name: 'InstallStep',
      comp: markRaw(InstallStep)
    },
    parent: {
      name: 'ChooseInstallWay',
      comp: markRaw(ChooseInstallWay)
    }
  },
  OnlineInstall: {
    children: {
      name: 'InstallStep',
      comp: markRaw(InstallStep)
    },
    parent: {
      name: 'ChooseInstallWay',
      comp: markRaw(ChooseInstallWay)
    }
  },
  InstallStep: {
    children: null,
    parent: {
      OFF_LINE: {
        name: 'OfflineInstall',
        comp: markRaw(OfflineInstall)
      },
      ON_LINE: {
        name: 'OnlineInstall',
        comp: markRaw(OnlineInstall)
      }
    },
    parentImport: {
      name: 'ChooseVersion',
      comp: markRaw(ChooseVersion)
    }
  }
}
export { firstStep, chooseStep }