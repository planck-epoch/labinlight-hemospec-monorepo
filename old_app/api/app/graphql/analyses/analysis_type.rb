module Analyses
  class AnalysisType < Types::BaseObject
    field :id, Integer, null: false

    field :organization_id, Integer, null: true
    field :organization, Organizations::OrganizationType, null: true
    field :analysis_bundle_id, Integer, null: true
    field :analysis_bundle, AnalysisBundles::AnalysisBundleType, null: true
    field :process_number, String, null: true
    field :sex_code, String, null: true
    field :birth_year, Integer, null: true
    field :country_code, String, null: true
    field :health_number, String, null: true
    field :phone, String, null: true
    field :payload, GraphQL::Types::JSON, null: true
    field :results, GraphQL::Types::JSON, null: true
  
    field :updated_at, GraphQL::Types::ISO8601DateTime, null: false
    field :created_at, GraphQL::Types::ISO8601DateTime, null: false
  end
end
