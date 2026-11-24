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

export const uuid = () => Math.random().toString(36).slice(2);

export const toFixed = (n: number | string, r: number = 2) => (Number.parseInt(`${Number.parseFloat(`${n}`) * (10 ** r)}`) / (10 ** r)).toFixed(r);

export const downloadCsvFile = (filename: string, title: string, data: Record<any, string | number>[]) => {
    let str = `${title}\n`;
    data.forEach(d => {
        for (const k in d) {
            str += `${d[k]}\t,`
        }
        str += `\n`
    })
    const link = document.createElement("a")
    link.href = 'data:text/csv;charset=utf-8,\ufeff' + encodeURIComponent(str)
    link.download = filename
    link.click()
    link.remove()
}
