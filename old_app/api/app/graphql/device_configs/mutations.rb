module DeviceConfigs
  module Mutations
    def self.included(child_class)
      child_class.field :create_device_config, DeviceConfigs::DeviceConfigType, null: false, description: "Create a DeviceConfig" do
        argument :device_config, DeviceConfigs::DeviceConfigInputType, required: true
      end

      child_class.field :update_device_config, DeviceConfigs::DeviceConfigType, null: false, description: "Update a DeviceConfig" do
        argument :id, Integer, required: true
        argument :device_config, DeviceConfigs::DeviceConfigInputType, required: true
      end

      child_class.field :delete_device_config, [DeviceConfigs::DeviceConfigInputType], null: false, description: "Destroy a DeviceConfig" do
        argument :ids, [Integer], required: true
      end
    end

    def create_device_config(device_config:)
      authorize_user_or_admin!

      device_config = DeviceConfig.new(device_config.to_h)

      if device_config.save
        device_config
      else
        raise GraphQL::ExecutionError, device_config.errors.full_messages.join(", ")
      end
    end

    def update_device_config(id:, device_config:)
      authorize_user_or_admin!

      DeviceConfig.find(id).tap do |elem|
        elem.update!(device_config.to_h)
      end
    end

    def delete_device_config(ids:)
      authorize_user_or_admin!
      
      DeviceConfig.where(id: ids).destroy_all
    end
  end
end
