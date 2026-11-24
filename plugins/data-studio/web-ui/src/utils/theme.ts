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

import { getDarkColor, getLightColor } from '@/utils/index';

export const changePrimary = (isDark) => {
  const primaryColor = isDark ? '#fcef92' : '#e41d1d';
  // deep color
  document.documentElement.style.setProperty(
    '--el-color-primary-dark-2',
    `${getDarkColor(primaryColor, 0.1)}`,
  );
  document.documentElement.style.setProperty('--el-color-primary', primaryColor);
  // light color
  for (let i = 1; i <= 9; i++) {
    document.documentElement.style.setProperty(
      `--el-color-primary-light-${i}`,
      `${getLightColor(primaryColor, i / 10)}`,
    );
  }
};
