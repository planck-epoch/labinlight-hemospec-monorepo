import gql from 'graphql-tag'

export const DEFAULT_USER_VALUES = {
  email: '',
  password: '',
  passwordConfirmation: ''
}

export const fragments = {
  user: gql`
    fragment userFields on User {
      id
      email
      resetPasswordSentAt
      createdAt
      updatedAt
    }
  `
}

export const GET_ALL_USERS_QUERY = gql`
  query users($page: Int, $order: String, $direction: String, $search: String) {
    users(page: $page, limit: 20, order: $order, direction: $direction, search: $search) {
      collection {
        ...userFields
      }
      metadata {
        totalPages
        totalCount
        currentPage
        limitValue
      }
    }
  }
  ${fragments.user}
`

export const GET_USER_QUERY = gql`
  query users($id: Int!) {
    user(id: $id) {
      ...userFields
    }
  }
  ${fragments.user}
`

export const CREATE_USER_MUTATION = gql`
  mutation createUser($user: UserInput!) {
    users: createUser(user: $user) {
      ...userFields
    }
  }
  ${fragments.user}
`

export const UPDATE_USER_MUTATION = gql`
  mutation updateUser($id: Int!, $user: UserInput!) {
    users: updateUser(id: $id, user: $user) {
      ...userFields
    }
  }
  ${fragments.user}
`

export const DELETE_USER_MUTATION = gql`
  mutation deleteUser($ids: [Int!]!) {
    users: deleteUser(ids: $ids) {
      ...userFields
    }
  }
  ${fragments.user}
`
