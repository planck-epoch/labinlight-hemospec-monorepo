class AddReferenceSampleToDevices < ActiveRecord::Migration[6.0]
  def change
    add_column :devices, :reference_sample, :jsonb
  end
end
