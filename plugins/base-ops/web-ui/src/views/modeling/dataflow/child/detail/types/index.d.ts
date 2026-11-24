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

import { Addon } from '@antv/x6'
export interface PropsOptions extends Addon.Stencil.Options {
  containerId: string,
  title?: string,
  target?: Graph | Scroller;
  stencilGraphWidth?: number,
  stencilGraphHeight?: number,
  getDragNode?: (sourceNode: Node, options: GetDragNodeOptions) => Node,
  getDropNode?: (draggingNode: Node, options: GetDropNodeOptions) => Node,
  stencil: {
    title?: string,
    groups?: Array<Group>,
    nodes: Array<{ name?: string, groupType?: string, icon?: string, child: Array<Node.Metadata> }>
  },
  canvasOption?: Partial<Options.Manual>
}
