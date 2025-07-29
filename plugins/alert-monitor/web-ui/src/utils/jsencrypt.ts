///
/// Copyright (c) 2023 Huawei Technologies Co.,Ltd.
///

import JsEncrypt from "jsencrypt";
import platformRequest from "../request/platform";

interface KeyValue {
    [key: string]: any;
}
interface Res {
    data: KeyValue;
}

const isEncryptedData = (data: string | null) => {
    if (!data) return false;
    // Check Base64 format (encrypted data is usually Base64 encoded)
    const base64Check = /^(?=.*[+/=])[A-Za-z0-9+/=]+$/.test(data) &&
        data.length % 4 === 0 &&
        /^(?:[A-Za-z0-9+/]{4})*(?:[A-Za-z0-9+/]{2}==|[A-Za-z0-9+/]{3}=)?$/.test(data)
    return base64Check;
}

// host password encryption
export async function encryptPassword(pwd: string) {
    if (!pwd) {
        return
    }
    const isEncryptTxt = isEncryptedData(pwd);
    if (isEncryptTxt) {
        return pwd;
    }
    let publicKey = "";
    const getPublicKey: KeyValue = await getEntryKey();
    if (Number(getPublicKey.code) === 200 && getPublicKey.key) {
        const newKey = getPublicKey.key;
        publicKey = newKey;
    }
    const encryptor = new JsEncrypt();
    encryptor.setPublicKey(publicKey);
    return encryptor.encrypt(pwd);
}
const getEntryKey: KeyValue = (data) => {
    return platformRequest
        .getNative("/encryption/getKey", {})
        .then(function (res: Res) {
            return res.data;
        })
        .catch(function (res) { });
};
