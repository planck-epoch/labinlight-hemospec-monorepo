module Devices
  class DeviceType < Types::BaseObject
    field :id, Integer, null: false

    field :serial_number, String, null: false
    field :organization_id, Integer, null: false
    field :organization, Organizations::OrganizationType, null: true
    field :device_config_id, Integer, null: true
    field :device_config, DeviceConfigs::DeviceConfigType, null: true

    field :reference_sample, GraphQL::Types::JSON, null: true
    field :last_calibration_at, GraphQL::Types::ISO8601DateTime, null: true
  
    field :updated_at, GraphQL::Types::ISO8601DateTime, null: false
    field :created_at, GraphQL::Types::ISO8601DateTime, null: false
  end
end
