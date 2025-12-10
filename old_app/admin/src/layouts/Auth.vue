<template>
  <q-layout view="lHh Lpr lFf">
    <q-page-container>
      <router-view />
    </q-page-container>
  </q-layout>
</template>

<script>
  import { bodyClassMixin } from '@/mixins/bodyClass'

  export default {
    mixins: [ bodyClassMixin('auth') ],
    data: function() {
      return {
        leftDrawerOpen: this.$q.platform.is.desktop,
      }
    }
  }
</script>

<style lang="scss">
.auth-links {
  margin-top: 20px;
  text-align: center;
  font-size: 16px;

  a {
    font-weight: bold;
    color: #fff;
  }
}
</style>
