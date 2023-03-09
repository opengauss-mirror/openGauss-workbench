import { defineStore } from 'pinia'
import { MinimalistInstallConfig, LiteInstallConfig, EnterpriseInstallConfig, OpenGaussVersionEnum, InstallModeEnum, DeployTypeEnum, WsConnectTypeEnum } from '@/types/ops/install'
import { KeyValue } from '@antv/x6/lib/types'

const installStore = defineStore('install', {
  state: () => {
    return {
      installContext: {
        installType: 'install',
        installOs: 'CENTOS_X86_64',
        openGaussVersion: OpenGaussVersionEnum.LITE,
        openGaussVersionNum: '3.0.0',
        installMode: InstallModeEnum.OFF_LINE,
        packageId: '',
        packagePath: '',
        packageName: '',
        installPackagePath: '',
        deployType: DeployTypeEnum.SINGLE_NODE,
        envPath: '',
        clusterId: '',
        clusterName: ''
      },
      minimalistInstallConfig: {} as MinimalistInstallConfig,
      liteInstallConfig: {} as LiteInstallConfig,
      enterpriseInstallConfig: {} as EnterpriseInstallConfig
    }
  },

  getters: {
    getInstallConfig: state => state.installContext,
    getMiniConfig: state => state.minimalistInstallConfig,
    getLiteConfig: state => state.liteInstallConfig,
    getEnterpriseConfig: state => state.enterpriseInstallConfig,
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
    resetInstall() {
      this.$reset()
    }
  }
})

export default installStore
