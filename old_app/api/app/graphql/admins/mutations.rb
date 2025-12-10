module Admins
  module Mutations
    def self.included(child_class)
      child_class.field :create_admin, Admins::AdminType, null: false, description: "Create a Admin" do
        argument :admin, Admins::AdminInputType, required: true
      end

      child_class.field :update_admin, Admins::AdminType, null: false, description: "Update a Admin" do
        argument :id, Integer, required: true
        argument :admin, Admins::AdminInputType, required: true
      end

      child_class.field :delete_admin, [Admins::AdminType], null: false, description: "Destroy a Admin" do
        argument :ids, [Integer], required: true
      end
    end

    def create_admin(admin:)
      authorize_admin!

      admin = Admin.new(admin.to_h)

      if admin.save
        admin
      else
        raise GraphQL::ExecutionError, admin.errors.full_messages.join(", ")
      end
    end

    def update_admin(id:, admin:)
      authorize_admin!

      Admin.find(id).tap do |elem|
        elem.update!(admin.to_h)
      end
    end

    def delete_admin(ids:)
      authorize_admin!
      
      Admin.where(id: ids).destroy_all
    end
  end
end
