module Organizations
  class OrganizationInputType < Types::BaseInputObject
    description "Attributes for creating or updating a organization"

    argument :name, String, required: false
  
  end
end
