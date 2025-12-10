module Organizations
  class OrganizationType < Types::BaseObject
    field :id, Integer, null: false

    field :name, String, null: true
  
    field :updated_at, GraphQL::Types::ISO8601DateTime, null: false
    field :created_at, GraphQL::Types::ISO8601DateTime, null: false
  end
end
