export const bodyClassMixin = (layoutName) => ({
  beforeMount() {
    let bodyClass = this.body || ''
    document.body.className = (bodyClass + ' ' + layoutName + '-layout').trim()
  },
  beforeDestroyed() {
    document.body.className = (this.bodyClass.replace(layoutName + '-layout', '')).trim()
  }
})
