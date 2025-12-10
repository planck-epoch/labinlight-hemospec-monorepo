module AnalysisBundles
  module Queries
    def self.included(child_class)
      child_class.field :analysis_bundles, AnalysisBundles::AnalysisBundleType.collection_type, null: false, description: "An example field added by the generator" do
        argument :page, Integer, required: false
        argument :limit, Integer, required: false, default_value: 20, prepare: ->(limit, ctx) {[limit, 20].min}
        argument :order, String, required: false
        argument :direction, String, required: false
        argument :search, String, required: false
      end
      child_class.field :analysis_bundle, AnalysisBundles::AnalysisBundleType, null: false, description: "Find a AnalysisBundle by ID" do
        argument :id, Integer, required: true
      end
    end

    def analysis_bundles(page: nil, limit: nil, order: nil, direction: nil, search: '')
      authorize_user_or_admin!

      order ||= 'id'
      order = order.underscore
      order = AnalysisBundle.column_names.include?(order) ? order :
      direction = direction == 'asc' ? 'asc' : 'desc'

      analysis_bundles = AnalysisBundle.all.order(Arel.sql("#{order} #{direction}"))
      analysis_bundles = analysis_bundles.search_all(search) unless search.blank?

      analysis_bundles.page(page).per(limit)
    end

    def analysis_bundle(id:)
      authorize_user_or_admin!

      AnalysisBundle.find(id)
    end
  end
end
