module Types
  class QueryType < Types::BaseObject
    # Add root-level fields here.
    # They will be entry points for queries on your schema.
    include Auth::Queries
    include Users::Queries
    include Admins::Queries
    include Stats::Queries
    ### rgv queries ###
		include Analyses::Queries
		include Prices::Queries
    include AnalysisTests::Queries
		include AnalysisBundles::Queries
		include Devices::Queries
		include DeviceConfigs::Queries
		include Organizations::Queries
  end
end
