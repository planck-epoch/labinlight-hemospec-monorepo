module Prices
  class PriceType < Types::BaseObject
    field :id, Integer, null: false

    field :organization_id, Integer, null: false
    field :organization, Organizations::OrganizationType, null: true
    field :analysis_bundle_id, Integer, null: false
    field :analysis_bundle, AnalysisBundles::AnalysisBundleType, null: true
    field :value, Integer, null: false
  
    field :updated_at, GraphQL::Types::ISO8601DateTime, null: false
    field :created_at, GraphQL::Types::ISO8601DateTime, null: false
  end
end
