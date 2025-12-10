class AddUnitToAnalysisTest < ActiveRecord::Migration[6.1]
  def change
    add_column :analysis_tests, :unit, :string
  end
end
