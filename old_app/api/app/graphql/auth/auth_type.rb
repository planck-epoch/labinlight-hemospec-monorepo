module Auth
  class AuthType < Types::BaseObject
    field :email, String, null: true
    field :token, String, null: true
  end
end
