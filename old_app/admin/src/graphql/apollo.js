import Vue from 'vue'
import Router from '@/router'
import {Notify} from 'quasar'
// This is everything we need to work with Apollo 2.0.
import { ApolloClient } from 'apollo-client'
import { ApolloLink } from 'apollo-link'
import { HttpLink } from 'apollo-link-http'
import { setContext } from 'apollo-link-context'
import { onError } from 'apollo-link-error'
import { InMemoryCache } from 'apollo-cache-inmemory'
import VueApollo from 'vue-apollo'
import { store } from '@/store/'

// Register the VueApollo plugin with Vue.
Vue.use(VueApollo)

// Create a new HttpLink to connect to your GraphQL API.
// According to the Apollo docs, this should be an absolute URI.
const httpLink = new HttpLink({
  uri: `${process.env.VUE_APP_API_URL}/graphql`
})

const authLink = setContext((_, { headers }) => {
  // get the authentication token from local storage if it exists
  const token = Vue.$cookies.get('adminAuthToken')
  // return the headers to the context so httpLink can read them
  return {
    headers: {
      ...headers,
      authorization: token ? `Bearer ${token}` : "",
    }
  }
});

// Error Handling
const errorLink = onError(({ graphQLErrors, networkError }) => {
  if (graphQLErrors) {
    graphQLErrors.map(({ message, locations, path, extensions }) => {
      locations // for later reference
      path // for later reference

      const prevPath = window.location.pathname

      // console.log(
      //   `[GraphQL error]: Message: ${message}, Location: ${locations}, Path: ${path}`
      // )

      if (extensions.code == 'AUTH_ERROR') {
        Router.push({name: 'Login', params: { prevPath: prevPath }})
      } else if (['undefinedField', 'NOT_FOUND'].includes(extensions.code)) {
        Router.push({name: 'NotFound'})
      }

      if (!['undefinedType', 'undefinedField', 'NOT_FOUND'].includes(extensions.code)) {
        // console.log(graphQLErrors)
        const multiline = message.replace(/\n/g, '<br>')
        Notify.create({
          message: multiline,
          html: true
        })
      }

      store.commit('setLoading', false)
    })
  }
  if (networkError) {
    // console.log(`[Network error]: ${networkError}`)
    Notify.create("Can't connect to server")
    store.commit('setLoading', false)
  }
})


// I'm creating another variable here just because it makes it easier to add more links in the future.
const link = authLink.concat(httpLink)

// Create the apollo client
export const apolloClient = new ApolloClient({
  // Tells Apollo to use the link chain with the http link we set up.
  link: ApolloLink.from([errorLink, link]),
  // Handles caching of results and mutations.
  cache: new InMemoryCache(),
  // Useful if you have the Apollo DevTools installed in your browser.
  connectToDevTools: true,
  fetchOptions: {
    mode: 'no-cors',
  }
})

// Not being used yet
export const apolloProvider = new VueApollo({
  // Apollo 2.0 allows multiple clients to be enabled at once.
  // Here we select the default (and only) client.
  defaultClient: apolloClient,
})
