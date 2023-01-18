import axios from "axios";

export type OpenGaussNode = {
    dbName: string;
    dbType: string;
    dbUser: string;
    dbUserPassword: string;
    id: string;
    instanceId: string;
    ip: string;
    port: number;
    serverId: string;
}

export type ServerInfo = {
    id: string;
    ip: string;
    os: string;
    port: number;
    userName: string;
    userPassword: string;
}

export type OpenGaussInstance = {
    dbVersion: string;
    id: string;
    name: string;
    nodeInfo: Partial<OpenGaussNode>[],
    remark: string;
    serverInfoReq: Partial<ServerInfo>;
    serverInfoResp?: Partial<ServerInfo>;
    type: string;
    vip: string;
    port: number;
}

export async function getList(keyword?: string, page?: number, limit?: number): Promise<Partial<OpenGaussInstance>[]> {
    let resp = await axios.post('/sql-diagnosis/api/v1/instance/list', { keyword, page, limit });
    return resp.data.data;
}

export async function getDetail(id: string): Promise<Partial<OpenGaussInstance>> {
    let resp = await axios.get(`/sql-diagnosis/api/v1/instance/detail/${id}`);
    return resp.data.data;
}

export async function deleteInstance(id: string): Promise<boolean> {
    let resp = await axios.post('/sql-diagnosis/api/v1/instance/delete', [id]);
    return resp.data.data;
}

export async function addInstance(entry: Partial<OpenGaussInstance>): Promise<Partial<OpenGaussInstance>> {
    let resp = await axios.post('/sql-diagnosis/api/v1/instance/add', entry);
    return resp.data.data;
}

export async function updateInstance(entry: Partial<OpenGaussInstance>): Promise<Partial<OpenGaussInstance>> {
    let resp = await axios.post(`/sql-diagnosis/api/v1/instance/update/${entry.id}`, entry);
    return resp.data.data;
}

export async function testConnection(dbNode: Partial<OpenGaussNode>): Promise<boolean> {
    let resp = await axios.post('/sql-diagnosis/api/v1/instance/connect', dbNode);
    return resp.data.code === '200' || resp.data.code === 200;
}
