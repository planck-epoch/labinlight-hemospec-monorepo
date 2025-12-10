import gql from "graphql-tag";

export const DEFAULT_ANALYSIS_BUNDLE_VALUES = {
  name: "",
  code: "",
  analysisTestIds: [],
  enabled: false,
  default: false,
};

export const fragments = {
  analysisBundle: gql`
    fragment analysisBundleFields on AnalysisBundle {
      id
      name
      code
      analysisTestIds
      analysisTests {
        id
        name
      }
      enabled
      default
      updatedAt
      createdAt
    }
  `,
};

export const GET_ALL_ANALYSIS_BUNDLES_QUERY = gql`
  query analysisBundles(
    $page: Int
    $order: String
    $direction: String
    $search: String
  ) {
    analysisBundles(
      page: $page
      limit: 10
      order: $order
      direction: $direction
      search: $search
    ) {
      collection {
        ...analysisBundleFields
      }
      metadata {
        totalPages
        totalCount
        currentPage
        limitValue
      }
    }
  }
  ${fragments.analysisBundle}
`;

export const GET_ANALYSIS_BUNDLE_QUERY = gql`
  query analysisBundles($id: Int!) {
    analysisBundle(id: $id) {
      ...analysisBundleFields
    }
  }
  ${fragments.analysisBundle}
`;

export const CREATE_ANALYSIS_BUNDLE_MUTATION = gql`
  mutation createAnalysisBundle($analysisBundle: AnalysisBundleInput!) {
    analysisBundles: createAnalysisBundle(analysisBundle: $analysisBundle) {
      ...analysisBundleFields
    }
  }
  ${fragments.analysisBundle}
`;

export const UPDATE_ANALYSIS_BUNDLE_MUTATION = gql`
  mutation updateAnalysisBundle(
    $id: Int!
    $analysisBundle: AnalysisBundleInput!
  ) {
    analysisBundles: updateAnalysisBundle(
      id: $id
      analysisBundle: $analysisBundle
    ) {
      ...analysisBundleFields
    }
  }
  ${fragments.analysisBundle}
`;

export const DELETE_ANALYSIS_BUNDLE_MUTATION = gql`
  mutation deleteAnalysisBundle($ids: [Int!]!) {
    analysisBundles: deleteAnalysisBundle(ids: $ids) {
      ...analysisBundleFields
    }
  }
  ${fragments.analysisBundle}
`;
