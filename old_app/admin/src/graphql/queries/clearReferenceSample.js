import gql from "graphql-tag";

export const CLEAR_REFERENCE_SAMPLE_MUTATION = gql`
  mutation clearReferenceSample($id: Int!) {
    devices: clearReferenceSample(id: $id) {
      id
      referenceSample
    }
  }
`;
