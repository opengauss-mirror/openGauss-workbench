<template>
  <div class="hello">
    <h1>{{ msg }}</h1>
    <p>plugin page demo</p>
    <h3>form plugin platform param</h3>
    <code>{{ queryData }}</code>
  </div>
</template>

<script>
export default {
  name: 'HelloWorld',
  props: {
    msg: String
  },
  data () {
    return {
      queryData: ''
    }
  },
  created () {
    // Obtain the parameters of the main program through window.$wujie.props
    this.queryData = JSON.stringify(window.$wujie.props)
  }
}
</script>

<!-- Add "scoped" attribute to limit CSS to this component only -->
<style scoped>
h3 {
  margin: 40px 0 0;
}
ul {
  list-style-type: none;
  padding: 0;
}
li {
  display: inline-block;
  margin: 0 10px;
}
a {
  color: #42b983;
}
</style>
