module Prices
  module Queries
    def self.included(child_class)
      child_class.field :prices, Prices::PriceType.collection_type, null: false, description: "An example field added by the generator" do
        argument :page, Integer, required: false
        argument :limit, Integer, required: false, default_value: 20, prepare: ->(limit, ctx) {[limit, 20].min}
        argument :order, String, required: false
        argument :direction, String, required: false
        argument :search, String, required: false
      end
      child_class.field :price, Prices::PriceType, null: false, description: "Find a Price by ID" do
        argument :id, Integer, required: true
      end
    end

    def prices(page: nil, limit: nil, order: nil, direction: nil, search: '')
      authorize_user_or_admin!

      order ||= 'id'
      order = order.underscore
      order = Price.column_names.include?(order) ? order :
      direction = direction == 'asc' ? 'asc' : 'desc'

      prices = Price.all.order(Arel.sql("#{order} #{direction}"))
      prices = prices.search_all(search) unless search.blank?

      prices.page(page).per(limit)
    end

    def price(id:)
      authorize_user_or_admin!

      Price.find(id)
    end
  end
end
