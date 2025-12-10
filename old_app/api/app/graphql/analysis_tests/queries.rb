module AnalysisTests
  module Queries
    def self.included(child_class)
      child_class.field :analysis_tests, AnalysisTests::AnalysisTestType.collection_type, null: false, description: "An example field added by the generator" do
        argument :page, Integer, required: false
        argument :limit, Integer, required: false, default_value: 20, prepare: ->(limit, ctx) {[limit, 20].min}
        argument :order, String, required: false
        argument :direction, String, required: false
        argument :search, String, required: false
      end
      child_class.field :analysis_test, AnalysisTests::AnalysisTestType, null: false, description: "Find a AnalysisTest by ID" do
        argument :id, Integer, required: true
      end
    end

    def analysis_tests(page: nil, limit: nil, order: nil, direction: nil, search: '')
      authorize_user_or_admin!

      order ||= 'id'
      order = order.underscore
      order = AnalysisTest.column_names.include?(order) ? order : 
      direction = direction == 'asc' ? 'asc' : 'desc'

      analysis_tests = AnalysisTest.all.order(Arel.sql("#{order} #{direction}"))
      analysis_tests = analysis_tests.search_all(search) unless search.blank?

      analysis_tests.page(page).per(limit)
    end

    def analysis_test(id:)
      authorize_admin!

      AnalysisTest.find(id)
    end
  end
end
