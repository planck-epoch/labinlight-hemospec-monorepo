module AnalysisBundles
  class AnalysisBundleType < Types::BaseObject
    field :id, Integer, null: false

    field :name, String, null: false
    field :code, String, null: false
    field :enabled, Boolean, null: false
    field :default, Boolean, null: false

    field :analysis_tests, [AnalysisTests::AnalysisTestType], null: true
    field :analysis_test_ids, [Integer], null: true
  
    field :updated_at, GraphQL::Types::ISO8601DateTime, null: false
    field :created_at, GraphQL::Types::ISO8601DateTime, null: false
  end
end
