import JSEncrypt from 'jsencrypt/bin/jsencrypt.min'
import { getEntryKey } from '@/api/ops'
import { KeyValue } from '@/types/global'

// host password encryption
export async function encryptPassword(pwd: string) {
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
