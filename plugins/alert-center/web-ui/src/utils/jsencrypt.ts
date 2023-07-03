import JsEncrypt from "jsencrypt";
import platformRequest from "@/request/platform";

interface KeyValue {
    [key: string]: any;
}
interface Res {
    data: KeyValue;
}
// host password encryption
export async function encryptPassword(pwd: string) {
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
const getEntryKey: KeyValue = () => {
    return platformRequest
        .getNative("/encryption/getKey", {})
        .then(function (res: Res) {
            return res.data;
        })
        .catch(function (res) {});
};
