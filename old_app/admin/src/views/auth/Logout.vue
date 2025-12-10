<template>
  <div />
</template>

<script>
import { mapActions } from 'vuex'

export default {
  mounted () {
    this.logout().then(() => this.$router.push({path: '/'})).catch(() => {})
  },
  methods: {
    ...mapActions({
      logout: 'auth/logout'
    })
  }
}
</script>
