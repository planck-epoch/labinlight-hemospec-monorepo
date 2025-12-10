<template>
  <q-page class="q-pa-md">
    <div class="row justify-center">
      <h1>404 - NOT FOUND</h1>
    </div>
    <div class="row justify-center">
      <p>
        <q-btn 
          label="Return" 
          color="primary" 
          @click="goBack"
        />
      </p>
    </div>
  </q-page>
</template>

<script>
export default {

  name: 'NotFound',

  data () {
    return {

    }
  },
  methods: {
    goBack: function() {
      if (window.history.length > 2) {
        this.$router.go(-2)
      } else {
        this.$router.push({path: '/'})
      }
    }
  }
}
</script>

<style lang="scss" scoped>
</style>
