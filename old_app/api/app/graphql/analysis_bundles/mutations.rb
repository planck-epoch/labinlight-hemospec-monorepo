module AnalysisBundles
  module Mutations
    def self.included(child_class)
      child_class.field :create_analysis_bundle, AnalysisBundles::AnalysisBundleType, null: false, description: "Create a AnalysisBundle" do
        argument :analysis_bundle, AnalysisBundles::AnalysisBundleInputType, required: true
      end

      child_class.field :update_analysis_bundle, AnalysisBundles::AnalysisBundleType, null: false, description: "Update a AnalysisBundle" do
        argument :id, Integer, required: true
        argument :analysis_bundle, AnalysisBundles::AnalysisBundleInputType, required: true
      end

      child_class.field :delete_analysis_bundle, [AnalysisBundles::AnalysisBundleType], null: false, description: "Destroy a AnalysisBundle" do
        argument :ids, [Integer], required: true
      end
    end

    def create_analysis_bundle(analysis_bundle:)
      authorize_user_or_admin!

      analysis_bundle = AnalysisBundle.new(analysis_bundle.to_h)

      if analysis_bundle.save
        analysis_bundle
      else
        raise GraphQL::ExecutionError, analysis_bundle.errors.full_messages.join(", ")
      end
    end

    def update_analysis_bundle(id:, analysis_bundle:)
      authorize_user_or_admin!

      AnalysisBundle.find(id).tap do |elem|
        elem.update!(analysis_bundle.to_h)
      end
    end

    def delete_analysis_bundle(ids:)
      authorize_user_or_admin!
      
      AnalysisBundle.where(id: ids).destroy_all
    end
  end
end
