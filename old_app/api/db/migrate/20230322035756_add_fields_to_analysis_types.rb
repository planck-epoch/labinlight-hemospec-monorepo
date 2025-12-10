class AddFieldsToAnalysisTypes < ActiveRecord::Migration[6.1]
  def up
    change_column :analysis_types, :code, :string, unique: true, null: false

    create_table :analysis_tests do |t|
      t.string :name, null: false
      t.string :code, null: false, unique: true
      t.string :reference_male
      t.string :reference_female

      t.timestamps
    end

    create_join_table :analysis_tests, :analysis_types
  end

  def down
    change_column :analysis_types, :code, :string

    drop_table :analysis_tests
    drop_table :analysis_tests_types
  end
end
