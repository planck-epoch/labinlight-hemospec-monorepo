module Types
  class MutationType < Types::BaseObject
    include Auth::Mutations
    include Users::Mutations
    include Admins::Mutations
    ### rgv mutations ###
		include Analyses::Mutations
		include Prices::Mutations
    include AnalysisTests::Mutations
		include AnalysisBundles::Mutations
		include Devices::Mutations
		include DeviceConfigs::Mutations
		include Organizations::Mutations
  end
end
