module Admins
  class AdminInputType < Types::BaseInputObject
    description "Attributes for creating or updating a admin"
    argument :email, String, required: true
    argument :password, String, required: true
    argument :password_confirmation, String, required: true
  end
end
