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

import axios from 'axios'

export interface MessageRecord {
  id: number;
  type: string;
  title: string;
  subTitle: string;
  avatar?: string;
  content: string;
  time: string;
  status: 0 | 1;
  messageType?: number;
}
export type MessageListType = MessageRecord[];

export function queryMessageList () {
  return axios.post<MessageListType>('/api/message/list')
}

interface MessageStatus {
  ids: number[];
}

export function setMessageStatus (data: MessageStatus) {
  return axios.post<MessageListType>('/api/message/read', data)
}

export interface ChatRecord {
  id: number;
  username: string;
  content: string;
  time: string;
  isCollect: boolean;
}

export function queryChatList () {
  return axios.post<ChatRecord[]>('/api/chat/list')
}
