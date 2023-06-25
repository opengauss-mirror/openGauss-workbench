import request from '@/request';

export async function getAlertConf(): Promise<any> {
    return request.get("/alertCenter/api/v1/alertConf")
}