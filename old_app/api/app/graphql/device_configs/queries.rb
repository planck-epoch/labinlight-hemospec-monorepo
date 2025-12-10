module DeviceConfigs
  module Queries
    def self.included(child_class)
      child_class.field :device_configs, DeviceConfigs::DeviceConfigType.collection_type, null: false, description: "An example field added by the generator" do
        argument :page, Integer, required: false
        argument :limit, Integer, required: false, default_value: 20, prepare: ->(limit, ctx) {[limit, 20].min}
        argument :order, String, required: false
        argument :direction, String, required: false
        argument :search, String, required: false
      end
      child_class.field :device_config, DeviceConfigs::DeviceConfigType, null: false, description: "Find a DeviceConfig by ID" do
        argument :id, Integer, required: true
      end
    end

    def device_configs(page: nil, limit: nil, order: nil, direction: nil, search: '')
      authorize_user_or_admin!

      order ||= 'id'
      order = order.underscore
      order = DeviceConfig.column_names.include?(order) ? order :
      direction = direction == 'asc' ? 'asc' : 'desc'

      device_configs = DeviceConfig.all.order(Arel.sql("#{order} #{direction}"))
      device_configs = device_configs.search_all(search) unless search.blank?

      device_configs.page(page).per(limit)
    end

    def device_config(id:)
      authorize_user_or_admin!

      DeviceConfig.find(id)
    end
  end
end
