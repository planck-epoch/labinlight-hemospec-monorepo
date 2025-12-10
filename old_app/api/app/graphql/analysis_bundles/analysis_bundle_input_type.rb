module AnalysisBundles
  class AnalysisBundleInputType < Types::BaseInputObject
    description "Attributes for creating or updating a analysis_bundle"

    argument :name, String, required: true
    argument :code, String, required: true
    argument :enabled, Boolean, required: false
    argument :default, Boolean, required: false
    argument :analysis_test_ids, [Integer], required: false    
  
  end
end
