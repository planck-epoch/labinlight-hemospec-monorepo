module AnalysisTests
  class AnalysisTestType < Types::BaseObject
    field :id, Integer, null: false

    field :name, String, null: false
    field :code, String, null: false
    field :test_type, String, null: false
    field :unit, String, null: true
    field :reference_male, String, null: true
    field :reference_female, String, null: true
    field :reference_value, Float, null: true

    field :analysis_bundles, [AnalysisBundles::AnalysisBundleType], null: true    
  
    field :updated_at, GraphQL::Types::ISO8601DateTime, null: false
    field :created_at, GraphQL::Types::ISO8601DateTime, null: false
  end
end
