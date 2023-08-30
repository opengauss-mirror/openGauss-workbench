// host information
export interface hostData {
    publicIp: string,
    privateIp: string,
    password: string,
    azId: string,
    remark: string
}
// host user information
export interface hostUserData {
    hostId: string,
    username: string,
    password: string
}
