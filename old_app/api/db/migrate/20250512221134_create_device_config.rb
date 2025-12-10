class CreateDeviceConfig < ActiveRecord::Migration[6.0]
  def change
    create_table :device_configs do |t|

      t.string :config_name, null: false
      t.integer :num_repeats, null: false
      t.jsonb :sections, null: false
  
      t.timestamps
    end

    add_column :devices, :device_config_id, :integer
  end
end
