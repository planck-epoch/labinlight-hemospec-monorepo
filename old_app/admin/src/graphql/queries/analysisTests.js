import gql from 'graphql-tag'

export const DEFAULT_ANALYSIS_TEST_VALUES = {
  name: '',
  code: '',
  unit: '',
  testType: 'numeric',
  referenceMale: '',
  referenceFemale: '',
  referenceValue: 0,
}

export const fragments = {
  analysisTest: gql`
    fragment analysisTestFields on AnalysisTest {
      id
      name
      code
      testType
      unit
      referenceMale
      referenceFemale
      referenceValue
      updatedAt
      createdAt
    }
  `
}

export const GET_ALL_ANALYSIS_TESTS_QUERY = gql`
  query analysisTests($page: Int, $order: String, $direction: String, $search: String) {
    analysisTests(page: $page, limit: 10, order: $order, direction: $direction, search: $search) {
      collection {
        ...analysisTestFields
      }
      metadata {
        totalPages
        totalCount
        currentPage
        limitValue
      }
    }
  }
  ${fragments.analysisTest}
`

export const GET_ANALYSIS_TEST_QUERY = gql`
  query analysisTests($id: Int!) {
    analysisTest(id: $id) {
      ...analysisTestFields
    }
  }
  ${fragments.analysisTest}
`

export const CREATE_ANALYSIS_TEST_MUTATION = gql`
  mutation createAnalysisTest($analysisTest: AnalysisTestInput!) {
    analysisTests: createAnalysisTest(analysisTest: $analysisTest) {
      ...analysisTestFields
    }
  }
  ${fragments.analysisTest}
`

export const UPDATE_ANALYSIS_TEST_MUTATION = gql`
  mutation updateAnalysisTest($id: Int!, $analysisTest: AnalysisTestInput!) {
    analysisTests: updateAnalysisTest(id: $id, analysisTest: $analysisTest) {
      ...analysisTestFields
    }
  }
  ${fragments.analysisTest}
`

export const DELETE_ANALYSIS_TEST_MUTATION = gql`
  mutation deleteAnalysisTest($ids: [Int!]!) {
    analysisTests: deleteAnalysisTest(ids: $ids) {
      ...analysisTestFields
    }
  }
  ${fragments.analysisTest}
`
