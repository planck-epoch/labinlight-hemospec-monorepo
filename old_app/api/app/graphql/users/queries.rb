module Users
  module Queries
    def self.included(child_class)
      child_class.field :users, Users::UserType.collection_type, null: false, description: "An example field added by the generator" do
        argument :page, Integer, required: false
        argument :limit, Integer, required: false, default_value: 20, prepare: ->(limit, ctx) {[limit, 20].min}
        argument :order, String, required: false
        argument :direction, String, required: false
        argument :search, String, required: false
      end
      child_class.field :user, Users::UserType, null: false, description: "Find a user by ID" do
        argument :id, Integer, required: true
      end
    end

    def users(page: nil, limit: nil, order: nil, direction: nil, search: '')
      authorize_user_or_admin!

      order ||= 'id'
      order = order.underscore
      order = User.column_names.include?(order) ? order :
      direction = direction == 'asc' ? 'asc' : 'desc'

      users = User.all.order(Arel.sql("#{order} #{direction}"))
      users = users.search_all(search) unless search.blank?

      users.page(page).per(limit)
    end

    def user(id:)
      authorize_user_or_admin!

      if context[:current_user] && context[:current_user].id != id
        raise Exceptions::AuthenticationError
      end

      User.find(id)
    end
  end
end
