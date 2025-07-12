import JSEncrypt from 'jsencrypt/bin/jsencrypt.min'
import { getEntryKey, getDecryptKey } from '@/api/ops'
import { KeyValue } from '@/types/global'

const isEncryptedData = (data: string | null) => {
  if (!data) return false;
  // Check Base64 format (encrypted data is usually Base64 encoded)
  const base64Pattern = /^[A-Za-z0-9+/=]+$/;
  const base64Check = base64Pattern.test(data);
  return base64Check;
}
// host password encryption
export async function encryptPassword (pwd: string) {
  if (!pwd) {
    return
  }
  const isEncryptTxt = isEncryptedData(pwd);
  if (isEncryptTxt) {
    return pwd;
  }
  let publicKey = ''
  const getPublicKey: KeyValue = await getEntryKey()
  if (Number(getPublicKey.code) === 200 && getPublicKey.key) {
    const newKey = getPublicKey.key
    publicKey = newKey
  }
  const encryptor = new JSEncrypt()
  encryptor.setPublicKey(publicKey)
  return encryptor.encrypt(pwd)

}

// host password decryption
export async function decryptPassword (encryPwd: string) {
  const isEncryptTxt = isEncryptedData(encryPwd);
  if (!isEncryptTxt) return encryPwd;
  const res: KeyValue = await getDecryptKey(encryPwd);
  return res.password // decrypt the data
}