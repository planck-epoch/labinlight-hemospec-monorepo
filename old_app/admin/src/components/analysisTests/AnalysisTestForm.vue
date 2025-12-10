<template>
  <q-page class="q-pa-md">
    <q-form
      v-if="form"
      class="q-gutter-md"
      @submit="onSubmit"
      @reset="onReset"
    >
      <q-input
        v-model="form.name"
        label="name *"
      />

      <q-input
        v-model="form.code"
        label="code *"
      />

      <q-select
        v-model="form.testType"
        label="test type *"
        :options="[{label: 'numeric', value: 'numeric'}, {label: 'boolean', value: 'boolean'}]"
        emit-value
        map-options
      />

      <q-input
        v-if="form.testType === 'numeric'"
        v-model="form.unit"
        label="unit"
      />

      <q-input
        v-if="form.testType === 'numeric'"
        v-model="form.referenceMale"
        label="reference male"
      />

      <q-input
        v-if="form.testType === 'numeric'"
        v-model="form.referenceFemale"
        label="reference female"
      />

      <q-input
        v-if="form.testType === 'boolean'"
        v-model="form.referenceValue"
        type="number"
        label="reference value *"
        required
        @input="val => form.referenceValue = val === '' ? null : parseFloat(val)"
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
  import { DEFAULT_ANALYSIS_TEST_VALUES } from '@/graphql/queries/analysisTests'
  import { formMixin } from '@/mixins/crud/form'
  import _ from 'lodash'

  export default {
    name: 'AnalysisTestForm',
    mixins: [ formMixin('analysisTest') ],
    data: function() {
      return {
        form: JSON.parse(JSON.stringify(DEFAULT_ANALYSIS_TEST_VALUES))
      }
    },
    computed: {
      analysisTest() {
        return this.byId(this.id)
      }
    },
    watch: {
      analysisTest() {
        if (this.id) {
          this.form = Object.assign({}, this.analysisTest)
        }
      }
    },
    methods: {
      onSubmit() {
        const sanitizedForm = _.pick(this.form, _.keys(DEFAULT_ANALYSIS_TEST_VALUES))
            let vm = this
        this.save({id: parseInt(this.id), form: sanitizedForm}).then(function(response){
          if (response) {
            const responseId = response.data.analysisTests.id
            vm.$router.push({name: 'AnalysisTestShow', params: {id: responseId}})
          }
        })
      },
      onReset() {
        this.form = JSON.parse(JSON.stringify(DEFAULT_ANALYSIS_TEST_VALUES))
      },
      onCancel() {
        if (this.id) {
          this.$router.push({name: 'AnalysisTestShow', params: {id: parseInt(this.id)}})
        } else {
          this.$router.push({name: 'AnalysisTestList'})
        }
      }
    },
  }
</script>

<style lang="scss" scoped>
</style>
