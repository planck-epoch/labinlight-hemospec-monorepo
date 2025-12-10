module Devices
  module Mutations
    def self.included(child_class)
      child_class.field :create_device, Devices::DeviceType, null: false, description: "Create a Device" do
        argument :device, Devices::DeviceInputType, required: true
      end

      child_class.field :update_device, Devices::DeviceType, null: false, description: "Update a Device" do
        argument :id, Integer, required: true
        argument :device, Devices::DeviceInputType, required: true
      end

      child_class.field :delete_device, [Devices::DeviceType], null: false, description: "Destroy a Device" do
        argument :ids, [Integer], required: true
      end
      child_class.field :clear_reference_sample, Devices::DeviceType, null: false, description: "Clear reference sample for a Device" do
        argument :id, Integer, required: true
      end
    end

    def create_device(device:)
      authorize_user_or_admin!

      device = Device.new(device.to_h)

      if device.save
        device
      else
        raise GraphQL::ExecutionError, device.errors.full_messages.join(", ")
      end
    end

    def update_device(id:, device:)
      authorize_user_or_admin!

      Device.find(id).tap do |elem|
        elem.update!(device.to_h)
      end
    end

    def delete_device(ids:)
      authorize_user_or_admin!
      Device.where(id: ids).destroy_all
    end

    def clear_reference_sample(id:)
      authorize_user_or_admin!
      device = Device.find(id)
      device.update!(reference_sample: nil, last_calibration_at: nil)
      device
    end
  end
end
