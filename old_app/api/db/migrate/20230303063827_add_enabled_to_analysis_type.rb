class AddEnabledToAnalysisType < ActiveRecord::Migration[6.1]
  def change
    add_column :analysis_types, :enabled, :boolean, default: false
  end
end
