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
