import { defineStore } from 'pinia'
import { OpenLookengInstallConfig, MinimalistInstallConfig, LiteInstallConfig, EnterpriseInstallConfig, OpenGaussVersionEnum, InstallModeEnum, DeployTypeEnum, WsConnectTypeEnum } from '@/types/ops/install'
import { KeyValue } from '@antv/x6/lib/types'

export const installStore = defineStore('install', {
  state: () => {
    return {
      installContext: {
        // only for ui use
        id: '',
        installType: 'install',
        installOs: 'CENTOS_X86_64',
        openGaussVersion: OpenGaussVersionEnum.LITE,
        openGaussVersionNum: '5.0.0',
        installMode: InstallModeEnum.OFF_LINE,
        packageId: '',
        packagePath: '',
        packageName: '',
        installPackagePath: '',
        deployType: DeployTypeEnum.SINGLE_NODE,
        isEnvSeparate: false,
        envPath: '',
        clusterId: '',
        clusterName: ''
      },
      installPackageHasDownload: [] as any[],
      minimalistInstallConfig: {} as MinimalistInstallConfig,
      liteInstallConfig: {} as LiteInstallConfig,
      enterpriseInstallConfig: {} as EnterpriseInstallConfig,
      openLookengInstallConfig: {} as OpenLookengInstallConfig
    }
  },

  getters: {
    getInstallConfig: state => state.installContext,
    getMiniConfig: state => state.minimalistInstallConfig,
    getLiteConfig: state => state.liteInstallConfig,
    getEnterpriseConfig: state => state.enterpriseInstallConfig,
    getOpenLookengInstallConfig: state => state.openLookengInstallConfig,
    getHasDownload: state => state.installPackageHasDownload,
    getInstallParam: (state): KeyValue => {
      const param = {
        installContext: {
          minimalistInstallConfig: {},
          liteInstallConfig: {},
          enterpriseInstallConfig: {}
        },
        wsConnectType: WsConnectTypeEnum.COMMAND_EXEC,
        businessId: 'installLog'
      }
      if (state.installContext.openGaussVersion === OpenGaussVersionEnum.MINIMAL_LIST) {
        param.installContext.minimalistInstallConfig = state.minimalistInstallConfig
      } else if (state.installContext.openGaussVersion === OpenGaussVersionEnum.LITE) {
        param.installContext.liteInstallConfig = state.liteInstallConfig
      } else {
        param.installContext.enterpriseInstallConfig = state.enterpriseInstallConfig
      }
      Object.assign(param.installContext, state.installContext)
      return param
    }
  },

  actions: {
    setInstallContext(installInfo: KeyValue) {
      this.$patch(state => {
        Object.assign(state.installContext, installInfo)
      })
    },
    addDownloadInstallPackage(installPackageUrl: string) {
      this.$patch(state => {
        state.installPackageHasDownload.push(installPackageUrl)
      })
    },
    setMiniConfig(config: MinimalistInstallConfig) {
      this.$patch(state => {
        const param = {
          clusterName: config.clusterName
        }
        this.setInstallContext(param)
        state.minimalistInstallConfig.port = config.port
        state.minimalistInstallConfig.databaseUsername = config.databaseUsername
        state.minimalistInstallConfig.databasePassword = config.databasePassword
        state.minimalistInstallConfig.installPackagePath = config.installPackagePath
        state.minimalistInstallConfig.nodeConfigList = []
        state.minimalistInstallConfig.nodeConfigList = config.nodeConfigList
      })
    },
    setLiteConfig(config: LiteInstallConfig) {
      this.$patch(state => {
        state.liteInstallConfig.port = config.port
        state.liteInstallConfig.databaseUsername = config.databaseUsername
        state.liteInstallConfig.databasePassword = config.databasePassword
        state.liteInstallConfig.installPackagePath = config.installPackagePath
        state.liteInstallConfig.nodeConfigList = []
        state.liteInstallConfig.nodeConfigList = config.nodeConfigList
      })
    },
    setEnterpriseConfig(config: EnterpriseInstallConfig) {
      this.$patch(state => {
        Object.assign(state.enterpriseInstallConfig, config)
      })
    },
    setOpenLookengConfig(config: OpenLookengInstallConfig) {
      this.$patch(state => {
        Object.assign(state.openLookengInstallConfig, config)
      })
    },
    resetInstall() {
      this.$reset()
    }
  }
})

export default installStore
