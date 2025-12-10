<template>
  <q-page class="q-pa-md">
    <q-form
      v-if="form"
      class="q-gutter-md"
      @submit="onSubmit"
      @reset="onReset"
    >
      <q-input
        v-model="form.email"
        label="Email *"
        type="email"
      />

      <q-input
        v-model="form.password"
        label="Password *"
        type="password"
      />

      <q-input
        v-model="form.passwordConfirmation"
        label="Password Confirmation *"
        type="password"
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
  import { DEFAULT_ADMIN_VALUES } from '@/graphql/queries/admins'
  import { formMixin } from '@/mixins/crud/form'
  import _ from 'lodash'

  export default {
    name: 'AdminForm',
    mixins: [ formMixin('admin') ],
    data: function() {
      return {
        form: JSON.parse(JSON.stringify(DEFAULT_ADMIN_VALUES))
      }
    },
    computed: {
      'admin'() {
        return this.byId(this.id)
      }
    },
    watch: {
      admin() {
        if (this.id) {
          this.form = Object.assign({}, this.admin)
        }
      }
    },
    methods: {
      onSubmit() {
        const sanitizedForm = _.pick(this.form, _.keys(DEFAULT_ADMIN_VALUES))
        let vm = this
        this.save({id: parseInt(this.id), form: sanitizedForm}).then(function(response){
          if (response) {
            const responseId = response.data.admins.id
            vm.$router.push({name: 'AdminShow', params: {id: responseId}})
          }
        })
      },
      onReset() {
        this.form = JSON.parse(JSON.stringify(DEFAULT_ADMIN_VALUES))
      },
      onCancel() {
        if (this.id) {
          this.$router.push({name: 'AdminShow', params: {id: parseInt(this.id)}})
        } else {
          this.$router.push({name: 'AdminList'})
        }
      }
    },
  }
</script>

<style lang="scss" scoped>
</style>
