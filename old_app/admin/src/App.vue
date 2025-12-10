<template>
  <div id="app">
    <component :is="layout">
      <router-view />
    </component>
  </div>
</template>

<script>
  import { mapState } from 'vuex'
  import { loadingWatcher, globalErrorWatcher } from '@/mixins/loading-quasar'

  const default_layout = "default";

  export default {
    mixins: [ loadingWatcher, globalErrorWatcher ],
    computed: {
      ...mapState({
        loading: state => state.loading,
        errors: state => state.errors
      }),
      layout() {
        return (this.$route.meta.layout || default_layout) + "-layout";
      }
    }
  };
</script>


<style lang="scss">
</style>
