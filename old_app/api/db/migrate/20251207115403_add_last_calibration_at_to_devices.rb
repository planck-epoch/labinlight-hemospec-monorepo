class AddLastCalibrationAtToDevices < ActiveRecord::Migration[6.0]
  def change
    add_column :devices, :last_calibration_at, :datetime, null: true
  end
end
