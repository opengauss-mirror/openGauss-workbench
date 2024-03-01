import ogRequest from '@/request'

export type TemplateList = {
  id: string
  name: string
  description: string
}
export async function getTemplates(): Promise<void | null | TemplateList[]> {
  return ogRequest.get('/collectConfig/api/v1/templates')
}

export type TemplateDetailDetail = {
  metricGroupKey: string
  metricGroupName: string
  metricGroupDescription: string
  interval: string
  time: String
  unit: String
  isEnable: boolean
}
export type TemplateDetail = {
  templateId: number | null
  templateName: string | null
  details: TemplateDetailDetail[]
}
export async function getTemplateDetail(templateId: string | number): Promise<void | null | TemplateDetail> {
  return ogRequest.get('/collectConfig/api/v1/templates/' + templateId)
}
export async function getTemplateDetailByNodeId(nodeId: string): Promise<void | null | TemplateDetail> {
  return ogRequest.get('/collectConfig/api/v1/templates/getDetailByNodeId/' + nodeId)
}
export interface templateNode {
  templateId: String | number
  nodeId: string
}
export async function setTemplateNodes(param: templateNode): Promise<void | null> {
  return ogRequest.post('/collectConfig/api/v1/templates/action?action=setNodeTemplate', param)
}

export type TemplateNodeDirectDetail = {
  metricKey: String
  interval: string | null
  isEnable: boolean | null
}
export interface TemplateNodeDirect {
  details: TemplateNodeDirectDetail[]
  nodeId: string
}
export async function setTemplateNodeDirect(param: TemplateNodeDirect): Promise<void | null> {
  return ogRequest.post('/collectConfig/api/v1/templates/action?action=setNodeTemplateDirect', param)
}
