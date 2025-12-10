import Vue from 'vue'
import VueRouter from 'vue-router'
import { store } from '@/store'

import Login from '@/views/auth/Login.vue'
import Logout from '@/views/auth/Logout.vue'
import SendResetPassword from '@/views/auth/SendResetPassword'
import ResetPassword from '@/views/auth/ResetPassword'
import NotFound from '@/views/NotFound.vue'
import Home from '@/views/Home.vue'
import HelpReference from '@/views/help/HelpReference.vue'
// users components
import UserList from '@/components/users/UsersList.vue'
import User from '@/components/users/User.vue'
import UserForm from '@/components/users/UserForm.vue'
// admins components
import AdminList from '@/components/admins/AdminsList.vue'
import Admin from '@/components/admins/Admin.vue'
import AdminForm from '@/components/admins/AdminForm.vue'
/*** rgv imports ***/
// analyses components
import AnalysisList from '@/components/analyses/AnalysesList.vue'
import Analysis from '@/components/analyses/Analysis.vue'
import AnalysisForm from '@/components/analyses/AnalysisForm.vue'

// prices components
import PriceList from '@/components/prices/PricesList.vue'
import Price from '@/components/prices/Price.vue'
import PriceForm from '@/components/prices/PriceForm.vue'

// analysisTests components
import AnalysisTestList from '@/components/analysisTests/AnalysisTestsList.vue'
import AnalysisTest from '@/components/analysisTests/AnalysisTest.vue'
import AnalysisTestForm from '@/components/analysisTests/AnalysisTestForm.vue'

// analysisBundles components
import AnalysisBundleList from '@/components/analysisBundles/AnalysisBundlesList.vue'
import AnalysisBundle from '@/components/analysisBundles/AnalysisBundle.vue'
import AnalysisBundleForm from '@/components/analysisBundles/AnalysisBundleForm.vue'

// devices components
import DeviceList from '@/components/devices/DevicesList.vue'
import Device from '@/components/devices/Device.vue'
import DeviceForm from '@/components/devices/DeviceForm.vue'

// deviceConfigs components
import DeviceConfigList from '@/components/deviceConfigs/DeviceConfigsList.vue'
import DeviceConfig from '@/components/deviceConfigs/DeviceConfig.vue'
import DeviceConfigForm from '@/components/deviceConfigs/DeviceConfigForm.vue'

// organizations components
import OrganizationList from '@/components/organizations/OrganizationsList.vue'
import Organization from '@/components/organizations/Organization.vue'
import OrganizationForm from '@/components/organizations/OrganizationForm.vue'


Vue.use(VueRouter)

const routes = [
  { name: 'Home', path: '/', component: Home },
  { name: 'Reference', path: '/reference', component: HelpReference },
  { name: 'Login', path: '/login', component: Login, meta: { layout: "auth", requiresAuth: false }, props: true },
  { name: 'Logout', path: '/logout', component: Logout, meta: { layout: "auth", requiresAuth: false } },
  { name: 'SendResetPassword', path: '/recover_password', component: SendResetPassword, meta: { layout: "auth", requiresAuth: false } },
  { name: 'ResetPassword', path: '/new_password', component: ResetPassword, meta: { layout: "auth", requiresAuth: false }, props: (route) => ({ resetPasswordToken: route.query.token }) },
  { name: 'NotFound', path: '/404', component: NotFound, meta: { layout: "auth" } },

  // users paths
  { path: '/users', redirect: { name: 'UserList', params: {page: 1} }},
  { name: 'UserList', path: '/users/page/:page', component: UserList, props: true },
  { name: 'UserNew', path: '/users/new', component: UserForm },
  { name: 'UserShow', path: '/users/:id', component: User, props: true },
  { name: 'UserEdit', path: '/users/:id/edit', component: UserForm, props: true },
  // admins paths
  { path: '/admins', redirect: { name: 'AdminList', params: {page: 1} }},
  { name: 'AdminList', path: '/admins/page/:page', component: AdminList, props: true },
  { name: 'AdminNew', path: '/admins/new', component: AdminForm },
  { name: 'AdminShow', path: '/admins/:id', component: Admin, props: true },
  { name: 'AdminEdit', path: '/admins/:id/edit', component: AdminForm, props: true },
  /*** rgv routes ***/
// analyses paths
  { path: '/analyses', redirect: { name: 'AnalysisList', params: {page: 1} }},
  { name: 'AnalysisList', path: '/analyses/page/:page', component: AnalysisList, props: true },
  { name: 'AnalysisNew', path: '/analyses/new', component: AnalysisForm },
  { name: 'AnalysisShow', path: '/analyses/:id', component: Analysis, props: true },
  { name: 'AnalysisEdit', path: '/analyses/:id/edit', component: AnalysisForm, props: true },

// prices paths
  { path: '/prices', redirect: { name: 'PriceList', params: {page: 1} }},
  { name: 'PriceList', path: '/prices/page/:page', component: PriceList, props: true },
  { name: 'PriceNew', path: '/prices/new', component: PriceForm },
  { name: 'PriceShow', path: '/prices/:id', component: Price, props: true },
  { name: 'PriceEdit', path: '/prices/:id/edit', component: PriceForm, props: true },

// analysisTests paths
{ path: '/analysis_tests', redirect: { name: 'AnalysisTestList', params: {page: 1} }},
{ name: 'AnalysisTestList', path: '/analysis_tests/page/:page', component: AnalysisTestList, props: true },
{ name: 'AnalysisTestNew', path: '/analysis_tests/new', component: AnalysisTestForm },
{ name: 'AnalysisTestShow', path: '/analysis_tests/:id', component: AnalysisTest, props: true },
{ name: 'AnalysisTestEdit', path: '/analysis_tests/:id/edit', component: AnalysisTestForm, props: true },

// analysisBundles paths
  { path: '/analysis_bundles', redirect: { name: 'AnalysisBundleList', params: {page: 1} }},
  { name: 'AnalysisBundleList', path: '/analysis_bundles/page/:page', component: AnalysisBundleList, props: true },
  { name: 'AnalysisBundleNew', path: '/analysis_bundles/new', component: AnalysisBundleForm },
  { name: 'AnalysisBundleShow', path: '/analysis_bundles/:id', component: AnalysisBundle, props: true },
  { name: 'AnalysisBundleEdit', path: '/analysis_bundles/:id/edit', component: AnalysisBundleForm, props: true },

// devices paths
  { path: '/devices', redirect: { name: 'DeviceList', params: {page: 1} }},
  { name: 'DeviceList', path: '/devices/page/:page', component: DeviceList, props: true },
  { name: 'DeviceNew', path: '/devices/new', component: DeviceForm },
  { name: 'DeviceShow', path: '/devices/:id', component: Device, props: true },
  { name: 'DeviceEdit', path: '/devices/:id/edit', component: DeviceForm, props: true },

// deviceConfigs paths
  { path: '/deviceConfigs', redirect: { name: 'DeviceConfigList', params: {page: 1} }},
  { name: 'DeviceConfigList', path: '/deviceConfigs/page/:page', component: DeviceConfigList, props: true },
  { name: 'DeviceConfigNew', path: '/deviceConfigs/new', component: DeviceConfigForm },
  { name: 'DeviceConfigShow', path: '/deviceConfigs/:id', component: DeviceConfig, props: true },
  { name: 'DeviceConfigEdit', path: '/deviceConfigs/:id/edit', component: DeviceConfigForm, props: true },

// organizations paths
  { path: '/organizations', redirect: { name: 'OrganizationList', params: {page: 1} }},
  { name: 'OrganizationList', path: '/organizations/page/:page', component: OrganizationList, props: true },
  { name: 'OrganizationNew', path: '/organizations/new', component: OrganizationForm },
  { name: 'OrganizationShow', path: '/organizations/:id', component: Organization, props: true },
  { name: 'OrganizationEdit', path: '/organizations/:id/edit', component: OrganizationForm, props: true },


  { path: '*', component: NotFound, meta: { layout: "auth" } },
]

const router = new VueRouter({
  mode: 'history',
  base: process.env.BASE_URL,
  routes
})

router.beforeEach((to, from, next) => {
  if (to.matched.some(record => record.meta.requiresAuth == false)) next()
  else {
    if (store.getters['auth/logged']) {
      next()
    } else {
      next({ name: 'Login', params: {prevPath: window.location.pathname} })
    }
  }
})

export default router
