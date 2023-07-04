import request from '@/request';

export async function checkPrometheus(): Promise<any> {
    return request.get("/api/v1/environment/checkPrometheus")
}