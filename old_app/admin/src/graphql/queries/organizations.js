import gql from 'graphql-tag'

export const DEFAULT_ORGANIZATION_VALUES = {
  name: '',
  }

export const fragments = {
  organization: gql`
    fragment organizationFields on Organization {
      id
      name
      updatedAt
      createdAt
    }
  `
}

export const GET_ALL_ORGANIZATIONS_QUERY = gql`
  query organizations($page: Int, $order: String, $direction: String, $search: String) {
    organizations(page: $page, limit: 10, order: $order, direction: $direction, search: $search) {
      collection {
        ...organizationFields
      }
      metadata {
        totalPages
        totalCount
        currentPage
        limitValue
      }
    }
  }
  ${fragments.organization}
`

export const GET_ORGANIZATION_QUERY = gql`
  query organizations($id: Int!) {
    organization(id: $id) {
      ...organizationFields
    }
  }
  ${fragments.organization}
`

export const CREATE_ORGANIZATION_MUTATION = gql`
  mutation createOrganization($organization: OrganizationInput!) {
    organizations: createOrganization(organization: $organization) {
      ...organizationFields
    }
  }
  ${fragments.organization}
`

export const UPDATE_ORGANIZATION_MUTATION = gql`
  mutation updateOrganization($id: Int!, $organization: OrganizationInput!) {
    organizations: updateOrganization(id: $id, organization: $organization) {
      ...organizationFields
    }
  }
  ${fragments.organization}
`

export const DELETE_ORGANIZATION_MUTATION = gql`
  mutation deleteOrganization($ids: [Int!]!) {
    organizations: deleteOrganization(ids: $ids) {
      ...organizationFields
    }
  }
  ${fragments.organization}
`
