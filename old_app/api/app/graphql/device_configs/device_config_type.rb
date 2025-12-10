module DeviceConfigs
  class DeviceConfigType < Types::BaseObject
    field :id, Integer, null: false

    field :config_name, String, null: false
    field :num_repeats, Integer, null: false
    field :sections, GraphQL::Types::JSON, null: false
  
    field :updated_at, GraphQL::Types::ISO8601DateTime, null: false
    field :created_at, GraphQL::Types::ISO8601DateTime, null: false
  end
end
