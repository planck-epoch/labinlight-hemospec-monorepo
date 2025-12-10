import gql from 'graphql-tag'

export const DEFAULT_ADMIN_VALUES = {
  email: '',
  password: '',
  passwordConfirmation: ''
}

export const fragments = {
  admin: gql`
    fragment adminFields on Admin {
      id
      email
      resetPasswordSentAt
      createdAt
      updatedAt
    }
  `
}

export const GET_ALL_ADMINS_QUERY = gql`
  query admins($page: Int, $order: String, $direction: String, $search: String) {
    admins(page: $page, limit: 20, order: $order, direction: $direction, search: $search) {
      collection {
        ...adminFields
      }
      metadata {
        totalPages
        totalCount
        currentPage
        limitValue
      }
    }
  }
  ${fragments.admin}
`

export const GET_ADMIN_QUERY = gql`
  query admins($id: Int!) {
    admin(id: $id) {
      ...adminFields
    }
  }
  ${fragments.admin}
`

export const CREATE_ADMIN_MUTATION = gql`
  mutation createAdmin($admin: AdminInput!) {
    admins: createAdmin(admin: $admin) {
      ...adminFields
    }
  }
  ${fragments.admin}
`

export const UPDATE_ADMIN_MUTATION = gql`
  mutation updateAdmin($id: Int!, $admin: AdminInput!) {
    admins: updateAdmin(id: $id, admin: $admin) {
      ...adminFields
    }
  }
  ${fragments.admin}
`

export const DELETE_ADMIN_MUTATION = gql`
  mutation deleteAdmin($ids: [Int!]!) {
    admins: deleteAdmin(ids: $ids) {
      ...adminFields
    }
  }
  ${fragments.admin}
`
