import JSEncrypt from 'jsencrypt/bin/jsencrypt.min'
import {getEntryKey, getLoginKey} from '@/api/ops'
import { KeyValue } from '@/types/global'
import {getPublckey, setPublckey} from "@/utils/auth";

export async function encrypt(txt: any) {
  let publicKey = ''
  const getPublicKey: KeyValue = await getLoginKey()
  if (Number(getPublicKey.code) === 200 && getPublicKey.msg) {
    const newKey = getPublicKey.msg
    publicKey = newKey
    setPublckey(newKey)
  }
  const encryptor = new JSEncrypt()
  encryptor.setPublicKey(publicKey) // set public key
  return encryptor.encrypt(txt) // encrypt data
}

export function encryptWithPublicKey(txt: any) {
  const publicKey = getPublckey()
  const encryptor = new JSEncrypt()
  encryptor.setPublicKey(publicKey)
  const result = encryptor.encrypt(txt)
  if (result === false) {
    console.error("Encryption failed. Last error:", encryptor.lastError);
  }
  return result
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

