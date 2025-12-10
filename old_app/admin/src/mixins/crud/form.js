import Pluralize from 'pluralize';

export const formMixin = (modelName) => ({
  props: ['id'],
  data() {
    return {
      modelNamePlural: Pluralize(modelName)
    }
  },
  computed: {
    byId() {
      return this.$store.getters[`${this.modelNamePlural}/byId`]
    },
  },
  methods: {
    loadById(args) {
      return this.$store.dispatch(`${this.modelNamePlural}/loadById`, args)
    },
    save(args) {
      return this.$store.dispatch(`${this.modelNamePlural}/save`, args)
    },
  },
  beforeMount() {
    let vm = this
    if (this.id) {
      this.loadById(this.id).then(function(response){
        if (!response) {
          vm.$router.push({name: 'NotFound'})
        }
      })
    } else {
      this.onReset()
    }
  }
})
