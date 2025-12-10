<template>
  <q-page
    padding
    class="q-pa-md row items-center justify-center q-gutter-md bg-primary"
  >
    <div class="col-lg-4 col-md-6 col-sm-8 col-xs-10">
      <q-card class="q-pa-xl">
        <q-card-section>
          <div class="text-h6">
            Login
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
            <q-input
              v-model="form.password"
              type="password"
              label="Password"
            />
          </q-card-section>
          <q-card-actions align="center">
            <q-btn
              color="primary"
              align="center"
              label="Log In"
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
import { required, email } from 'vuelidate/lib/validators'

export default {
  name: 'Login',
  props: {
    prevPath: {
      type: String,
      default: ''
    }
  },
  data () {
    return {
      form: {
        email: '',
        password: '',
        resourceName: 'admin'
      }
    }
  },
  validations: {
    form: {
      email: { required, email },
      password: { required }
    }
  },
  methods: {
    ...mapActions({
      login: 'auth/login'
    }),
    onSubmit () {
      this.login(this.form).then(() => {
        if (this.prevPath != '') {
          this.$router.push({path: this.prevPath})
        } else {
          this.$router.push({path: '/'})
        }
      }).catch(() => {})
    }
  }
}
</script>

<style lang="scss" scoped>
</style>
