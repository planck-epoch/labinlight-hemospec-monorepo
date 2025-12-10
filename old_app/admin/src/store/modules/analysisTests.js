import { apolloClient } from '@/graphql/apollo'
import { GET_ALL_ANALYSIS_TESTS_QUERY, GET_ANALYSIS_TEST_QUERY, CREATE_ANALYSIS_TEST_MUTATION, UPDATE_ANALYSIS_TEST_MUTATION, DELETE_ANALYSIS_TEST_MUTATION } from '@/graphql/queries/analysisTests'
import { normalize } from 'normalizr'
import { analysisTestSchema, analysisTestsSchema } from '@/schema'

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
    const response = await apolloClient.query({ query: GET_ALL_ANALYSIS_TESTS_QUERY, variables: { page: page, order: 'createdAt', direction: 'desc', search: search }, fetchPolicy: fetchPolicy })

    let normalizedData = normalize(response.data, analysisTestsSchema)
    commit('updateEntities', { normalizedData }, { root: true })
    commit('setLoading', false, { root: true })
  },
  loadById: async ({ commit }, id) => {
    let response = null

    const parsedId = parseInt(id)
    if (typeof(parsedId) == 'number' && parsedId > 0) {
      commit('setLoading', true, { root: true })

      response = await apolloClient.query({ query: GET_ANALYSIS_TEST_QUERY, variables: { id: parsedId } })
      const normalizedData = normalize(response.data, analysisTestSchema)
      commit('updateEntities', { normalizedData }, { root: true })

      commit('setLoading', false, { root: true })
    }

    return response
  },
  save: async ({ commit }, { id, form }) => {
    let response = null

    commit('setLoading', true, { root: true })
    if (id) {
      response = await apolloClient.mutate({ mutation: UPDATE_ANALYSIS_TEST_MUTATION, variables: {id: id, analysisTest: form} })
    } else {
      response = await apolloClient.mutate({ mutation: CREATE_ANALYSIS_TEST_MUTATION, variables: {analysisTest: form} })
      apolloClient.clearStore()
    }

    commit('setLoading', false, { root: true })

    if (response) {
      const normalizedData = normalize(response.data, analysisTestSchema)
      commit('updateEntities', { normalizedData }, { root: true })
    }

    return response
  },

  delete: async ({ commit }, ids) => {
    commit('setLoading', true, { root: true })

    const response = await apolloClient.mutate({ mutation: DELETE_ANALYSIS_TEST_MUTATION, variables: { ids: ids } })
    response.data.analysisTests.map(m => commit('removeEntity', m.id))
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
