import Vue from 'vue'
import Vuex from 'vuex'
import createPersistedState from 'vuex-persistedstate'
import _ from 'lodash'

import auth from './modules/auth'
import users from './modules/users'
import admins from './modules/admins'
/*** rgv imports ***/
import analyses from './modules/analyses'
import prices from './modules/prices'
import analysisTests from './modules/analysisTests'
import analysisBundles from './modules/analysisBundles'
import devices from './modules/devices'
import deviceConfigs from './modules/deviceConfigs'
import organizations from './modules/organizations'

Vue.use(Vuex)

const debug = process.env.NODE_ENV !== 'production'

export const storeModules = {
	analyses,
	prices,
  analysisTests,
	analysisBundles,
	devices,
	deviceConfigs,
	organizations,
  auth,
  users,
  admins
};

const initState = () => {
  return {
    loading: false
  }
}

const state = initState()

const actions = {
  resetAllState: async ({ commit }) => {
    commit('resetAllState')
  }
}

const mutations = {
  resetAllState (state) {
    Object.assign(state, initState())
    _.forOwn(storeModules, (value, key) => {
      this.dispatch(key+'/resetState', {}, {root:true})
    });
  },
  setLoading (state, value) {
    Vue.set(state, 'loading', value)
  },
  updateEntities (state, { normalizedData }) {
    // Loop over all kinds of entities we received
    const entities = normalizedData.entities
    for (let type in entities) {
      for (let entity in entities[type]) {
        const oldObj = state[type][entity] || {}
        // Merge the new data in the old object
        const newObj = Object.assign(oldObj, entities[type][entity])
        // Make sure new entities are also reactive
        Vue.set(state[type].entities, entity, newObj)
      }
    }

    // Loop over all kinds of entities order arrays we received
    const results = normalizedData.result
    for (let type in results) {
      // for non paginated results
      if (Array.isArray(results[type])) {
        Vue.set(state[type], 'entitiesOrder', results[type])
      }
      // for paginated results
      if (Array.isArray(results[type].collection)) {
        Vue.set(state[type], 'entitiesOrder', results[type].collection)

        const metadata = results[type].metadata
        // for (let key in metadata) {
        //   Vue.set(state[type].metadata, key, metadata[key])
        // }
        Vue.set(state[type].metadata, 'currentPage', metadata['currentPage'])
        Vue.set(state[type].metadata, 'totalPages', metadata['totalPages'])
        Vue.set(state[type].metadata, 'totalCount', metadata['totalCount'])
        Vue.set(state[type].metadata, 'limitValue', metadata['limitValue'])
      }
    }
  }
}

export const store = new Vuex.Store({
  state,
  modules: {
    ...storeModules
  },
  actions,
  mutations,
  plugins: [createPersistedState({
    key: 'lilcoreAdminAuth',
    paths: ['auth.user']
  })],
  strict: debug
})
