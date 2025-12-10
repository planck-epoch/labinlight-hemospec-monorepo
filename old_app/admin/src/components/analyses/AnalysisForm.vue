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
        label="Analysis Type *"
      />
      <q-input
        v-model="form.processNumber"
        label="process_number "
      />
      <q-input
        v-model="form.sexCode"
        label="sex_code"
      />
      <q-input
        v-model="form.birthYear"
        label="birth_year "
        type="number"
      />
      <q-input
        v-model="form.countryCode"
        label="country_code"
        maxlength="2"
        mask="AA"
        :rules="[val => !val || /^[A-Za-z]{0,2}$/.test(val) || 'Only letters allowed']"
        @update:model-value="val => form.countryCode = val?.toUpperCase()"
      />
      <q-input
        v-model="form.healthNumber"
        label="health_number "
      />
      <q-input
        v-model="form.phone"
        label="phone "
      />

      <q-item>
        <q-item-section>
          <q-item-label overline>Payload</q-item-label>
          <v-jsoneditor v-model="form.payload" height="400px" />
        </q-item-section>
      </q-item>

      <q-item>
        <q-item-section>
          <q-item-label overline>Results</q-item-label>
          <v-jsoneditor v-model="form.results" height="250px" />
        </q-item-section>
      </q-item>
  
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
  import { DEFAULT_ANALYSIS_VALUES } from '@/graphql/queries/analyses'
  import { formMixin } from '@/mixins/crud/form'
  import { mapGetters, mapActions } from 'vuex'
  import VJsoneditor from 'v-jsoneditor/src/index'
  import _ from 'lodash'

  export default {
    name: 'AnalysisForm',
    components: {
      VJsoneditor
    },
    mixins: [ formMixin('analysis') ],
    data: function() {
      return {
        organizationsAvailable: [],
        analysisBundlesAvailable: [],
        form: JSON.parse(JSON.stringify(DEFAULT_ANALYSIS_VALUES))
      }
    },
    computed: {
      ...mapGetters({
        organizations: 'organizations/all',
        analysisBundles: 'analysisBundles/all'
      }),
      analysis() {
        return this.byId(this.id)
      }
    },
    watch: {
      analysis() {
        if (this.id) {
          this.form = Object.assign({}, this.analysis)
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
        const sanitizedForm = _.pick(this.form, _.keys(DEFAULT_ANALYSIS_VALUES))
        sanitizedForm.organizationId = Number(sanitizedForm.organizationId)
        sanitizedForm.analysisBundleId = Number(sanitizedForm.analysisBundleId)
        sanitizedForm.birthYear = Number(sanitizedForm.birthYear)
        let vm = this
        this.save({id: parseInt(this.id), form: sanitizedForm}).then(function(response){
          if (response) {
            const responseId = response.data.analyses.id
            vm.$router.push({name: 'AnalysisShow', params: {id: responseId}})
          }
        })
      },
      onReset() {
        this.form = JSON.parse(JSON.stringify(DEFAULT_ANALYSIS_VALUES))
      },
      onCancel() {
        if (this.id) {
          this.$router.push({name: 'AnalysisShow', params: {id: parseInt(this.id)}})
        } else {
          this.$router.push({name: 'AnalysisList'})
        }
      }
    },
  }
</script>

<style lang="scss" scoped>
</style>
