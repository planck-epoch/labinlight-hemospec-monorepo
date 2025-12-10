module Analyses
  module Mutations
    def self.included(child_class)
      child_class.field :create_analysis, Analyses::AnalysisType, null: false, description: "Create a Analysis" do
        argument :analysis, Analyses::AnalysisInputType, required: true
      end

      child_class.field :update_analysis, Analyses::AnalysisType, null: false, description: "Update a Analysis" do
        argument :id, Integer, required: true
        argument :analysis, Analyses::AnalysisInputType, required: true
      end

      child_class.field :delete_analysis, [Analyses::AnalysisType], null: false, description: "Destroy a Analysis" do
        argument :ids, [Integer], required: true
      end
    end

    def create_analysis(analysis:)
      authorize_user_or_admin!

      analysis = Analysis.new(analysis.to_h)

      if analysis.save
        analysis
      else
        raise GraphQL::ExecutionError, analysis.errors.full_messages.join(", ")
      end
    end

    def update_analysis(id:, analysis:)
      authorize_user_or_admin!

      Analysis.find(id).tap do |elem|
        elem.update!(analysis.to_h)
      end
    end

    def delete_analysis(ids:)
      authorize_user_or_admin!
      
      Analysis.where(id: ids).destroy_all
    end
  end
end
