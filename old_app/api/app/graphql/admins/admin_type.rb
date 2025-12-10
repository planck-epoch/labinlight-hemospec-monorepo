module Admins
  class AdminType < Types::BaseObject
    field :id, Integer, null: false
    field :email, String, null: true
    field :reset_password_sent_at, GraphQL::Types::ISO8601DateTime, null: true
    field :created_at, GraphQL::Types::ISO8601DateTime, null: true
    field :updated_at, GraphQL::Types::ISO8601DateTime, null: true
    field :token, String, null: false

    def self.visible?(context)
      super && context[:current_admin]
    end
  end
end
