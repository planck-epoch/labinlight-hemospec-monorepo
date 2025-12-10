module Devices
  class DeviceInputType < Types::BaseInputObject
    description "Attributes for creating or updating a device"

    argument :serial_number, String, required: true
    argument :organization_id, Integer, required: true
    argument :device_config_id, Integer, required: true
  
  end
end
