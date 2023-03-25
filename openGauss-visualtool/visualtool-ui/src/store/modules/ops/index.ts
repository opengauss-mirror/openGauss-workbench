import { defineStore } from 'pinia'
import { MinimalistInstallConfig, LiteInstallConfig, EnterpriseInstallConfig, OpenGaussVersionEnum, InstallModeEnum, DeployTypeEnum, WsConnectTypeEnum } from '@/types/ops/install'
import { KeyValue } from '@antv/x6/lib/types'

const installStore = defineStore('install', {
  state: () => {
    return {
      installContext: {
        installType: 'install',
        openGaussVersion: OpenGaussVersionEnum.LITE,
        openGaussVersionNum: '5.0.0',
        installMode: InstallModeEnum.OFF_LINE,
        packagePath: '',
        packageName: '',
        installPackagePath: '',
        deployType: DeployTypeEnum.SINGLE_NODE,
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
    getInstallParam: (state) => {
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
    // Set installation data
    setInstallContext (installInfo: KeyValue) {
      this.$patch(state => {
        Object.assign(state.installContext, installInfo)
      })
    },
    // Set Minimalist Data
    setMiniConfig (config: MinimalistInstallConfig) {
      this.$patch(state => {
        // The minimal version can only be used on one machine
        const param = {
          clusterName: config.clusterName
        }
        this.setInstallContext(param)
        state.minimalistInstallConfig.port = config.port
        state.minimalistInstallConfig.databaseUsername = config.databaseUsername
        state.minimalistInstallConfig.databasePassword = config.databasePassword
        state.minimalistInstallConfig.nodeConfigList = []
        state.minimalistInstallConfig.nodeConfigList = config.nodeConfigList
      })
    },
    setLiteConfig (config: LiteInstallConfig) {
      this.$patch(state => {
        // The lightweight version can be installed on multiple machines. If it is the same machine, just choose the same host
        state.liteInstallConfig.port = config.port
        state.liteInstallConfig.databaseUsername = config.databaseUsername
        state.liteInstallConfig.databasePassword = config.databasePassword
        state.liteInstallConfig.nodeConfigList = []
        state.liteInstallConfig.nodeConfigList = config.nodeConfigList
      })
    },
    setEnterpriseConfig (config: EnterpriseInstallConfig) {
      this.$patch(state => {
        // The lightweight version can be installed on multiple machines. If it is the same machine, just choose the same host
        Object.assign(state.enterpriseInstallConfig, config)
      })
    },
    resetInstall () {
      this.$reset()
    }
  }
})

export default installStore
