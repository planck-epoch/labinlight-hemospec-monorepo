class FixAnalysisTypesFieldOnPrices < ActiveRecord::Migration[6.1]
  def change
    rename_column :prices, :analysis_type, :analysis_type_id
  end
end
