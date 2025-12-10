import _ from 'lodash'
import Pluralize from 'pluralize';

export const paginationMixin = (modelName, modelKey) => ({
  data() {
    return {
      showDelete: false,
      selected: new Array(300).fill(false), // page size
      modelNamePlural: Pluralize(modelName),
      searchValue: '',
      filtering: {},
      filterOpen: false
    }
  },
  computed: {
    entitiesOrder() {
      return this.$store.state[this.modelNamePlural].entitiesOrder
    },
    metadata() {
      return this.$store.state[this.modelNamePlural].metadata
    },
    deletedScopes() {
      if (this.selectedIds.length > 0) {
        let toDelete = ""
        this.selectedIds.forEach((id, index) => {
          if (this.selectedIds.length > 1) {
            switch(index) {
              case 0:
                break;
              case this.selectedIds.length:
                toDelete = toDelete + " and "
                break;
              default:
                toDelete = toDelete + ", "
                break;
            }
          }
          const obj = _.find(this[this.modelNamePlural], ['id', id])
          if (obj) {
            toDelete = toDelete + obj[modelKey]
          }
        })

        return toDelete
      }
    },
    multiActionable() {
      return this.selected.includes(true)
    },
    selectedIds() {
      return this.selected.reduce((a, item, index) => {
        if (item == true && this[this.modelNamePlural][index]) {
          a.push(this[this.modelNamePlural][index].id)
        }
        return a;
      }, [])
    },
    allChecked: {
      get() {
        let state = false

        if (this.selectedIds.length > 0)
          state = undefined

        if (this.selectedIds.length == this[this.modelNamePlural].length)
          state = true

        return state
      },
      set(value) {
          if (value) {
            this[this.modelNamePlural].map((m, index) => {
                this.selected[index] = true
              })
          } else {
            this[this.modelNamePlural].map((m, index) => {
                this.selected[index] = false
              })
          }
          this.selected.push()

          this.value = value
      }
    },
    currentPage: {
      get() {
        return this.$route.params.page ? parseInt(this.$route.params.page) : this.metadata.currentPage
      },
      set(value) {
        this.resetSelected()
        if (this.$route.params.page != value) {
          this.$router.push({name: `${this.modelNameCapitalized}List`, params: {page: value}, query: this.filtering})
        }
      }
    }
  },
  methods: {
    loadAll(args) {
      return this.$store.dispatch(`${this.modelNamePlural}/loadAll`, args)
    },
    delete(args) {
      return this.$store.dispatch(`${this.modelNamePlural}/delete`, args)
    },
    load(args) {
      args.page = args.page ? args.page : this.currentPage
      Object.assign(args, this.filtering)

      if (_.size(this[this.modelNamePlural]) != this.entitiesOrder.length) {
        Object.assign(args, {force: true})
      }

      this.loadAll(args)
    },
    resetSelected() {
      if(typeof this.resetSelected === "function") {
        this.selected = this.selected.map(() => false)
      }
    },
    onSearch() {
      Object.assign(this.filtering, {search: this.searchValue})
      this.$router.push({name: `${this.modelNameCapitalized}List`, params: {page: 1}, query: this.filtering})
    }
  },
  beforeRouteUpdate(to, from, next) {
    this.load(Object.assign({}, {page: to.params.page}, to.query))
    next();
  }
})

export const showMixin = (modelName) => ({
  data() {
    return {
      modelNameCapitalized: _.upperFirst(modelName),
      showDelete: false
    }
  },
  methods: {
    delete(args) {
      return this.$store.dispatch(`${this.modelNamePlural}/delete`, args)
    },
    showRecord: function(id) {
      this.$router.push({name: `${this.modelNameCapitalized}Show`, params: {id: id}})
    },
    editRecord: function(id) {
      this.$router.push({name: `${this.modelNameCapitalized}Edit`, params: {id: id}})
    },
    newRecord: function() {
      this.$router.push({name: `${this.modelNameCapitalized}New`})
    },
    confirmDelete: function(id) {
      if (id && typeof this.resetSelected === "function") {
        this.resetSelected()
        this.selected[this.entitiesOrder.indexOf(id)] = true
      }
      this.showDelete = true
    },
    cancelSelected: function() {
      if (this.selectedIds && this.selectedIds.length == 1)
        this.resetSelected()
      this.showDelete = false
    },
    deleteSelected: function(id) {
      if (id == undefined || arguments.length === 0) {
        this.delete(this.selectedIds).then(function(response){
          if (response) {
            this.resetSelected()
            this.showDelete = false
            this.load({
              page: this.currentPage
            })
          }
        }.bind(this))
      } else if (id) {
        this.delete([id]).then(function(response){
          if (response) {
            this.$router.push({name: `${this.modelNameCapitalized}List`, params: {page: 1}})
          }
        }.bind(this))
      }
    }
  }
})
