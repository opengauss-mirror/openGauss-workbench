import CryptoJS from 'crypto-js';

const key = CryptoJS.enc.Utf8.parse('57Kk5riv5r6z5aSn5rm+5Yy65Zu95Yib'); // Key, AES-128 requires 16 characters, AES-256 requires 32 characters
const iv = CryptoJS.enc.Utf8.parse('1234567890000000'); // Key offset, 16 characters

export default {
  encrypt(word: string) {
    const srcs = CryptoJS.enc.Utf8.parse(word);
    const encrypted = CryptoJS.AES.encrypt(srcs, key, {
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
