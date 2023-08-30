<template>
    <div class="login">
      <div class="container">
        <div class="main">
          <h3>二维识别码</h3>
          <!-- 放置二维码盒子 -->
          <canvas ref="canvas"></canvas>
          <div class="qrcode_text">
            识别码:  <a :href="url" target="_blank">  {{url}}  </a>
          </div>
          <div>
            <template v-if="!is_register">
                <a-input v-model="qrcode_in" class="qrcode_in"></a-input>
                <a-button type="primary" @click="checkLicense">注册</a-button>
            </template>
            <template v-else>
                已注册!  <a> {{this.qrcode_out}} </a>
            </template>
          </div>
        </div>
      </div>
    </div>
</template>

<script>
import QRCode from 'qrcode'
export default {
  name: 'qrLogin',
  data () {
    return {
      // 二维码地址
      url: 'http://opengauss.org',
      // 二维码的key
      key: '',
      qrcode_in: '',
      qrcode_out: '',
      is_register: false
    }
  },
  mounted() {
    this.makeQrCode()
  },
  methods: {
    makeQrCode () {
      let opts = {
        errorCorrectionLevel: 'H',
        type: 'image/png',
        quality: 0.3,
        width: 165,
        height: 165,
        text: '二维码测试',
        color: {
          dark: '#333333',
          light: '#fff'
        }
      }
      QRCode.toCanvas(this.$refs.canvas, this.url, opts)
    },
    checkLicense() {
        console.log('qrcode_in is ' + this.qrcode_in)
        buildLicense({srcKeyCode: this.url, inputKey: this.qrcode_in}).then(
            res => {
                this.is_register = true;
                this.qrcode_out = res.data.outputKey
            }
        ).catch(
            res => {

            }
        )
        this.makeQrCode()
    }
  }
}
</script>

<style>
* {
  margin: 0 auto;
  padding: 0;
}
.login {
  width: 100%;
  height: 100vh;
}
.container {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: left;
  justify-content: left;
}
.main {
  position: relative;
  width: 280px;
  height: 250px;
  margin: 0 auto;
  border-radius: 5px;
  background-color: #fff;
  box-shadow: 2px 2px 2px #bbb;
}

.qrcode_in {
    width: 70%;
    background-color: white;
    border: solid;
    border-color: red;
    border-width: 1px;
}

h3 {
  padding: 10px;
  text-align: center;
}
.text_qrcode {
  width: 100%;
  height: 100vh;
}
canvas {
  display: flex;
  align-items: center;
  justify-content: center;
}
.invalid {
  position: absolute;
  top: 50%;
  left: 50%;
  margin-top: -68px;
  margin-left: -70px;
  width: 143px;
  height: 143px;
  background: rgba(255, 255, 255, 0.9);
  text-align: center;
}
p {
  font-size: 14px;
  margin-top: 40px;
  margin-bottom: 5px;
}
button {
  color: #fff;
  padding: 3px 10px;
  background: #71c771;
  border: 1px solid #5baf5b;
  border-radius: 5px;
  cursor: pointer;
}
</style>