class AddFieldsToAnalysisTests < ActiveRecord::Migration[6.1]
  def change
    add_column :analysis_tests, :test_type, :integer, default: 0
    add_column :analysis_tests, :reference_value, :float
  end
end
