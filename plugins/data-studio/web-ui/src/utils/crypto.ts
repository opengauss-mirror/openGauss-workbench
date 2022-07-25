import CryptoJS from 'crypto-js';

const key = CryptoJS.enc.Utf8.parse('R0JBLU5DVEktRFM='); //16b
const iv = CryptoJS.enc.Utf8.parse('1234567890000000');

export default {
  encrypt(word: string) {
    let encrypted = '';
    const srcs = CryptoJS.enc.Utf8.parse(word);
    encrypted = CryptoJS.AES.encrypt(srcs, key, {
      iv: iv,
      mode: CryptoJS.mode.CBC,
      padding: CryptoJS.pad.ZeroPadding,
    });
    return encrypted.toString();
  },
  decrypt(word: string) {
    const decrypt = CryptoJS.AES.decrypt(word, key, {
      iv: iv,
      mode: CryptoJS.mode.CBC,
      padding: CryptoJS.pad.ZeroPadding,
    });
    return CryptoJS.enc.Utf8.stringify(decrypt).toString();
  },
};
