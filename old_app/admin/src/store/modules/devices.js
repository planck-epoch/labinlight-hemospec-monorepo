import { apolloClient } from "@/graphql/apollo";
import {
  GET_ALL_DEVICES_QUERY,
  GET_DEVICE_QUERY,
  CREATE_DEVICE_MUTATION,
  UPDATE_DEVICE_MUTATION,
  DELETE_DEVICE_MUTATION,
} from "@/graphql/queries/devices";
import { normalize } from "normalizr";
import { deviceSchema, devicesSchema } from "@/schema";

import {
  initStateMixin,
  gettersMixin,
  actionsMixin,
  mutationsMixin,
} from "@/mixins/crud/store";

const initState = () => {
  return initStateMixin();
};

// initial state
const state = initState();

// getters
const getters = {
  ...gettersMixin,
};

// actions
const actions = {
  ...actionsMixin,
  // loadAll: async ({ commit }, {force = false}) => {
  loadAll: async ({ commit }, { force, page, search }) => {
    force = force === undefined ? false : force;
    page = page === undefined ? 1 : parseInt(page);
    commit("setLoading", true, { root: true });

    const fetchPolicy = force ? "network-only" : "cache-first";
    const response = await apolloClient.query({
      query: GET_ALL_DEVICES_QUERY,
      variables: {
        page: page,
        order: "createdAt",
        direction: "desc",
        search: search,
      },
      fetchPolicy: fetchPolicy,
    });

    let normalizedData = normalize(response.data, devicesSchema);
    commit("updateEntities", { normalizedData }, { root: true });
    commit("setLoading", false, { root: true });
  },
  loadById: async ({ commit }, id) => {
    let response = null;

    const parsedId = parseInt(id);
    if (typeof parsedId == "number" && parsedId > 0) {
      commit("setLoading", true, { root: true });

      response = await apolloClient.query({
        query: GET_DEVICE_QUERY,
        variables: { id: parsedId },
      });
      const normalizedData = normalize(response.data, deviceSchema);
      commit("updateEntities", { normalizedData }, { root: true });

      commit("setLoading", false, { root: true });
    }

    return response;
  },
  save: async ({ commit }, { id, form }) => {
    let response = null;

    commit("setLoading", true, { root: true });
    if (id) {
      response = await apolloClient.mutate({
        mutation: UPDATE_DEVICE_MUTATION,
        variables: { id: id, device: form },
      });
    } else {
      response = await apolloClient.mutate({
        mutation: CREATE_DEVICE_MUTATION,
        variables: { device: form },
      });
      apolloClient.clearStore();
    }

    commit("setLoading", false, { root: true });

    if (response) {
      const normalizedData = normalize(response.data, deviceSchema);
      commit("updateEntities", { normalizedData }, { root: true });
    }

    return response;
  },
  clearReferenceSample: async ({ commit }, id) => {
    // Import mutation dynamically to avoid circular deps
    const { CLEAR_REFERENCE_SAMPLE_MUTATION } = await import(
      "@/graphql/queries/clearReferenceSample"
    );
    commit("setLoading", true, { root: true });
    const response = await apolloClient.mutate({
      mutation: CLEAR_REFERENCE_SAMPLE_MUTATION,
      variables: { id },
    });
    if (response && response.data && response.data.devices) {
      // Update the device entity in the store
      const normalizedData = normalize(response.data, deviceSchema);
      commit("updateEntities", { normalizedData }, { root: true });
    }
    commit("setLoading", false, { root: true });
    return response;
  },
  delete: async ({ commit }, ids) => {
    commit("setLoading", true, { root: true });

    const response = await apolloClient.mutate({
      mutation: DELETE_DEVICE_MUTATION,
      variables: { ids: ids },
    });
    response.data.devices.map((m) => commit("removeEntity", m.id));
    apolloClient.clearStore();

    commit("setLoading", false, { root: true });

    return response;
  },
};

// mutations
const mutations = {
  ...mutationsMixin,
};

export default {
  namespaced: true,
  state,
  getters,
  actions,
  mutations,
};
