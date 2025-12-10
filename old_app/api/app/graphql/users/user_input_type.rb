module Users
  class UserInputType < Types::BaseInputObject
    description "Attributes for creating or updating a user"
    argument :email, String, required: true
    argument :password, String, required: true
    argument :password_confirmation, String, required: true
  end
end
