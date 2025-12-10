import gql from 'graphql-tag'

export const DEFAULT_DEVICE_CONFIG_VALUES = {
    configName: '',
    numRepeats: 1,
    sections: "[]"
  }

export const fragments = {
  deviceConfig: gql`
    fragment deviceConfigFields on DeviceConfig {
      id
      configName
      numRepeats
      sections
      updatedAt
      createdAt
    }
  `
}

export const GET_ALL_DEVICE_CONFIG_CONFIGS_QUERY = gql`
  query deviceConfigs($page: Int, $order: String, $direction: String, $search: String) {
    deviceConfigs(page: $page, limit: 10, order: $order, direction: $direction, search: $search) {
      collection {
        ...deviceConfigFields
      }
      metadata {
        totalPages
        totalCount
        currentPage
        limitValue
      }
    }
  }
  ${fragments.deviceConfig}
`

export const GET_DEVICE_CONFIG_QUERY = gql`
  query deviceConfigs($id: Int!) {
    deviceConfig(id: $id) {
      ...deviceConfigFields
    }
  }
  ${fragments.deviceConfig}
`

export const CREATE_DEVICE_CONFIG_MUTATION = gql`
  mutation createDeviceConfig($deviceConfig: DeviceConfigInput!) {
    deviceConfigs: createDeviceConfig(deviceConfig: $deviceConfig) {
      ...deviceConfigFields
    }
  }
  ${fragments.deviceConfig}
`

export const UPDATE_DEVICE_CONFIG_MUTATION = gql`
  mutation updateDeviceConfig($id: Int!, $deviceConfig: DeviceConfigInput!) {
    deviceConfigs: updateDeviceConfig(id: $id, deviceConfig: $deviceConfig) {
      ...deviceConfigFields
    }
  }
  ${fragments.deviceConfig}
`

export const DELETE_DEVICE_CONFIG_MUTATION = gql`
  mutation deleteDeviceConfig($ids: [Int!]!) {
    deviceConfigs: deleteDeviceConfig(ids: $ids) {
      ...deviceConfigFields
    }
  }
  ${fragments.deviceConfig}
`
