import gql from 'graphql-tag'

export const DEFAULT_PRICE_VALUES = {
    organizationId: '',
    analysisBundleId: '',
    value: '',
  }

export const fragments = {
  price: gql`
    fragment priceFields on Price {
      id
      organizationId
      organization {
        name
      }
      analysisBundleId
      analysisBundle {
        name
      }
      value
      updatedAt
      createdAt
    }
  `
}

export const GET_ALL_PRICES_QUERY = gql`
  query prices($page: Int, $order: String, $direction: String, $search: String) {
    prices(page: $page, limit: 10, order: $order, direction: $direction, search: $search) {
      collection {
        ...priceFields
      }
      metadata {
        totalPages
        totalCount
        currentPage
        limitValue
      }
    }
  }
  ${fragments.price}
`

export const GET_PRICE_QUERY = gql`
  query prices($id: Int!) {
    price(id: $id) {
      ...priceFields
    }
  }
  ${fragments.price}
`

export const CREATE_PRICE_MUTATION = gql`
  mutation createPrice($price: PriceInput!) {
    prices: createPrice(price: $price) {
      ...priceFields
    }
  }
  ${fragments.price}
`

export const UPDATE_PRICE_MUTATION = gql`
  mutation updatePrice($id: Int!, $price: PriceInput!) {
    prices: updatePrice(id: $id, price: $price) {
      ...priceFields
    }
  }
  ${fragments.price}
`

export const DELETE_PRICE_MUTATION = gql`
  mutation deletePrice($ids: [Int!]!) {
    prices: deletePrice(ids: $ids) {
      ...priceFields
    }
  }
  ${fragments.price}
`
