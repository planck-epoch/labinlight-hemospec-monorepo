export const loadingWatcher = {
  watch: {
    loading(newValue) {
      if (newValue) {
        this.$q.loading.show({ delay: 100 })
      } else {
        this.$q.loading.hide()
      }
    }
  }
}

export const globalErrorWatcher = {
  watch: {
    errors({graphQLErrors, networkError}) {
      if (graphQLErrors)
        graphQLErrors.map(({ message, locations, path }) => {
          locations // for later reference
          path // for later reference

          const multiline = message.replace(/\n/g, '<br>')
          this.$q.notify({
            message: multiline, 
            html: true
          })
        })
      if (networkError) this.$q.notify(networkError)
    }
  }
};
