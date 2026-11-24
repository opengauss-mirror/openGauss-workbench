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

export type OperateType = 'schema' | 'table' | 'function' | 'sequence' | 'view';
export type GrantOrRevoke = 'GRANT' | 'REVOKE';
export type SelectedObjectList = Array<{ showName: string; name: string; schema?: string }>;
export interface SaveParams {
  uuid: string;
  grantOrRevoke: string;
  type: string;
  obj: { name: string; schema?: string }[];
  user: string[];
  privilegeOption: { privilege: string; checkPrivilege: boolean; checkOption: boolean }[];
}
