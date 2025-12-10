class RemoveUnitFromAnalysisType < ActiveRecord::Migration[6.1]
  def change
    remove_column :analysis_types, :unit, :string
  end
end
