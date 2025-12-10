class CreateAnalyses < ActiveRecord::Migration[6.0]
  def change
    create_table :analyses do |t|

      t.integer :organization_id, null: true
      t.integer :analysis_type_id, null: true
      t.string :process_number, null: true
      t.integer :sex_id, null: true
      t.integer :birth_year, null: true
      t.integer :country_id, null: true
      t.string :health_number, null: true
      t.string :phone, null: true
      t.jsonb :payload, null: true
  
      t.timestamps
    end
  end
end
