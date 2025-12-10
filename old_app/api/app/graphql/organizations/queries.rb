module Organizations
  module Queries
    def self.included(child_class)
      child_class.field :organizations, Organizations::OrganizationType.collection_type, null: false, description: "An example field added by the generator" do
        argument :page, Integer, required: false
        argument :limit, Integer, required: false, default_value: 20, prepare: ->(limit, ctx) {[limit, 20].min}
        argument :order, String, required: false
        argument :direction, String, required: false
        argument :search, String, required: false
      end
      child_class.field :organization, Organizations::OrganizationType, null: false, description: "Find a Organization by ID" do
        argument :id, Integer, required: true
      end
    end

    def organizations(page: nil, limit: nil, order: nil, direction: nil, search: '')
      authorize_user_or_admin!

      order ||= 'id'
      order = order.underscore
      order = Organization.column_names.include?(order) ? order :
      direction = direction == 'asc' ? 'asc' : 'desc'

      organizations = Organization.all.order(Arel.sql("#{order} #{direction}"))
      organizations = organizations.search_all(search) unless search.blank?

      organizations.page(page).per(limit)
    end

    def organization(id:)
      authorize_user_or_admin!

      Organization.find(id)
    end
  end
end
