module Admins
  module Queries
    def self.included(child_class)
      child_class.field :admins, Admins::AdminType.collection_type, null: false, description: "An example field added by the generator" do
        argument :page, Integer, required: false
        argument :limit, Integer, required: false, default_value: 20, prepare: ->(limit, ctx) {[limit, 20].min}
        argument :order, String, required: false
        argument :direction, String, required: false
        argument :search, String, required: false

        def self.visible?(context)
          super && context[:current_admin]
        end
      end
      child_class.field :admin, Admins::AdminType, null: false, description: "Find a admin by ID" do
        argument :id, Integer, required: true
      end
    end

    def admins(page: nil, limit: nil, order: nil, direction: nil, search: '')
      authorize_admin!

      order ||= 'id'
      order = order.underscore
      order = Admin.column_names.include?(order) ? order :
      direction = direction == 'asc' ? 'asc' : 'desc'

      admins = Admin.all.order(Arel.sql("#{order} #{direction}"))
      admins = admins.search_all(search) unless search.blank?

      admins.page(page).per(limit)
    end

    def admin(id:)
      authorize_admin!

      Admin.find(id)
    end
  end
end
