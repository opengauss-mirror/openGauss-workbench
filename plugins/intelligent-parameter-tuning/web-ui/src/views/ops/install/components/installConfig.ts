import { markRaw } from 'vue'

import ChooseVersion from './ChooseVersion.vue'
import ChooseInstallWay from './ChooseInstallWay.vue'
import OfflineInstall from './OfflineInstall.vue'
import OnlineInstall from './OnlineInstall.vue'
import InstallStep from './InstallStep.vue'

type stringkey = Record<string, any>

const firstStep = markRaw(ChooseVersion)
const copyStep = markRaw(InstallStep)

const chooseStep: stringkey = {
  ChooseVersion: {
    children: {
      name: 'ChooseInstallWay',
      comp: markRaw(ChooseInstallWay)
    },
    parent: {
      name: 'ChooseVersion',
      comp: markRaw(ChooseVersion)
    }
  },
  ChooseInstallWay: {
    children: {
      name: 'InstallStep',
      comp: markRaw(InstallStep)
    },
    parent: {
      name: 'ChooseVersion',
      comp: markRaw(ChooseVersion)
    }
  },
  InstallStep: {
    children: {     
      OFF_LINE: {
        name: 'OfflineInstall',
        comp: markRaw(OfflineInstall)
      },
      ON_LINE: {
        name: 'OnlineInstall',
        comp: markRaw(OnlineInstall)
      }},
    parent: {
      name: 'ChooseInstallWay',
      comp: markRaw(ChooseInstallWay)
    }
  }
}
export { firstStep, chooseStep, copyStep }