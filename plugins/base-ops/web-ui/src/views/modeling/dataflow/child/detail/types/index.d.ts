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
