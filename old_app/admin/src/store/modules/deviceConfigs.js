import { apolloClient } from '@/graphql/apollo'
import { GET_ALL_DEVICE_CONFIG_CONFIGS_QUERY, GET_DEVICE_CONFIG_QUERY, CREATE_DEVICE_CONFIG_MUTATION, UPDATE_DEVICE_CONFIG_MUTATION, DELETE_DEVICE_CONFIG_MUTATION } from '@/graphql/queries/deviceConfigs'
import { normalize } from 'normalizr'
import { deviceConfigSchema, deviceConfigsSchema } from '@/schema'

import { initStateMixin, gettersMixin, actionsMixin, mutationsMixin } from '@/mixins/crud/store'

const initState = () => {
  return initStateMixin()
}

// initial state
const state = initState()

// getters
const getters = {
  ...gettersMixin
}

// actions
const actions = {
  ...actionsMixin,
  // loadAll: async ({ commit }, {force = false}) => {
  loadAll: async ({ commit }, { force, page, search }) => {
    force = (force === undefined) ? false : force
    page = (page === undefined) ? 1 : parseInt(page)
    commit('setLoading', true, { root: true })

    const fetchPolicy = force ? 'network-only' : 'cache-first'
    const response = await apolloClient.query({ query: GET_ALL_DEVICE_CONFIG_CONFIGS_QUERY, variables: { page: page, order: 'createdAt', direction: 'desc', search: search }, fetchPolicy: fetchPolicy })

    let normalizedData = normalize(response.data, deviceConfigsSchema)
    commit('updateEntities', { normalizedData }, { root: true })
    commit('setLoading', false, { root: true })
  },
  loadById: async ({ commit }, id) => {
    let response = null

    const parsedId = parseInt(id)
    if (typeof(parsedId) == 'number' && parsedId > 0) {
      commit('setLoading', true, { root: true })

      response = await apolloClient.query({ query: GET_DEVICE_CONFIG_QUERY, variables: { id: parsedId } })
      const normalizedData = normalize(response.data, deviceConfigSchema)
      commit('updateEntities', { normalizedData }, { root: true })

      commit('setLoading', false, { root: true })
    }

    return response
  },
  save: async ({ commit }, { id, form }) => {
    let response = null

    commit('setLoading', true, { root: true })
    if (id) {
      response = await apolloClient.mutate({ mutation: UPDATE_DEVICE_CONFIG_MUTATION, variables: {id: id, deviceConfig: form} })
    } else {
      response = await apolloClient.mutate({ mutation: CREATE_DEVICE_CONFIG_MUTATION, variables: {deviceConfig: form} })
      apolloClient.clearStore()
    }

    commit('setLoading', false, { root: true })

    if (response) {
      const normalizedData = normalize(response.data, deviceConfigsSchema)
      commit('updateEntities', { normalizedData }, { root: true })
    }

    return response
  },

  delete: async ({ commit }, ids) => {
    commit('setLoading', true, { root: true })

    const response = await apolloClient.mutate({ mutation: DELETE_DEVICE_CONFIG_MUTATION, variables: { ids: ids } })
    response.data.deviceConfigs.map(m => commit('removeEntity', m.id))
    apolloClient.clearStore()

    commit('setLoading', false, { root: true })

    return response
  }
}

// mutations
const mutations = {
  ...mutationsMixin
}

export default {
  namespaced: true,
  state,
  getters,
  actions,
  mutations
}
