module Prices
  module Mutations
    def self.included(child_class)
      child_class.field :create_price, Prices::PriceType, null: false, description: "Create a Price" do
        argument :price, Prices::PriceInputType, required: true
      end

      child_class.field :update_price, Prices::PriceType, null: false, description: "Update a Price" do
        argument :id, Integer, required: true
        argument :price, Prices::PriceInputType, required: true
      end

      child_class.field :delete_price, [Prices::PriceType], null: false, description: "Destroy a Price" do
        argument :ids, [Integer], required: true
      end
    end

    def create_price(price:)
      authorize_user_or_admin!

      price = Price.new(price.to_h)

      if price.save
        price
      else
        raise GraphQL::ExecutionError, price.errors.full_messages.join(", ")
      end
    end

    def update_price(id:, price:)
      authorize_user_or_admin!

      Price.find(id).tap do |elem|
        elem.update!(price.to_h)
      end
    end

    def delete_price(ids:)
      authorize_user_or_admin!
      
      Price.where(id: ids).destroy_all
    end
  end
end
