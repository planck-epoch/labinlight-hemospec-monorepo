class CreatePrices < ActiveRecord::Migration[6.0]
  def change
    create_table :prices do |t|

      t.integer :organization_id, null: true
      t.integer :analysis_type, null: true
      t.integer :value, null: true
  
      t.timestamps
    end
  end
end
