<script>
import axios from 'axios';
import request from '@/request.js'
export default {
  data() {
    return {
      responseData: null
    };
  },
  methods: {

    async getData() {
      try {
        console.log('enter')
        const response = await axios.get('/plugins/oauth-login/oauth/test?param=123')
        this.responseData = response.data;
        console.log(response.data)
      } catch (error) {
        console.log('error')
        console.error(error);
      }
    }
  }
};

export function getCodeImg() {
  return request({
    url: '/plugins/oauth-login/oauth/test?param=123',
    method: 'get'
  })
}
</script>

<template>
  <p>test123</p>
  <a>oauth-login.vue</a>
  <button @click="getData">获取数据</button>
  <div v-if="responseData">
    <p>{{ responseData }}</p>

  </div>

</template>

<style>

</style>



