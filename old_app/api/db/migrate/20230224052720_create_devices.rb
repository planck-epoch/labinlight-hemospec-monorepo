class CreateDevices < ActiveRecord::Migration[6.0]
  def change
    create_table :devices do |t|

      t.string :serial_number, null: true
      t.integer :organization_id, null: true
  
      t.timestamps
    end
  end
end
