module Devices
  module Queries
    def self.included(child_class)
      child_class.field :devices, Devices::DeviceType.collection_type, null: false, description: "An example field added by the generator" do
        argument :page, Integer, required: false
        argument :limit, Integer, required: false, default_value: 20, prepare: ->(limit, ctx) {[limit, 20].min}
        argument :order, String, required: false
        argument :direction, String, required: false
        argument :search, String, required: false
      end
      child_class.field :device, Devices::DeviceType, null: false, description: "Find a Device by ID" do
        argument :id, Integer, required: true
      end
    end

    def devices(page: nil, limit: nil, order: nil, direction: nil, search: '')
      authorize_user_or_admin!

      order ||= 'id'
      order = order.underscore
      order = Device.column_names.include?(order) ? order :
      direction = direction == 'asc' ? 'asc' : 'desc'

      devices = Device.all.order(Arel.sql("#{order} #{direction}"))
      devices = devices.search_all(search) unless search.blank?

      devices.page(page).per(limit)
    end

    def device(id:)
      authorize_user_or_admin!

      Device.find(id)
    end
  end
end
