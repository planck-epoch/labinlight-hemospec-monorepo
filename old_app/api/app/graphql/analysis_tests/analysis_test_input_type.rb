module AnalysisTests
  class AnalysisTestInputType < Types::BaseInputObject
    description "Attributes for creating or updating a analysis_test"

    argument :name, String, required: true
    argument :code, String, required: true
    argument :test_type, String, required: true
    argument :unit, String, required: false
    argument :reference_male, String, required: false
    argument :reference_female, String, required: false
    argument :reference_value, Float, required: false
    argument :analysis_bundles, [Integer], required: false
  end
end
