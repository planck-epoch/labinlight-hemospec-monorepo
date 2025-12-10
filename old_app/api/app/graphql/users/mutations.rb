module Users
  module Mutations
    def self.included(child_class)
      child_class.field :create_user, Users::UserType, null: false, description: "Create a User" do
        argument :user, Users::UserInputType, required: true
      end

      child_class.field :update_user, Users::UserType, null: false, description: "Update a User" do
        argument :id, Integer, required: true
        argument :user, Users::UserInputType, required: true
      end

      child_class.field :delete_user, [Users::UserType], null: false, description: "Destroy a User" do
        argument :ids, [Integer], required: true
      end
    end

    def create_user(user:)
      authorize_admin!

      user = User.new(user.to_h)

      if user.save
        user
      else
        raise GraphQL::ExecutionError, user.errors.full_messages.join(", ")
      end
    end

    def update_user(id:, user:)
      authorize_admin!

      if context[:current_user] && context[:current_user].id != id
        raise Exceptions::AuthenticationError
      end

      User.find(id).tap do |elem|
        elem.update!(user.to_h)
      end
    end

    def delete_user(ids:)
      authorize_admin!
      
      User.where(id: ids).destroy_all
    end
  end
end
