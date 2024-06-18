<script>
import axios from 'axios';

export default {
  data() {
    return {
      rootUrl: '',
      urlParams: null,
      loginParams: ['id_token', 'sso_state'],
      codeParams: ['code', 'state']
    };
  },

  mounted() {
    this.rootUrl = window.location.origin;
    this.urlParams = new URLSearchParams(window.location.search);
    this.processUrl();
  },

  methods: {
    processUrl() {
      const hasLoginParams = this.loginParams.every(param => this.urlParams.has(param) && this.urlParams.get(param) !== '');
      const hasCodeParam = this.codeParams.every(param => this.urlParams.has(param) && this.urlParams.get(param) !== '');

      if ((hasLoginParams || hasCodeParam) && !(hasLoginParams && hasCodeParam)) {
        if (hasLoginParams) {
          this.getCode(this.urlParams.get(this.loginParams[0]), this.urlParams.get(this.loginParams[1]))
          console.log('Parameters id_token and sso_state exist.');
        } else if (hasCodeParam) {
          this.getToken(this.urlParams.get(this.codeParams[0]), this.urlParams.get(this.codeParams[1]))
          console.log('Parameters code and state exists.');
        }
      } else {
        console.error('URL parameters are incorrect.');
      }
    },

    async getCode(id_token, sso_state) {
      const params = new URLSearchParams();
      params.append('id_token', id_token);
      params.append('sso_state', sso_state);

      const fullUrl = this.rootUrl + '/plugins/oauth-login/oauth/authorize?' + params.toString();

      try {
        const response = await axios.get(fullUrl);

        if (response.data.code === 200) {
          window.location.href = response.data.data.Location;
        } else {
          console.error('Error occurred in the response:', response.data.msg);
        }
      } catch (error) {
        console.error('An error occurred while making the GET request:', error);
      }
    },

    async getToken(code, state) {
      const params = new URLSearchParams();
      params.append('code', code);
      params.append('state', state);

      const fullUrl = this.rootUrl + '/plugins/oauth-login/oauth/token?' + params.toString();

      try {
        const response = await axios.get(fullUrl);

        if (response.data.code === 200) {
          const token = response.data.data.token;
          this.setToken(token);
          window.location.href = this.rootUrl;
        } else {
          console.error('Error occurred in the response:', response.data.msg);
        }
      } catch (error) {
        console.error('An error occurred while making the GET request:', error);
      }
    },

    setToken(token){
      localStorage.setItem('opengauss-token', token)
    }
  }
};
</script>

<template>
  <h2>LOADING...</h2>
</template>

<style>

</style>



