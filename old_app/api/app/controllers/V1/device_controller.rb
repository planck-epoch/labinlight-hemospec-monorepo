module V1
  class DeviceController < ApplicationController
    def get_device_config 
      device = Device.find_by_serial_number(params[:serial_number])
      if device
        dev_config = device.device_config
        if dev_config
          render json: {
            Head: {
              config_name: dev_config.config_name,
              num_repeats: dev_config.num_repeats,
            },
            Section: JSON.parse(dev_config.sections),
            LastUpdate: dev_config.updated_at.to_i,
            id: dev_config.id,
            LastCalibrationAt: device.last_calibration_at.nil? ? 0 : device.last_calibration_at.to_i
          }
        else
          render json: {error: "Device configuration not found"}, status: :not_found
        end
      else
        render json: {error: "Device not found"}, status: :not_found
      end
    end 

    def calibrate
      Rails.logger.info("Calibration requested for device with serial number: #{params[:serial_number]}")
      device = Device.find_by_serial_number(params[:serial_number])
      if device
        payload = params
        Rails.logger.info("Calibration payload received: #{payload}")
        if device.reference_sample.nil?
          device.update(reference_sample: payload, last_calibration_at: Time.current)
          render status: :ok
        else
          uri = URI("#{ENV['PREDICT_URL']}/calibrate")
          headers = { 'Content-Type': 'application/json' }
          res = Net::HTTP.post(uri, {
            Reference: device.reference_sample,
            Sample: payload
          }.to_json, headers)

          if res.is_a?(Net::HTTPSuccess)
            device.update(last_calibration_at: Time.current)
            render status: :ok
          else
            render status: :internal_server_error
          end
        end
      else
        render json: {error: "Device not found"}, status: :not_found
      end
    end
  end
end