class ChangeAnalysisTypesToBundles < ActiveRecord::Migration[6.1]
  def up
    rename_table :analysis_types, :analysis_bundles
    rename_column :analyses, :analysis_type_id, :analysis_bundle_id
    rename_column :analysis_tests_types, :analysis_type_id, :analysis_bundle_id
    rename_column :prices, :analysis_type_id, :analysis_bundle_id

    rename_table :analysis_tests_types, :analysis_bundles_tests
  end

  def down
    rename_table :analysis_bundles, :analysis_types
    rename_table :analysis_bundles_tests, :analysis_tests_types
    
    rename_column :analyses, :analysis_bundle_id, :analysis_type_id
    rename_column :analysis_tests_types, :analysis_bundle_id, :analysis_type_id
    rename_column :prices, :analysis_bundle_id, :analysis_type_id
  end
end
