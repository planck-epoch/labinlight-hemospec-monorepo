class FixAnalysesSchema < ActiveRecord::Migration[6.1]
  def up
    change_column :analyses, :organization_id, :integer, null: false
    change_column :analyses, :analysis_type_id, :integer, null: false
    change_column :analyses, :process_number, :string, null: false
    change_column :analyses, :sex_id, :integer, null: false
    change_column :analyses, :birth_year, :integer, null: false
    change_column :analyses, :payload, :jsonb, null: false

    add_column :analyses, :results, :jsonb
  end

  def down
    change_column :analyses, :organization_id, :integer
    change_column :analyses, :analysis_type_id, :integer
    change_column :analyses, :process_number, :string
    change_column :analyses, :sex_id, :integer
    change_column :analyses, :birth_year, :integer
    change_column :analyses, :payload, :jsonb

    remove_column :analyses, :results
  end
end
