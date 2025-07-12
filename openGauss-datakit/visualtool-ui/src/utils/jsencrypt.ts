import JSEncrypt from 'jsencrypt/bin/jsencrypt.min'
import { getEntryKey, getDecryptKey } from '@/api/ops'
import { KeyValue } from '@/types/global'

const publicKey = 'MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQDxRJAwJGMHBOKHW3nHFHaXylHy\n' +
  't+ZrXNDOsbbZqCwgfTGrE9Jw6A+WQY/jiDEqThuMKLhb7z8//w6fmPu0kLdAmc1n\n' +
  'qkmozOOVt+1BgRqsVckJjw7eiRflqsDZEIgtnUMX61eyMcIGPHzxdL3XmlaPf1Qp\n' +
  'gPXBWQaNl4I7qJ2LoQIDAQAB'

const privateKey = 'MIICeAIBADANBgkqhkiG9w0BAQEFAASCAmIwggJeAgEAAoGBAPFEkDAkYwcE4odb\n' +
  'eccUdpfKUfK35mtc0M6xttmoLCB9MasT0nDoD5ZBj+OIMSpOG4wouFvvPz//Dp+Y\n' +
  '+7SQt0CZzWeqSajM45W37UGBGqxVyQmPDt6JF+WqwNkQiC2dQxfrV7IxwgY8fPF0\n' +
  'vdeaVo9/VCmA9cFZBo2XgjuonYuhAgMBAAECgYEAq7rZxuqfcgeQFjiOXZ27LB/e\n' +
  'ZJ1xbUoLdpQYSqThg96Y0+SwDZ2gOptAB/yQwkQGZ6U0VHve0XaCuibyQnwfcoI7\n' +
  'ZzqxqGnna2+WL2lOePWGuHn7Yub2Aw+94FRPti6F5Nms37XJ6NQ2XikmeXZJvU8G\n' +
  'bO2UiceBHone54i9eAECQQD4qxfelo/+UmxbYtokak+nenV3+jr+SEkOhNN9jLVf\n' +
  'OvrPQ4mTEgTLCxd8Wp7aN9Q9vc83H6woPz6i1TwaLV9RAkEA+GGcuYTOSJM9V8pJ\n' +
  '3KJ55gWfSBlw/yZPQ9od+OzsjUE2nq54mNbpWb3KhNpF5TNj4m2tMyFWJSwTwftF\n' +
  'JSFzUQJAfUQDFDgYdW8j8q1LYojDc4S25CeDzFCxrMSwnVBilYRqkDpfdVzgWUBP\n' +
  'Jm+oEhmJq0iQuB4WZXfmn7R3QGCW4QJBAItnxd3+IwVq1oAigmg6LO6kcyy1Us1y\n' +
  'BAUSM8ZVu8LgOja/t+IYpSoAMt1z4Mzulf4tDovnBwGgBorWa42Wg7ECQQD0myzE\n' +
  'TM2NJQxFKSo4FEogYH6OOK236xooe95pzwxiSy/3tdDU3C2ohAcRJvUU839BXvyO\n' +
  'zexW0T/Mnc5U6X9+'

// encryption
export function encrypt (txt: any) {
  const encryptor = new JSEncrypt()
  encryptor.setPublicKey(publicKey) // set public key
  return encryptor.encrypt(txt) // encrypt data
}

// decrypt
export function decrypt (txt: any) {
  const isEncryptTxt = isEncryptedData(txt);
  if (!isEncryptTxt) return txt;
  const encryptor = new JSEncrypt()
  encryptor.setPrivateKey(privateKey) // set private key
  return encryptor.decrypt(txt) // decrypt the data
}

const isEncryptedData = (data: string | null) => {
  if (!data) return false;
  // Check Base64 format (encrypted data is usually Base64 encoded)
  const base64Pattern = /^[A-Za-z0-9+/=]+$/;
  const base64Check = base64Pattern.test(data);
  return base64Check;
}

// host password encryption
export async function encryptPassword (pwd: string) {
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