<template>
  <q-page class="q-pa-md">
    <q-form
      v-if="form"
      class="q-gutter-md"
      @submit="onSubmit"
      @reset="onReset"
    >
      <q-select
        v-model="form.organizationId"
        use-input
        input-debounce="0"
        :options="organizationsAvailable"
        option-value="id"
        option-label="name"
        emit-value
        map-options
        label="Organization *"
      />
      
      <q-select
        v-model="form.analysisBundleId"
        use-input
        input-debounce="0"
        :options="analysisBundlesAvailable"
        option-value="id"
        option-label="name"
        emit-value
        map-options
        label="Analysis Bundle *"
      />
      
      <q-input
        v-model="form.value"
        label="value *"
        type="number"
      />
  
      <div class="row q-pa-md">
        <div class="col">
          <q-btn
            label="Cancel"
            color="primary"
            flat
            size="sm"
            class="q-ml-sm"
            @click="onCancel"
          />
        </div>

        <div class="col-6" />

        <div class="col">
          <q-btn
            label="Submit"
            type="submit"
            color="primary"
            size="sm"
            class="float-right"
          />
        </div>
      </div>
    </q-form>
  </q-page>
</template>

<script>
  import { DEFAULT_PRICE_VALUES } from '@/graphql/queries/prices'
  import { formMixin } from '@/mixins/crud/form'
  import { mapGetters, mapActions } from 'vuex'
  import _ from 'lodash'

  export default {
    name: 'PriceForm',
    mixins: [ formMixin('price') ],
    data: function() {
      return {
        organizationsAvailable: [],
        analysisBundlesAvailable: [],
        form: JSON.parse(JSON.stringify(DEFAULT_PRICE_VALUES))
      }
    },
    computed: {
      ...mapGetters({
        organizations: 'organizations/all',
        analysisBundles: 'analysisBundles/all'
      }),
      price() {
        return this.byId(this.id)
      }
    },
    watch: {
      price() {
        if (this.id) {
          this.form = Object.assign({}, this.price)
        }
      },
      organizations() {
        this.organizationsAvailable = this.organizations
      },
      analysisBundles() {
        this.analysisBundlesAvailable = this.analysisBundles
      }
    },
    mounted() {
      this.loadAllOrganizations({force: false, limit: 0})
      this.loadAllAnalysisBundles({force: false, limit: 0})
    },
    methods: {
      ...mapActions({
        loadAllOrganizations: 'organizations/loadAll',
        loadAllAnalysisBundles: 'analysisBundles/loadAll',
      }),
      onSubmit() {
        const sanitizedForm = _.pick(this.form, _.keys(DEFAULT_PRICE_VALUES))
        sanitizedForm.organizationId = Number(sanitizedForm.organizationId)
        sanitizedForm.analysisBundleId = Number(sanitizedForm.analysisBundleId)
        sanitizedForm.value = Number(sanitizedForm.value)
        let vm = this
        this.save({id: parseInt(this.id), form: sanitizedForm}).then(function(response){
          if (response) {
            const responseId = response.data.prices.id
            vm.$router.push({name: 'PriceShow', params: {id: responseId}})
          }
        })
      },
      onReset() {
        this.form = JSON.parse(JSON.stringify(DEFAULT_PRICE_VALUES))
      },
      onCancel() {
        if (this.id) {
          this.$router.push({name: 'PriceShow', params: {id: parseInt(this.id)}})
        } else {
          this.$router.push({name: 'PriceList'})
        }
      }
    },
  }
</script>

<style lang="scss" scoped>
</style>
