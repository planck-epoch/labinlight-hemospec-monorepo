class AddUnitToAnalysisType < ActiveRecord::Migration[6.1]
  def change
    add_column :analysis_types, :unit, :string
  end
end
