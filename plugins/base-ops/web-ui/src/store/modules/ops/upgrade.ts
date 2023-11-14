import { defineStore } from 'pinia'
import { KeyValue } from '@/types/global'

export interface upClusterInfo {
    id: string,
    currentVersion: string,
    rootPassword: string,
    diskUsed: number,
    osCheckResult: string,
    checkLoading: boolean,
    hostList: Array<hostInfo>
}

export interface hostInfo {
    isMaster: boolean,
    hostId: string,
    publicIp: string
}

export interface targetVersion {
    packageId: string,
    packageUrl: string,
    packagePath: KeyValue,
    packageVersionNum: string
}

export const useUpgradeStore = defineStore('upgrade', {
    state: () => {
        return {
            upClusterList: [] as upClusterInfo[],
            upTargetVersion: {} as targetVersion
        }
    },
    getters: {
        getUpClusterList: state => state.upClusterList,
        getUpTargetVersion: state => state.upTargetVersion
    },
    actions: {
        setUpgradeContext(upClusterList: Array<upClusterInfo>, upTargetVersion: targetVersion) {
            this.$patch(state => {
              state.upClusterList = [...upClusterList]
              Object.assign(state.upTargetVersion, upTargetVersion)
            })
        }
    }
})
