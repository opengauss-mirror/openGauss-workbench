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

export function mergeObjectArray (arr1, arr2, idProp) {
  const arr1Temp = JSON.parse(JSON.stringify(arr1))
  const arr2Temp = JSON.parse(JSON.stringify(arr2))
  const mergedArr = arr1Temp.slice()

  arr2Temp.forEach(obj => {
    const index = mergedArr.findIndex(item => item[idProp] === obj[idProp])
    if (index !== -1) {
      mergedArr[index] = obj
    } else {
      mergedArr.push(obj)
    }
  })

  return mergedArr
}
