module Organizations
  module Mutations
    def self.included(child_class)
      child_class.field :create_organization, Organizations::OrganizationType, null: false, description: "Create a Organization" do
        argument :organization, Organizations::OrganizationInputType, required: true
      end

      child_class.field :update_organization, Organizations::OrganizationType, null: false, description: "Update a Organization" do
        argument :id, Integer, required: true
        argument :organization, Organizations::OrganizationInputType, required: true
      end

      child_class.field :delete_organization, [Organizations::OrganizationType], null: false, description: "Destroy a Organization" do
        argument :ids, [Integer], required: true
      end
    end

    def create_organization(organization:)
      authorize_user_or_admin!

      organization = Organization.new(organization.to_h)

      if organization.save
        organization
      else
        raise GraphQL::ExecutionError, organization.errors.full_messages.join(", ")
      end
    end

    def update_organization(id:, organization:)
      authorize_user_or_admin!

      Organization.find(id).tap do |elem|
        elem.update!(organization.to_h)
      end
    end

    def delete_organization(ids:)
      authorize_user_or_admin!
      
      Organization.where(id: ids).destroy_all
    end
  end
end
