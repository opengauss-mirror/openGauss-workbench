import EventBus, { EventTypeName } from '@/utils/event-bus';
import { ElMessage } from 'element-plus';
import pinia from '@/store/index';
import { useAppStore } from '@/store/modules/app';
import { useUserStore } from '@/store/modules/user';
import { i18n } from '@/i18n/index';
import { reLogin } from '@/api/connect';

export async function interceptHttpDisconnection(uuid: string) {
  const AppStore = useAppStore(pinia);
  const UserStore = useUserStore(pinia);
  const t = i18n.global.t;
  const targetDbInfo = AppStore.historyConnectedDatabase.find((item) => item.uuid === uuid);
  // Whether to manually open the database due to timeout disconnection
  const isManualOpeningDb = AppStore.connectedDatabase.find((item) => item.uuid === uuid);
  const targetRootNode = AppStore.connectListMap.find((item) => item.id === targetDbInfo?.rootId);
  if (isManualOpeningDb && targetRootNode) {
    const { connectInfo } = targetRootNode;
    if (connectInfo.isRememberPassword == 'y') {
      const res = (await reLogin({
        type: connectInfo.type,
        name: connectInfo.name,
        ip: connectInfo.ip,
        port: connectInfo.port,
        dataName: connectInfo.dataName,
        userName: connectInfo.userName,
        id: connectInfo.id,
        password: undefined,
        isRememberPassword: 'y',
        webUser: UserStore.userId,
        connectionid: uuid,
      })) as unknown as {
        connectionid: string;
        id: string;
        type: string;
        name: string;
        driver: string;
        ip: string;
        port: string;
        dataName: string;
        userName: string;
        webUser: string;
        edition: string;
        isRememberPassword: 'y' | 'n';
      };
      EventBus.notify(EventTypeName.UPDATE_CONNECTINFO, res);
      return ElMessage.info(`${t('message.reconnectTips')}`);
    } else {
      EventBus.notify(EventTypeName.OPEN_ENTERPASSWORD, {
        uuid: targetDbInfo.uuid,
        connectInfo,
      });
      return;
    }
  } else {
    return ElMessage.error(`${t('message.notExistConnection')}`);
  }
}
