import gql from "graphql-tag";

export const DEFAULT_DEVICE_VALUES = {
  serialNumber: "",
  organizationId: "",
  deviceConfigId: "",
};

export const fragments = {
  device: gql`
    fragment deviceFields on Device {
      id
      serialNumber
      organizationId
      organization {
        name
      }
      deviceConfigId
      deviceConfig {
        configName
      }
      referenceSample
      updatedAt
      createdAt
      lastCalibrationAt
    }
  `,
};

export const GET_ALL_DEVICES_QUERY = gql`
  query devices(
    $page: Int
    $order: String
    $direction: String
    $search: String
  ) {
    devices(
      page: $page
      limit: 10
      order: $order
      direction: $direction
      search: $search
    ) {
      collection {
        ...deviceFields
      }
      metadata {
        totalPages
        totalCount
        currentPage
        limitValue
      }
    }
  }
  ${fragments.device}
`;

export const GET_DEVICE_QUERY = gql`
  query devices($id: Int!) {
    device(id: $id) {
      ...deviceFields
    }
  }
  ${fragments.device}
`;

export const CREATE_DEVICE_MUTATION = gql`
  mutation createDevice($device: DeviceInput!) {
    devices: createDevice(device: $device) {
      ...deviceFields
    }
  }
  ${fragments.device}
`;

export const UPDATE_DEVICE_MUTATION = gql`
  mutation updateDevice($id: Int!, $device: DeviceInput!) {
    devices: updateDevice(id: $id, device: $device) {
      ...deviceFields
    }
  }
  ${fragments.device}
`;

export const DELETE_DEVICE_MUTATION = gql`
  mutation deleteDevice($ids: [Int!]!) {
    devices: deleteDevice(ids: $ids) {
      ...deviceFields
    }
  }
  ${fragments.device}
`;
