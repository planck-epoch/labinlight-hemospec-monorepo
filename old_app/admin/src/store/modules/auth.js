import Vue from 'vue'
import { apolloClient } from '@/graphql/apollo'
import _ from 'lodash'
import { LOGIN_MUTATION, SIGNUP_MUTATION, LOGOUT_MUTATION, SEND_RESET_PASSWORD_MUTATION, RESET_PASSWORD_MUTATION } from '@/graphql/queries/auth'

const initState = () => {
  return {
    user: {}
  }
}

// initial state
const state = initState()

// getters
const getters = {
  user: (state) => {
    return state.user
  },
  logged: (state) => {
    return !_.isEmpty(state.user)
  }
}

// actions
const actions = {
  resetState: async ({ commit }) => {
    commit('resetState')
  },
  setUser: async ({ commit }, user) => {
    commit('setUser', user)
  },
  setLoginData: async({ commit }, data) => {
    Vue.$cookies.set('adminAuthToken', data.token)
    apolloClient.clearStore()
    const user = {
      email: data.email
    }
    commit('setUser', user)
  },
  clearData: async({ commit, dispatch }) => {
    Vue.$cookies.set('adminAuthToken', '')
    dispatch('resetAllState', {}, { root: true })
    commit('setUser', {})
    apolloClient.clearStore()
  },
  login: async ({ commit, dispatch }, form) => {
    commit('setLoading', true, { root: true })
    let response = null

    response = await apolloClient.mutate({ mutation: LOGIN_MUTATION, variables: { ...form } })
    dispatch('setLoginData', response.data.login)

    commit('setLoading', false, { root: true })

    return response.data.login
  },
  signup: async ({ commit, dispatch }, form) => {
    commit('setLoading', true, { root: true })
    let response = null

    response = await apolloClient.mutate({ mutation: SIGNUP_MUTATION, variables: { ...form } })
    dispatch('setLoginData', response.data.signUp)
    
    commit('setLoading', false, { root: true })

    return response.data.signUp
  },
  logout: async ({ commit, dispatch }) => {
    commit('setLoading', true, { root: true })

    apolloClient.mutate({ mutation: LOGOUT_MUTATION })
    dispatch('clearData')

    commit('setLoading', false, { root: true })
  },
  sendResetPassword: async ({ commit }, form) => {
    commit('setLoading', true, { root: true })

    const response = await apolloClient.mutate({ mutation: SEND_RESET_PASSWORD_MUTATION, variables: { ...form } })

    commit('setLoading', false, { root: true })

    return response.data.sendResetPasswordInstructions
  },
  resetPassword: async ({ commit, dispatch }, form) => {
    commit('setLoading', true, { root: true })

    const response = await apolloClient.mutate({ mutation: RESET_PASSWORD_MUTATION, variables: { ...form } })
    dispatch('clearData')

    commit('setLoading', false, { root: true })

    return response.data.resetPassword
  }
}

// mutations
const mutations = {
  resetState (state) {
    Object.assign(state, initState())
  },
  setUser (state, value) {
    Vue.set(state, 'user', value)
  },
}

export default {
  namespaced: true,
  state,
  getters,
  actions,
  mutations
}
