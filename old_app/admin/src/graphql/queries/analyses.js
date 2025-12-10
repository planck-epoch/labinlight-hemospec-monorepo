import gql from 'graphql-tag'

export const DEFAULT_ANALYSIS_VALUES = {
  organizationId: '',
  analysisBundleId: '',
  processNumber: '',
  sexCode: '',
  birthYear: '',
  countryCode: '',
  healthNumber: '',
  phone: '',
  payload: '',
  results: '',
}

export const fragments = {
  analysis: gql`
    fragment analysisFields on Analysis {
      id
      organizationId
      organization {
        name
      }
      analysisBundleId
      analysisBundle {
        name
      }
      processNumber
      sexCode
      birthYear
      countryCode
      healthNumber
      phone
      payload
      results
      updatedAt
      createdAt
    }
  `
}

export const GET_ALL_ANALYSES_QUERY = gql`
  query analyses($page: Int, $order: String, $direction: String, $search: String) {
    analyses(page: $page, limit: 10, order: $order, direction: $direction, search: $search) {
      collection {
        ...analysisFields
      }
      metadata {
        totalPages
        totalCount
        currentPage
        limitValue
      }
    }
  }
  ${fragments.analysis}
`

export const GET_ANALYSIS_QUERY = gql`
  query analyses($id: Int!) {
    analysis(id: $id) {
      ...analysisFields
    }
  }
  ${fragments.analysis}
`

export const EXPORT_ALL_ANALYSES_QUERY = gql`
  query analysesExport($ids: [Int!]) {
    analysesExport(ids: $ids) {
      filename
      encodedFile
    }
  }
`

export const CREATE_ANALYSIS_MUTATION = gql`
  mutation createAnalysis($analysis: AnalysisInput!) {
    analyses: createAnalysis(analysis: $analysis) {
      ...analysisFields
    }
  }
  ${fragments.analysis}
`

export const UPDATE_ANALYSIS_MUTATION = gql`
  mutation updateAnalysis($id: Int!, $analysis: AnalysisInput!) {
    analyses: updateAnalysis(id: $id, analysis: $analysis) {
      ...analysisFields
    }
  }
  ${fragments.analysis}
`

export const DELETE_ANALYSIS_MUTATION = gql`
  mutation deleteAnalysis($ids: [Int!]!) {
    analyses: deleteAnalysis(ids: $ids) {
      ...analysisFields
    }
  }
  ${fragments.analysis}
`
