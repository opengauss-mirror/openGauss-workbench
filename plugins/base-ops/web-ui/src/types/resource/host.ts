export interface hostData {
    publicIp: string,
    privateIp: string,
    password: string,
    azId: string,
    remark: string
}
export interface hostUserData {
    hostId: string,
    username: string,
    password: string
}