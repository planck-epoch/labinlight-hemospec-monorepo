class AddDefaultToAnalysisBundles < ActiveRecord::Migration[6.0]
  def change
    add_column :analysis_bundles, :default, :boolean, default: false, null: false
  end
end
