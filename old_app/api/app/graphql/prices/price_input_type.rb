module Prices
  class PriceInputType < Types::BaseInputObject
    description "Attributes for creating or updating a price"

    argument :organization_id, Integer, required: true
    argument :analysis_bundle_id, Integer, required: true
    argument :value, Integer, required: true
  
  end
end
