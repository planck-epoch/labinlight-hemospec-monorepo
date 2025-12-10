import Vue from "vue";
import App from "@/App.vue";
import "@/quasar";

import { store } from "@/store";
import { apolloProvider } from "@/graphql/apollo";

import VueCookies from "vue-cookies";
Vue.use(VueCookies);

/******** ROUTES ********/
import router from "./router";

/******** LAYOUTS ********/
import Default from "@/layouts/Default.vue";
import Auth from "@/layouts/Auth.vue";

Vue.component("default-layout", Default);
Vue.component("auth-layout", Auth);

/******** VALIDATIONS ********/
import Vuelidate from "vuelidate";
Vue.use(Vuelidate);

/******** DATES ********/
import moment from "vue-moment";
Vue.use(moment);
import formatDate from "@/mixins/formatDate";
Vue.mixin(formatDate);

import deviceConfig from "@/mixins/deviceConfig";
Vue.mixin(deviceConfig);

Vue.config.productionTip = false;

new Vue({
  store,
  router,
  apolloProvider,
  render: (h) => h(App),
}).$mount("#app");
