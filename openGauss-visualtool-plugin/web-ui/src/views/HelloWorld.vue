<template>
  <div class="hello">
    <div class="title">
      <img :src="og_img1" class="default_img"/>
      <h2>openGauss DataKit</h2>
    </div>
    <h1>{{ queryData }}</h1>
  </div>
</template>

<script>
export default {
  name: 'HelloWorld',
  data () {
    return {
      queryData: 'Hello, openGauss DataKit plugin.',
      og_img: require('@/assets/icons/svg/opengaussdb.svg'),
      og_img1: require('@/assets/logo.png')
    }
  }
}
</script>

<!-- Add "scoped" attribute to limit CSS to this component only -->
<style scoped>
h1 {
  margin: 20px 0 0;
  color: red;
  display: inline-block;
  align-items: center;
}

.title {
  display: grid;
  grid-template-columns: repeat(2, auto) 1fr;
  align-items: center;
}

.default_img {
  background-color: #2d2d3d;
}

a {
  color: #42b983;
}
</style>
