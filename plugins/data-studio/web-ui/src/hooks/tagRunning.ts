import { ElMessage } from 'element-plus';
import { i18n } from '@/i18n/index';
import pinia from '@/store/index';
import { useTagsViewStore } from '@/store/modules/tagsView';

const t = i18n.global.t;
export const queue = new Set();

export const refreshRunningTags = () => {
  const TagsViewStore = useTagsViewStore(pinia);
  for (const key of queue) {
    if (!TagsViewStore.visitedViews.find((item) => item.id == key)) {
      queue.delete(key);
    }
  }
};

export const changeRunningTagStatus = (tagId: string | number, isRunning: boolean): void => {
  if (isRunning) {
    !queue.has(tagId) && queue.add(tagId);
  } else {
    queue.delete(tagId);
  }
};

export const hasRunningTagStatus = (tagId: string | number): boolean => {
  return queue.has(tagId);
};

export const deleteRunningTag = (tagId: string | number): boolean => {
  if (queue.has(tagId)) {
    ElMessage.error(t('message.debugCannotFinish'));
    return false;
  } else {
    changeRunningTagStatus(tagId, false);
    return true;
  }
};
