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
        label="name "
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
  import { DEFAULT_ORGANIZATION_VALUES } from '@/graphql/queries/organizations'
  import { formMixin } from '@/mixins/crud/form'
  import _ from 'lodash'

  export default {
    name: 'OrganizationForm',
    mixins: [ formMixin('organization') ],
    data: function() {
      return {
        form: JSON.parse(JSON.stringify(DEFAULT_ORGANIZATION_VALUES))
      }
    },
    computed: {
      organization() {
        return this.byId(this.id)
      }
    },
    watch: {
      organization() {
        if (this.id) {
          this.form = Object.assign({}, this.organization)
        }
      }
    },
    methods: {
      onSubmit() {
        const sanitizedForm = _.pick(this.form, _.keys(DEFAULT_ORGANIZATION_VALUES))
          let vm = this
        this.save({id: parseInt(this.id), form: sanitizedForm}).then(function(response){
          if (response) {
            const responseId = response.data.organizations.id
            vm.$router.push({name: 'OrganizationShow', params: {id: responseId}})
          }
        })
      },
      onReset() {
        this.form = JSON.parse(JSON.stringify(DEFAULT_ORGANIZATION_VALUES))
      },
      onCancel() {
        if (this.id) {
          this.$router.push({name: 'OrganizationShow', params: {id: parseInt(this.id)}})
        } else {
          this.$router.push({name: 'OrganizationList'})
        }
      }
    },
  }
</script>

<style lang="scss" scoped>
</style>
