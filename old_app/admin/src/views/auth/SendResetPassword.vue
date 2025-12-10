<template>
  <q-page
    padding
    class="q-pa-md row items-center justify-center q-gutter-md bg-primary"
  >
    <div class="col-lg-4 col-md-6 col-sm-8 col-xs-10">
      <q-card class="q-pa-xl">
        <q-card-section>
          <div class="text-h6">
            Recover Password
          </div>
        </q-card-section>
        <q-form
          class="q-gutter-md"
          @submit="onSubmit"
        >
          <q-card-section>
            <q-input
              v-model="form.email"
              type="email"
              label="Email"
            />
          </q-card-section>
          <q-card-actions align="center">
            <q-btn
              color="primary"
              align="center"
              label="Send Email"
              type="submit"
            />
          </q-card-actions>
        </q-form>
      </q-card>

      <div class="auth-links">
        <router-link to="/login" class="text-center">Login</router-link>
      </div>
    </div>
  </q-page>
</template>

<script>
import { mapActions } from 'vuex'
import { required, email } from 'vuelidate/lib/validators'

export default {
  name: 'SendResetPassword',
  data () {
    return {
      form: {
        email: '',
        resource: 'admin'
      }
    }
  },
  validations: {
    form: {
      email: { required, email }
    }
  },
  methods: {
    ...mapActions({
      sendResetPassword: 'auth/sendResetPassword'
    }),
    onSubmit () {
      this.sendResetPassword(this.form).then((response) => {
        if (response) {
          this.$router.push({path: '/new_password'})
          this.$q.notify({message: 'An email has been sent to your email with instructions on how to recover your password.', color: 'positive'})
        } else {
          this.$q.notify({message: 'Email not found'})
        }
      }).catch(() => {})
    }
  }
}
</script>

<style lang="scss" scoped>
</style>
