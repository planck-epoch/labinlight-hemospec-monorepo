module DeviceConfigs
  class DeviceConfigInputType < Types::BaseInputObject
    description "Attributes for creating or updating a device config"

    argument :config_name, String, required: true
    argument :num_repeats, Integer, required: true
    argument :sections, GraphQL::Types::JSON, required: true
  
  end
end
