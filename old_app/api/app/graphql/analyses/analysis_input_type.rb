module Analyses
  class AnalysisInputType < Types::BaseInputObject
    description "Attributes for creating or updating a analysis"

    argument :organization_id, Integer, required: true
    argument :analysis_bundle_id, Integer, required: true
    argument :process_number, String, required: true
    argument :sex_code, String, required: true
    argument :birth_year, Integer, required: true
    argument :country_code, String, required: false
    argument :health_number, String, required: false
    argument :phone, String, required: false
    argument :payload, GraphQL::Types::JSON, required: true
    argument :results, GraphQL::Types::JSON, required: true
  
  end
end
