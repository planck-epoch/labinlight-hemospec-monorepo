module Auth
  module Queries
    def self.included(child_class)
      child_class.field :me, Auth::AuthType, null: true do
        description 'Returns the current user'
        argument :resource_name, String, required: false
      end
    end

    def me(resource_name: 'user')
      resource_symbol = "current_#{resource_name.underscore}".to_sym
      context[resource_symbol]
    end
  end
end
