module AnalysisTests
  module Mutations
    def self.included(child_class)
      child_class.field :create_analysis_test, AnalysisTests::AnalysisTestType, null: false, description: "Create a AnalysisTest" do
        argument :analysis_test, AnalysisTests::AnalysisTestInputType, required: true
      end

      child_class.field :update_analysis_test, AnalysisTests::AnalysisTestType, null: false, description: "Update a AnalysisTest" do
        argument :id, Integer, required: true
        argument :analysis_test, AnalysisTests::AnalysisTestInputType, required: true
      end

      child_class.field :delete_analysis_test, [AnalysisTests::AnalysisTestType], null: false, description: "Destroy a AnalysisTest" do
        argument :ids, [Integer], required: true
      end
    end

    def create_analysis_test(analysis_test:)
      authorize_user_or_admin!

      analysis_test = AnalysisTest.new(analysis_test.to_h)

      if analysis_test.save
        analysis_test
      else
        raise GraphQL::ExecutionError, analysis_test.errors.full_messages.join(", ")
      end
    end

    def update_analysis_test(id:, analysis_test:)
      authorize_user_or_admin!

      AnalysisTest.find(id).tap do |elem|
        elem.update!(analysis_test.to_h)
      end
    end

    def delete_analysis_test(ids:)
      authorize_user_or_admin!
      
      AnalysisTest.where(id: ids).destroy_all
    end
  end
end
