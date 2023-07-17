import { ElMessageBox } from 'element-plus';
import type { Action } from 'element-plus';
import router from '@/router/index';
import { i18n } from '@/i18n/index';
import pinia from '@/store/index';
import { useAppStore } from '@/store/modules/app';
import { useTagsViewStore } from '@/store/modules/tagsView';

const t = i18n.global.t;
export const eventQueue = {};

export const updateEventQueue = () => {
  const TagsViewStore = useTagsViewStore(pinia);
  Object.keys(eventQueue).forEach((key) => {
    if (!TagsViewStore.visitedViews.find((item) => item.id == key)) {
      delete eventQueue[key];
    }
  });
};

const checkAvailableTag = (tag) => {
  const AppStore = useAppStore(pinia);
  const tagUuid = tag.query.uuid;
  return !!AppStore.connectedDatabase.find((item) => item.uuid == tagUuid);
};

export const handleEventQueueProcedure = async (tag) => {
  const key = tag.id;
  if (tag.name == 'table' && eventQueue[key] && checkAvailableTag(tag)) {
    router.push(tag.fullPath);
    return await ElMessageBox.confirm(
      t('message.saveData', { name: tag.query.fileName }),
      'Warning',
      {
        confirmButtonText: t('common.save'),
        cancelButtonText: t('common.notSave'),
        type: 'warning',
        distinguishCancelAndClose: true,
      },
    )
      .then(async () => {
        await eventQueue[key]();
        return Promise.resolve();
      })
      .catch((action: Action) => {
        if (action === 'cancel') {
          return Promise.resolve();
        } else {
          return Promise.reject();
        }
      });
  }
};
