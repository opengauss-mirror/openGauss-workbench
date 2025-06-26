import JSEncrypt from 'jsencrypt/bin/jsencrypt.min'
import { getEntryKey } from '@/api/detail'

const isEncryptedData = (data) => {
  if (!data) return false;
  // Check Base64 format (encrypted data is usually Base64 encoded)
  const base64Pattern = /^[A-Za-z0-9+/=]+$/;
  const base64Check = base64Pattern.test(data);
  return base64Check;
}

// host password encryption
export async function encryptPassword (pwd) {
  const isEncryptTxt = isEncryptedData(pwd);
  if (isEncryptTxt) {
    return pwd;
  }
  let publicKey = ''
  const getPublicKey = await getEntryKey()
  if (Number(getPublicKey.code) === 200 && getPublicKey.key) {
    const newKey = getPublicKey.key
    publicKey = newKey
  }
  const encryptor = new JSEncrypt()
  encryptor.setPublicKey(publicKey)
  return encryptor.encrypt(pwd)
}

export async function initPublicKey() {
  try {
    const res = await getEntryKey()
    if (Number(res.code) === 200 && res.key) {
      return res.key
    }
  } catch (error) {
    console.error('Failed to load public key:', error)
    return ''
  }
}
