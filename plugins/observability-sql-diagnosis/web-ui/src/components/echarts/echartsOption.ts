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

export function getDefaultEcharOption() {
    const theme = localStorage.getItem('theme')
    let themeColor = {
        nodes: {
            font: {
                color: 'black',
            },
            color: {
                border: '#424242',
                background: '#424242',
                hover: {
                    border: '#fff',
                    background: '#424242',
                },
                highlight: {
                    border: '#fff',
                    background: '#424242',
                },
            },
        },
        edges: {
            color: {
                color: '#7d7d7d',
                highlight: '#424242',
                hover: 'green',
                inherit: 'from',
                opacity: 0.5,
            },
        },
    }
    if (theme === 'dark') {
        themeColor = {
            nodes: {
                font: {
                    color: 'white',
                },
                color: {
                    border: '#424242',
                    background: '#424242',
                    hover: {
                        border: '#fff',
                        background: '#424242',
                    },
                    highlight: {
                        border: '#fff',
                        background: '#424242',
                    },
                },
            },
            edges: {
                color: {
                    color: '#7d7d7d',
                    highlight: '#fff',
                    hover: 'green',
                    inherit: 'from',
                    opacity: 0.5,
                },
            },
        }
    }
    return themeColor
}
