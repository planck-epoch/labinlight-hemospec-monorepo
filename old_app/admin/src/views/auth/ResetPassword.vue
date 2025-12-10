<template>
  <q-page
    padding
    class="q-pa-md row items-center justify-center q-gutter-md bg-primary"
  >
    <div class="col-lg-4 col-md-6 col-sm-8 col-xs-10">
      <q-card class="q-pa-xl">
        <q-card-section>
          <div class="text-h6">
            New Password
          </div>
        </q-card-section>
        <q-form
          class="q-gutter-md"
          @submit="onSubmit"
        >
          <q-card-section>
            <q-input
              v-show="!resetPasswordToken"
              v-model="form.resetPasswordToken"
              label="Token"
            />
            <q-input
              v-model="form.password"
              type="password"
              label="Password"
            />
            <q-input
              v-model="form.passwordConfirmation"
              type="password"
              label="Password Confirmation"
            />
          </q-card-section>
          <q-card-actions align="center">
            <q-btn
              color="primary"
              align="center"
              label="Change Password"
              type="submit"
            />
          </q-card-actions>
        </q-form>
      </q-card>
    </div>
  </q-page>
</template>

<script>
import { mapActions } from 'vuex'
import { required } from 'vuelidate/lib/validators'

export default {
  name: 'ResetPassword',
  props: {
    resetPasswordToken: {
      type: String,
      default: ''
    }
  },
  data () {
    return {
      form: {
        resetPasswordToken: this.resetPasswordToken,
        password: '',
        passwordConfirmation: '',
        resource: 'admin'
      }
    }
  },
  watch: {
    resetPasswordToken: function(newValue) {
      this.form.resetPasswordToken = newValue
    }
  },
  validations: {
    form: {
      resetPasswordToken: { required },
      password: { required },
      passwordConfirmation: { required }
    }
  },
  methods: {
    ...mapActions({
      resetPassword: 'auth/resetPassword'
    }),
    onSubmit () {
      this.resetPassword(this.form).then((response) => {
        if (response) {
          this.$router.push({name: 'Login'})
          this.$q.notify({message: 'Password changed successfully.', color: 'positive'})
        } else {
          this.$q.notify({message: 'Error'})
        }
      }).catch(() => {})
    }
  }
}
</script>

<style lang="scss" scoped>
</style>
