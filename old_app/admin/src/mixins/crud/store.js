import _ from 'lodash'

export const initStateMixin = () => {
  return {
    entities: {},
    entitiesOrder: [],
    metadata: {
      currentPage: 0,
      totalPages: 0,
      totalCount: 0,
      limitValue: 0
    }
  }
}

export const gettersMixin = {
  all: (state) => {
    // const filtered = _.filter(state.entities, function(item) { return state.entitiesOrder.indexOf(item.id) })
    // console.log(filtered)
    const visibleEntities = _.filter(state.entities, item => state.entitiesOrder.includes(item.id))
    return _.sortBy(visibleEntities, function(item){ return state.entitiesOrder.indexOf(item.id) })
  },
  byId: (state) => (id) => {
    return state.entities[id]
  }
};

export const actionsMixin = {
  resetState: async ({ commit }) => {
    commit('resetState')
  }
}

export const mutationsMixin = {
  resetState (state) {
    // Object.assign(state, initState())
    Object.assign(state, function(){ return initStateMixin })
  },
  removeEntity (state, id) {
    // state.entitiesOrder = _.without(state.entitiesOrder, id).filter(n => n)
    // to force apollo cache clear
    state.entitiesOrder = []
    delete state.entities[id]
  }
}
